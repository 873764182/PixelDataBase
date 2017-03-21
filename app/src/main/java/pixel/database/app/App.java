package pixel.database.app;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import pixel.database.library.OnDbUpdateCallback;
import pixel.database.library.PixelDao;

/**
 * Created by pixel on 2017/3/20.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PixelDao.initDataBase(getApplicationContext(), "pdb.db", 1, UserTable.class);

        PixelDao.initDataBase(this, "pdb.db", 2, new OnDbUpdateCallback() {
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                Log.e(App.class.getSimpleName(), "数据库版本 -> " + oldVersion + "\t" + newVersion);
            }
        }, UserTable.class);   // 初始化数据库与创建数据库表
    }
}
