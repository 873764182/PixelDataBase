package pixel.database.library;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by pixel on 2017/3/20.
 */

public class DataBaseHelper extends SQLiteOpenHelper {
    private OnDbUpdateCallback onDbUpdateCallback;
    private Class<?>[] tables;

    public DataBaseHelper(Context context, String name, int version, Class<?>[] tables, OnDbUpdateCallback onDbUpdateCallback) {
        super(context, name, null, version);
        this.onDbUpdateCallback = onDbUpdateCallback;
        this.tables = tables;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 回传数据库对象
        SqlTemplate.setSqLiteDatabase(db);
        // 执行建表操作
        SqlTemplate.createTable(tables);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 回传数据库对象
        SqlTemplate.setSqLiteDatabase(db);
        // 回传更新数据库更新事件
        if (onDbUpdateCallback != null) {
            onDbUpdateCallback.onUpgrade(db, oldVersion, newVersion, tables);
        } else {
            // 删除目前所有的表
            SqlTemplate.deleteTable(tables);
            // 重新生成所有数据库表
            SqlTemplate.createTable(tables);
        }
    }
}
