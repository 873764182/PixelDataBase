## 轻量的 Android SQLite 数据库工具集合
        根据Java实体自动生成数据库表.
        
        不需要书写任何SQL语句即可实现对数据库的 创表 增 删 查 改 分页 操作.
         
        所有的操作方法都是静态的方法,依赖库是一个仅为 10+ K 大小的jar,代码污染率极低.所有的操作方法都集成在 "PixelDao"与"PixelTools"两个对象上.PixelTools是PixelDao的子类.直接使用PixelTools即可.

## 开始使用

### 1. 导入一个依赖jar包到你的项目的lib目录,加入你的项目编译环境.
        pixel-data-base.jar (在项目根目录可以找到)

### 2. 创建你的实体类
        为需要实例化到数据库的字段添加 "@MapField" 注解, 如下:

        @MapField
        public String name;

        支持的类型: Integer, Long, Double, Byte, String. 其他类型将会实例化到数据库时失败.

### 3. 初始化
#### 传入数据库 名称 与 需要生成表的 Java实体 对象列表(第二步建立的实体对象).
        PixelDao.initDataBase(getApplicationContext(), "pdb.db", 1, UserTable.class, MsgTable.class);
#### 如果需要监听数据库版本变更,初始化时可以传入一个监听器.
        PixelDao.initDataBase(this, "pdb.db", 2, new OnDbUpdateCallback() {
             @Override
             public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion, Class<?>... tables) {
                  if (oldVersion < newVersion) {
                      for (Class<?> table : tables) {
                          if (table == UserTable.class) {
                              PixelTools.updateTable(table, null);    // 更新表结构,不保留原数据
                          }
                      }
                  }
             }
        }, UserTable.class, MsgTable.class);

### 4. 增 删 查 改  (数据表列名与实体属性名相同. 表名为实体的全路径名把"."替换为"_"后的字符串.)
        PixelDao.insert(new UserTable("测试", 100));  // 插入
        
        PixelDao.update(new UserTable("运行", 200), _id, "_id");  // 更新 (条件: 数据库"_id"列要等于_id变量的值)
        
        PixelDao.delete(UserTable.class, _id, "_id"); // 删除 (条件: 数据库"_id"列要等于_id变量的值)
        
        List<UserTable> userTables = PixelDao.query(UserTable.class); // 查询 (无条件,读取所有.)
        
        List<UserTable> userTables = PixelDao.query(UserTable.class, _id, "_id"); // 查询 (有条件: 数据库"_id"列要等于_id变量的值)
        
        List<UserTable> userTables = PixelDao.query(UserTable.class, _id, "_id", 20L, 0L); // 分页查询 (有条件: 数据库"_id"列要等于_id变量的值, 查询第0页, 每页20条.)

### 5. 更新表结构
        // 需要保留原数据,传入旧列名与新列名的对应关系,没变更的列名也需要传入.
        PixelTools.updateTable(UserTable.class, new ArrayList\<ColumnMapping>() {{ 
             add(new ColumnMapping("username", "name")); // ColumnMapping 参数说明: 1. 原列名, 2. 新列名
             add(new ColumnMapping("age", "age"));
        }});
        // 如果不需要保留原数据
        PixelTools.updateTable(UserTable.class, null);  // 映射参数传入 null, 仅仅更新表结构,不保留原数据.

### 6. 如果需要更多操作,可以通过以下方法获取SQLiteDatabase对象.
        SQLiteDatabase database = PixelDao.getSQLiteDatabase();

### 7. 目前已知的还需要优化的点
        封装对多表的联合操作,但是这样可能需要建立类似Hibernate那样的映射XML配置文件,这样会增加库的复杂度.
        
## 联系我
        [873764182@qq.com](https://mail.qq.com/cgi-bin/frame_html)
