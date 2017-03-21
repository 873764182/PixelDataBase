package pixel.database.library;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by pixel on 2017/3/21.
 */

public interface OnDbUpdateCallback {
    void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
}
