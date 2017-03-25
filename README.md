# 轻量的 Android SQLite 数据库工具集合
##### 根据Java实体自动生成数据库表.
##### 不需要书写任何SQL语句即可实现对数据库的 创表 增 删 查 改 分页 操作. 
##### 所有的操作方法都是静态的方法,依赖库是一个仅为9k大小的jar,代码污染率极低.
## 开始使用
### 1. 导入一个依赖jar包到你的项目的lib目录,加入你的项目编译环境.
#### pixel-data-base.jar (在项目根目录可以找到)
### 2. 初始化
####  // 传入数据库 名称 与 需要生成表的 Java实体 对象列表.
######  PixelDao.initDataBase(getApplicationContext(), "pdb.db", 1, UserTable.class, MsgTable.class);
####   // 如果需要监听数据库版本变更,初始化时可以传入一个监听器.
###### // PixelDao.initDataBase(this, "pdb.db", 2, new OnDbUpdateCallback() {
###### //            &nbsp;&nbsp;@Override
###### //            &nbsp;&nbsp;public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion, Class<?>... tables) {
###### //                &nbsp;&nbsp;&nbsp;&nbsp;Log.e(App.class.getSimpleName(), "数据库版本 -> " + oldVersion + "\t" + newVersion);
###### //            &nbsp;&nbsp;}
###### //        }, UserTable.class, MsgTable.class);
### 3. 增 删 查 改  (数据表列名与实体属性名相同. 表名为实体的全路径名把"."替换为"_"后的字符串.)
###### PixelDao.insert(new UserTable("测试", 100));  // 插入
###### PixelDao.update(new UserTable("运行", 200), _id, "_id");  // 更新 (条件: 数据库"_id"列要等于_id变量的值)
###### PixelDao.delete(UserTable.class, _id, "_id"); // 删除 (条件: 数据库"_id"列要等于_id变量的值)
###### List<UserTable> userTables = PixelDao.query(UserTable.class); // 查询 (无条件,读取所有.)
###### List<UserTable> userTables = PixelDao.query(UserTable.class, _id, "_id"); // 查询 (有条件: 数据库"_id"列要等于_id变量的值)
###### List<UserTable> userTables = PixelDao.query(UserTable.class, _id, "_id", 20L, 0L); // 分页查询 (有条件: 数据库"_id"列要等于_id变量的值, 查询第0页, 每页20条.)
### 如果以上的操作不能满足需求,可以通过以下方法获取数据库对象,执行自定义SQL语句.
###### SQLiteDatabase database = PixelDao.getSQLiteDatabase();
### 目前已知的还需要优化的点
###### 增加对多表的联合操作.
