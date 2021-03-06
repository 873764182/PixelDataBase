## 轻量的 Android SQLite 数据库工具集合, 适合用SQLite做一些数据存储的项目.
        根据Java实体自动生成数据库表.
        
        不需要书写任何SQL语句即可实现对数据库的 创表 增 删 查 改 分页 操作.
         
        所有的操作方法都是静态的方法,依赖库是一个仅为 10+ K 大小的jar,代码污染率极低.所有的操作方法都集成在 "SqlTemplate" 对象.

## 开始使用

### 1. 导入一个依赖jar包到你的项目的lib目录,加入你的项目编译环境.
        pixel-data-base.jar (在项目根目录可以找到)

### 2. 创建你的实体类
        支持Java的八大基础类型与String类型数据,不支持数组,不支持List,基本类型与String类型之外的其他类型将会实例化到数据库时失败.
        为需要实例化到数据库的字段添加 "@TableColumn" 注解, 要实例化到数据库表中的属性名不能是"_id",因为数据库中自己会建立一个列为"_id".
        如下:

        /**
         * 数据库实体 (就是一个普通的Java对象)
         */
        public class UserTable {

            public Long id;    // 没有 @TableColumn 注解在数据库表中不会创建该字段

            // @TableColumn
            // public Long _id;    // 错误,不能用该名字,数据库表中已经存在改列名,属于关键字,不能使用.

            @TableColumn
            public String name; // 在数据库表中会创建一个名为'name'的列

            @TableColumn
            public Integer age; // 在数据库表中会创建一个名为'age'的列

            // @TableColumn
            // public List<String> list;   // 创建失败,不支持List或者数组等集合的属性

        }

### 3. 初始化
#### 最好在Application对象的onCreate中初始化,传入数据库 名称 与 需要生成表的 Java实体 对象列表(第二步建立的实体对象).

        SqlTemplate.initDataBase(getApplicationContext(), "pdb.db", 1, UserTable.class, MsgTable.class);

#### 如果需要监听数据库版本变更,初始化时可以传入一个监听器.

        // 需要生成数据表的对象 注意: 数据名是对象的全路径名,不能随意修改对象包名与对象名.
        Class<?>[] tables = new Class[]{UserTable.class, MsgTable.class, TestTable.class, BaseTable.class};

        // 直接修改版本号，不传入回调接口数据库会重建所有表，现有的表数据会被删除。
        // SqlTemplate.initDataBase(this, "pdb.db", 3, tables);
        
        // 初始化数据库与创建数据库表 version修改后,onUpgrade方法会被回调.
        SqlTemplate.initDataBase(this, "pdb.db", 3, tables, new OnDbUpdateCallback() {
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion, Class<?>... tables) {
                if (oldVersion >= newVersion) {
                    return;
                }
                for (Class<?> table : tables) {
                    // 自定义修改部分表
                    if (table == UserTable.class) {
                        // 如果对象中有2两个属性有映射到数据库，则则两个字段都要声明，即使没有变更也要加入ColumnMapping。
                        List<ColumnMapping> columnMappingList = new ArrayList<ColumnMapping>() {{
                            // add(new ColumnMapping("name", "username")); // 数据库的列名从'user'变为'username',将原来'user'列的数据库,转移到新的'username'列上.
                            add(new ColumnMapping("username", "name")); // 数据库的列名从'username'变为'user',将原来'username'列的数据库,转移到新的'user'列上.
                            add(new ColumnMapping("age", "age")); // 'age'属性未变更也需要声明
                        }};
                        SqlTemplate.updateOrCreateTable(table, columnMappingList);    // 更新表结构,保留原数据.
                    }
                    /*else {
                        SqlTemplate.updateOrCreateTable(table, null);    // 更新表结构,不保留原数据.
                    }*/
                }
            }
        });
                 

### 4. 增 删 查 改  (数据表列名与实体属性名相同. 表名为实体的全路径名把"."替换为"_"后的字符串.)
        long count = SqlTemplate.getTableRowCount(UserTable.class);    // 获取 UserTable 数据表中有多少行数据

        SqlTemplate.insert(new UserTable("测试", 100));  // 插入
        
        SqlTemplate.update(new UserTable("运行", 200), _id, "_id");  // 更新 (条件: 数据库"_id"列要等于_id变量的值)
        
        SqlTemplate.delete(UserTable.class, _id, "_id"); // 删除 (条件: 数据库"_id"列要等于_id变量的值)
        
        List<UserTable> userTables = SqlTemplate.query(UserTable.class); // 查询 (无条件,读取所有.)
        
        List<UserTable> userTables = SqlTemplate.query(UserTable.class, _id, "_id"); // 查询 (有条件: 数据库"_id"列要等于_id变量的值)
        
        List<UserTable> userTables = SqlTemplate.query(UserTable.class, _id, "_id", 20L, 0L); // 分页查询 (有条件: 数据库"_id"列要等于_id变量的值, 查询第0页, 每页20条.)

        需要更复杂的查询可以考虑使用: SqlTemplate.querySupport(),或者获取SQLiteDatabase对象直接执行SQL语句进行操作.

### 5. 更新表结构
        // 需要保留原数据,传入旧列名与新列名的对应关系,没变更的列名也需要传入.
        PixelTools.updateTable(UserTable.class, new ArrayList\<ColumnMapping>() {{ 
             add(new ColumnMapping("username", "name")); // ColumnMapping 参数说明: 1. 原列名, 2. 新列名
             add(new ColumnMapping("age", "age"));  // 将未更新前的'age'列的值保存到更新后的'age'值 (即使字段名没有修改,也需要这样操作,不然之前的'age'字段的数据将会丢失.)
        }});
        // 如果不需要保留原数据
        PixelTools.updateTable(UserTable.class, null);  // 映射参数传入 null, 仅仅更新表结构,不保留原数据,原来的数据会丢失.

### 6. 如果需要获取数据库表中的自增长字段的值,可以使你的实体对象(如:UserTable对象)实现'OnDbIdCallback'接口.在执行查询的时候会自动返回数据库自增长值.
        public class UserTable implements OnDbIdCallback {
            @Override
            public void setId(Long _id) {
                this.id = _id; // OnDbIdCallback接口方法,回传数据库自增长的ID的值.
            }
        }

### 7. 如果需要更多操作,可以通过以下方法获取SQLiteDatabase对象.
        SQLiteDatabase database = SqlTemplate.getSQLiteDatabase();

### 8. 目前已知的还需要优化的点
        1. 封装对多表的联合查询操作,但是这样可能需要建立类似Hibernate那样的映射文件,支持库的复杂度会增加.
        2. 增加对byte[]类型的支持.
        3. 更新表结构的策略需要修改，不传入回调接口，默认在原来表基础上增加字段，不删除表数据。实际情况很少出现删除字段与修改字段的情况，大部分都是增加字段的情况。
        
### 9. 联系我
        [873764182@qq.com](https://mail.qq.com/cgi-bin/frame_html)
