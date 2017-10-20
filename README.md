## 轻量的 Android SQLite 数据库工具集合, 适合用SQLite做一些数据存储的项目.
        根据Java实体自动生成数据库表.
        
        不需要书写任何SQL语句即可实现对数据库的 创表 增 删 查 改 分页 操作.
         
        所有的操作方法都是静态的方法,依赖库是一个仅为 10+ K 大小的jar,代码污染率极低.所有的操作方法都集成在 "SqlTemplate" 对象.

## 开始使用

### 1. 导入一个依赖jar包到你的项目的lib目录,加入你的项目编译环境.
        pixel-data-base.jar (在项目根目录可以找到)

### 2. 创建你的实体类
        为需要实例化到数据库的字段添加 "@TableColumn" 注解, 如下:

        @TableColumn
        public String name;

        支持Java的八大基础类型与String类型数据,不支持数组,不支持List,基本类型与String类型之外的其他类型将会实例化到数据库时失败.

### 3. 初始化
#### 最好在Application对象的onCreate中初始化,传入数据库 名称 与 需要生成表的 Java实体 对象列表(第二步建立的实体对象).

        SqlTemplate.initDataBase(getApplicationContext(), "pdb.db", 1, UserTable.class, MsgTable.class);

#### 如果需要监听数据库版本变更,初始化时可以传入一个监听器.

        // 需要生成数据表的对象 注意: 数据名是对象的全路径名,初始化后不能随意修改对象包名与对象名,否则需要增加数据库版本号重新初始化.
        Class<?>[] tables = new Class[]{UserTable.class, MsgTable.class, TestTable.class};

        // 初始化数据库与创建数据库表 version修改后,onUpgrade方法会被回调.
        SqlTemplate.initDataBase(this, "pdb.db", 4, tables, new OnDbUpdateCallback() {
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion, Class<?>... tables) {
                Log.e(App.class.getSimpleName(), "数据库版本 -> " + oldVersion + "\t" + newVersion);
                if (oldVersion < newVersion) {

                    // PixelDao.deleteTable(tables);    // 删除目前所有的表
                    // PixelDao.createTable(tables);    // 重新生成所有数据库表

                    for (Class<?> table : tables) {
                        // 修改用户表变更
                        if (table == UserTable.class) {
                            List<ColumnMapping> columnMappingList = new ArrayList<ColumnMapping>() {{
                                // add(new ColumnMapping("name", "username")); // 数据库的列名从'user'变为'username',将原来'user'列的数据库,转移到新的'username'列上.
                                add(new ColumnMapping("username", "name")); // 数据库的列名从'username'变为'user',将原来'username'列的数据库,转移到新的'user'列上.
                            }};
                            SqlTemplate.updateOrCreateTable(table, columnMappingList);    // 更新表结构,保留原数据.
                        }
                    }
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

### 6. 如果需要更多操作,可以通过以下方法获取SQLiteDatabase对象.
        SQLiteDatabase database = SqlTemplate.getSQLiteDatabase();

### 7. 目前已知的还需要优化的点
        1. 封装对多表的联合查询操作,但是这样可能需要建立类似Hibernate那样的映射文件,支持库的复杂度会增加.
        
## 联系我
        [873764182@qq.com](https://mail.qq.com/cgi-bin/frame_html)
