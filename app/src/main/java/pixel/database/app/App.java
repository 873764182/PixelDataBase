package pixel.database.app;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.facebook.stetho.Stetho;

import pixel.database.library.OnDbUpdateCallback;
import pixel.database.library.SqlTemplate;

/**
 * Created by pixel on 2017/3/20.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initializeWithDefaults(this);    // TODO 数据库调试 发布时去掉

        SqlTemplate.initDataBase(this, "pdb.db", 2, new OnDbUpdateCallback() {
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion, Class<?>... tables) {
                Log.e(App.class.getSimpleName(), "数据库版本 -> " + oldVersion + "\t" + newVersion);
                if (oldVersion < newVersion) {
                    // 删除目前所有的表
//                    PixelDao.deleteTable(tables);
                    // 重新生成数据库表
//                    PixelDao.createTable(tables);

                    for (Class<?> table : tables) {
                        if (table == UserTable.class) {
                            SqlTemplate.updateOrCreateTable(table, null);    // 更新表结构,不保留原数据
                        }
                    }
                }
            }
        }, UserTable.class, MsgTable.class, TestTable.class);   // 初始化数据库与创建数据库表
    }
}
