package pixel.database.library;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pixel on 2017/3/20.
 * <p>
 * SQLite支持的数据类型: NULL(空)、INTEGER(整数)、REAL（浮点数字）、TEXT(字符串文本)和BLOB(二进制对象)
 */

public class PDB {
    private static SQLiteOpenHelper mSqLiteOpenHelper = null;
    private static SQLiteDatabase mSqLiteDatabase = null;

    public synchronized static void initDataBase(Context context, String name, int version, Class<?>... tables) {
        mSqLiteOpenHelper = new DataBaseHelper(context, name, version);
        for (Class<?> cls : tables) {
            createTable(getSQLiteDatabase(), getTableName(cls), getColumnInfo(cls));
        }
    }

    public synchronized static SQLiteDatabase getSQLiteDatabase() {
        if (mSqLiteOpenHelper == null) {
            throw new NumberFormatException("请先调用 initDataBase() 初始化数据库");
        }
        if (mSqLiteDatabase == null) {
            mSqLiteDatabase = mSqLiteOpenHelper.getWritableDatabase();
        }
        return mSqLiteDatabase;
    }

    private static String getTableName(Class<?> cls) {
        return cls.getName().replace(".", "_");
    }

    private static List<ColumnInfo> getColumnInfo(Class<?> cls) {
        List<ColumnInfo> columnInfos = new ArrayList<>();
        Field[] fields = cls.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                // CREATE TABLE IF NOT EXISTS pixel_database_app_UserTable ( _id INTEGER PRIMARY KEY AUTOINCREMENT, age TEXT, name TEXT, $change TEXT, serialVersionUID TEXT )
                if (field.getName().contains("$change") || field.getName().contains("serialVersionUID") || "_id".equalsIgnoreCase(field.getName())) {
                    continue;
                }
                field.setAccessible(true); // field.isAccessible() 过滤私有的属性
                columnInfos.add(new ColumnInfo(field.getName(), field.getType().getName(), field));
            }
        }
        return columnInfos;
    }

    private static void createTable(SQLiteDatabase sqLiteDatabase, String tableName, List<ColumnInfo> columnInfos) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append(tableName).append(" ( _id INTEGER PRIMARY KEY AUTOINCREMENT, ");
        for (ColumnInfo info : columnInfos) {
            sql.append(info.columnName).append(" ").append("TEXT").append(", ");    // info.typeString
        }
        sql.append(" ) ");
        String sqlStr = sql.toString().replace("  ", " ").replace(", )", " )");  // 去掉多余字符
        sqLiteDatabase.execSQL(sqlStr);
    }

    // ============================================================================================= 增删查改

    public static void insert(Object object) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(getTableName(object.getClass())).append(" ( ");
        List<ColumnInfo> columnInfos = getColumnInfo(object.getClass());
        Object[] params = new Object[columnInfos.size()];

        for (int i = 0; i < columnInfos.size(); i++) {
            sql.append(columnInfos.get(i).columnName);
            if (i != columnInfos.size() - 1) {
                sql.append(", ");
            }

            try {
                params[i] = columnInfos.get(i).field.get(object);   // 获取参数
            } catch (IllegalAccessException e) {
                params[i] = "";
            }
        }
        sql.append(" ) VALUES ( ");
        for (int i = 0; i < columnInfos.size(); i++) {
            sql.append(" ? ");
            if (i != columnInfos.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(" ) ");
        String sqlStr = sql.toString().replace("  ", " ");
        try {
            getSQLiteDatabase().beginTransaction();
            getSQLiteDatabase().execSQL(sqlStr, params);
            getSQLiteDatabase().setTransactionSuccessful();
        } finally {
            getSQLiteDatabase().endTransaction();
        }
    }

    public static void delete(Class<?> cls) {
        delete(cls, null, null);
    }

    public static void delete(Class<?> cls, Object key, String column) {
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(getTableName(cls));
        String[] params = null;
        if (key != null || column != null) {
            sql.append(" WHERE ");
            sql.append(column).append(" = ").append("?");

            params = new String[]{key.toString()};
        }
        String sqlStr = sql.toString().replace("  ", " ");
        try {
            getSQLiteDatabase().beginTransaction();
            getSQLiteDatabase().execSQL(sqlStr, params);
            getSQLiteDatabase().setTransactionSuccessful();
        } finally {
            getSQLiteDatabase().endTransaction();
        }
    }

    public static List<Object> query(Class<?> cls) {
        return query(cls, null, null);
    }

    public static List<Object> query(Class<?> cls, Object key, String column) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(getTableName(cls));
        String[] params = null;
        if (key != null || column != null) {
            sql.append(" WHERE ");
            sql.append(column).append(" = ").append("?");

            params = new String[]{key.toString()};
        }
        String sqlStr = sql.toString().replace("  ", " ");
        Cursor cursor = getSQLiteDatabase().rawQuery(sqlStr, params);
        return cursorToList(cls, cursor);
    }

    private static List<Object> cursorToList(Class<?> cls, Cursor cursor) {
        List<Object> objects = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                Object object = cls.newInstance();
                List<ColumnInfo> columnInfos = getColumnInfo(object.getClass());
                for (ColumnInfo info : columnInfos) {
                    if (info.typeString.contains("int") || info.typeString.contains("Integer")) {
                        info.field.set(object, cursor.getInt(cursor.getColumnIndex(info.columnName)));
                    } else if (info.typeString.contains("long") || info.typeString.contains("Long")) {
                        info.field.set(object, cursor.getLong(cursor.getColumnIndex(info.columnName)));
                    } else if (info.typeString.contains("double") || info.typeString.contains("Double")) {
                        info.field.set(object, cursor.getDouble(cursor.getColumnIndex(info.columnName)));
                    } else if (info.typeString.contains("byte") || info.typeString.contains("Byte")) {
                        info.field.set(object, cursor.getBlob(cursor.getColumnIndex(info.columnName)));
                    } else {
                        info.field.set(object, cursor.getString(cursor.getColumnIndex(info.columnName)));
                    }
                    if (object instanceof OnDataBaseIdInterface) {
                        ((OnDataBaseIdInterface) object).setId(cursor.getLong(cursor.getColumnIndex("_id")));
                    }
                    objects.add(object);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return objects;
    }

    public static void update(Object object, Object key, String column) {
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(getTableName(object.getClass()));
        sql.append(" SET ");
        List<ColumnInfo> columnInfos = getColumnInfo(object.getClass());
        Object[] params = new Object[columnInfos.size() + 1];

        for (int i = 0; i < columnInfos.size(); i++) {
            sql.append(columnInfos.get(i).columnName).append(" = ? ");
            if (i != columnInfos.size() - 1) {
                sql.append(", ");
            }

            try {
                params[i] = columnInfos.get(i).field.get(object);   // 获取参数
            } catch (IllegalAccessException e) {
                params[i] = "";
            }
        }
        sql.append(" WHERE ");
        sql.append(column);
        sql.append(" = ? ");
        String sqlStr = sql.toString().replace("  ", " ");

        params[columnInfos.size()] = key;

        try {
            getSQLiteDatabase().beginTransaction();
            getSQLiteDatabase().execSQL(sqlStr, params);
            getSQLiteDatabase().setTransactionSuccessful();
        } finally {
            getSQLiteDatabase().endTransaction();
        }
    }

}
