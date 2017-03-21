# PixelDataBase
简单的Android本地数据库支持库,目前已经实现根据Java对象自动生成数据库表和与对应的增删查改操作方法.
# 导入jar包
pixel-db.jar 
# 初始化(UserTable 是需要映射到SQLite里的一个普通实体,可以同时传入多个,每一个都会被映射成一个数据库表,所有非下划线开头的属性会被创建为表的列)
 PixelDao.initDataBase(getApplicationContext(), "pdb.db", 1, UserTable.class);
 需要监听数据库版本变更
 PixelDao.initDataBase(this, "pdb.db", 2, new OnDbUpdateCallback() {
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                Log.e(App.class.getSimpleName(), "数据库版本 -> " + oldVersion + "\t" + newVersion);
            }
        }, UserTable.class);
# 增删查改操作
PixelDao.insert(new UserTable("测试", 100));  // 插入
List<Object> objects = PixelDao.query(UserTable.class, _id, "_id"); // 查询
PixelDao.update(new UserTable("运行", 200), _id, "_id");  // 更新
PixelDao.delete(UserTable.class, _id, "_id"); // 删除
