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

    public DataBaseHelper(Context context, String name, int version, OnDbUpdateCallback onDbUpdateCallback, Class<?>... tables) {
        super(context, name, null, version);
        this.onDbUpdateCallback = onDbUpdateCallback;
        this.tables = tables;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        PixelDao.setSqLiteDatabase(db);
        PixelDao.createTable(tables);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        PixelDao.setSqLiteDatabase(db);
        if (onDbUpdateCallback != null) {
            onDbUpdateCallback.onUpgrade(db, oldVersion, newVersion, tables);
        }
    }
}
