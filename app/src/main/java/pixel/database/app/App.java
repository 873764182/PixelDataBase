package pixel.database.app;

import android.app.Application;

import pixel.database.library.PDB;

/**
 * Created by pixel on 2017/3/20.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PDB.initDataBase(this, "pdb.db", 1, UserTable.class);   // 初始化数据库与创建数据库表
    }
}
