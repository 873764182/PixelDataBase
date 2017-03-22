# Android SQLite
#### 简单的Android本地数据库支持库,目前已经实现根据普通Java对象自动生成数据库表,和生成与表对应的增删查改操作方法.
## 导入依赖jar包
##### pixel-data-base.jar 
# 开始使用
## 初始化
######  // pdb.db 数据库名称
######  // UserTable 是需要映射到SQLite里的一个普通实体,可以同时传入多个.
######  // 每一个都会被映射成一个数据库表,所有非下划线开头的属性会被映射为表的一列.
######  PixelDao.initDataBase(getApplicationContext(), "pdb.db", 1, UserTable.class);
###### // 如果需要监听数据库版本变更,初始化时可以传入一个监听器.
###### PixelDao.initDataBase(this, "pdb.db", 2, new OnDbUpdateCallback() {
######            @Override
######            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
######                Log.e(App.class.getSimpleName(), "数据库版本 -> " + oldVersion + "\t" + newVersion);
######            }
######        }, UserTable.class);
## 增删查改操作
###### PixelDao.insert(new UserTable("测试", 100));  // 插入
###### List<Object> objects = PixelDao.query(UserTable.class, _id, "_id"); // 查询 (条件: 数据库"_id"列要等于_id变量的值)
###### PixelDao.update(new UserTable("运行", 200), _id, "_id");  // 更新 (条件: 数据库"_id"列要等于_id变量的值)
###### PixelDao.delete(UserTable.class, _id, "_id"); // 删除 (条件: 数据库"_id"列要等于_id变量的值)
## 获取数据库对象
###### SQLiteDatabase database = PixelDao.getSQLiteDatabase();
