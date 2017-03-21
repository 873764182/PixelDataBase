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

public class PixelDao {
    protected volatile static SQLiteOpenHelper mSqLiteOpenHelper = null;
    protected volatile static SQLiteDatabase mSqLiteDatabase = null;

    public static void initDataBase(Context context, String name, int version, Class<?>... tables) {
        initDataBase(context, name, version, null, tables);
    }

    public synchronized static void initDataBase(Context context, String name, int version, OnDbUpdateCallback onDbUpdateCallback, Class<?>... tables) {
        mSqLiteOpenHelper = new DataBaseHelper(context, name, version);
        for (Class<?> cls : tables) {
            createTable(getTableName(cls), getColumnInfo(cls));
        }
        // 检测版本
        int localVersion = ConfigUtil.getInt(context, "v_" + name);
        if (localVersion < version) {
            if (onDbUpdateCallback != null) {
                onDbUpdateCallback.onUpgrade(getSQLiteDatabase(), localVersion, version);
            }
            ConfigUtil.saveInt(context, "v_" + name, version);
        }
    }

    public synchronized static SQLiteDatabase getSQLiteDatabase() {
        if (mSqLiteOpenHelper == null) {
            throw new NullPointerException("请先调用 initDataBase() 初始化数据库");
        }
        if (mSqLiteDatabase == null) {
            mSqLiteDatabase = mSqLiteOpenHelper.getWritableDatabase();
        }
        return mSqLiteDatabase;
    }

    private static String getTableName(Class<?> cls) {
        return cls.getName().replace(".", "_"); // 数据库表名为对象全路径
    }

    private static List<ColumnInfo> getColumnInfo(Class<?> cls) {
        List<ColumnInfo> columnInfos = new ArrayList<>();
        Field[] fields = cls.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                // CREATE TABLE IF NOT EXISTS pixel_database_app_UserTable ( _id INTEGER PRIMARY KEY AUTOINCREMENT, age TEXT, name TEXT, $change TEXT, serialVersionUID TEXT )
                if (field.getName().contains("$change") || field.getName().contains("serialVersionUID")
                        || field.getName().startsWith("_")) {   // 所有下划线开头的属性都不实例化到数据库
                    continue;
                }
                field.setAccessible(true); // field.isAccessible() 过滤私有的属性
                columnInfos.add(new ColumnInfo(field.getName(), field.getType().getName(), field));
            }
        }
        return columnInfos;
    }

    private static void execSQL(String sql, Object[] params) {
        try {
            getSQLiteDatabase().beginTransaction();
            getSQLiteDatabase().execSQL(sql, params);
            getSQLiteDatabase().setTransactionSuccessful();
        } finally {
            getSQLiteDatabase().endTransaction();
        }
    }

    public static void createTable(String tableName, List<ColumnInfo> columnInfos) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append(tableName).append(" ( _id INTEGER PRIMARY KEY AUTOINCREMENT, ");
        for (ColumnInfo info : columnInfos) {
            sql.append(info.columnName).append(" ").append("TEXT").append(", ");    // info.typeString
        }
        sql.append(" ) ");
        String sqlStr = sql.toString().replace("  ", " ").replace(", )", " )");  // 去掉多余字符
        getSQLiteDatabase().execSQL(sqlStr);
    }

    public static void deleteTable(String tableName) {
        getSQLiteDatabase().execSQL("DROP TABLE " + tableName);
    }

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
        execSQL(sqlStr, params);
    }

    public static void delete(Class<?> cls) {
        delete(cls, (Object) null, null);
    }

    public static void delete(Class<?> cls, Object key, String column) {
        delete(cls, key != null ? new Object[]{key} : null, column != null ? new String[]{column} : null);
    }

    public static void delete(Class<?> cls, Object[] keys, String[] columns) {
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(getTableName(cls));
        String[] params = null;
        if (keys != null && columns != null) {
            params = new String[columns.length];
            if (keys.length != columns.length) {
                throw new NullPointerException("参数与数据库列长度不一致");
            }
            sql.append(" WHERE ( ");
            for (int i = 0; i < columns.length; i++) {
                sql.append(columns[i]).append(" = ").append(" ? ");
                if (i != columns.length - 1) {
                    sql.append(" AND ");
                }
                params[i] = keys[i].toString();
            }
            sql.append(" ) ");
        }
        String sqlStr = sql.toString().replace("  ", " ");
        execSQL(sqlStr, params);
    }

    public static List<Object> query(Class<?> cls) {
        return query(cls, (Object) null, null);
    }

    public static List<Object> query(Class<?> cls, Object key, String column) {
        return query(cls, key != null ? new Object[]{key} : null, column != null ? new String[]{column} : null);
    }

    public static List<Object> query(Class<?> cls, Object[] keys, String[] columns) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(getTableName(cls));
        String[] params = null;
        if (keys != null && columns != null) {
            params = new String[columns.length];
            if (keys.length != columns.length) {
                throw new NullPointerException("参数与数据库列长度不一致");
            }
            sql.append(" WHERE ( ");
            for (int i = 0; i < columns.length; i++) {
                sql.append(columns[i]).append(" = ").append(" ? ");
                if (i != columns.length - 1) {
                    sql.append(" AND ");
                }
                params[i] = keys[i].toString();
            }
            sql.append(" ) ");
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
                    if (object instanceof OnDbIdCallback) {
                        ((OnDbIdCallback) object).setId(cursor.getLong(cursor.getColumnIndex("_id")));
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
        update(object, new Object[]{key}, new String[]{column});
    }

    public static void update(Object object, Object[] keys, String[] columns) {
        if (keys == null || columns == null) {
            throw new NullPointerException("更新的参数与数据库列不能为空");
        }
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(getTableName(object.getClass()));
        sql.append(" SET ");
        List<ColumnInfo> columnInfos = getColumnInfo(object.getClass());
        Object[] params = new Object[columnInfos.size() + columns.length];

        for (int i = 0; i < columnInfos.size(); i++) {
            sql.append(columnInfos.get(i).columnName).append(" = ? ");
            if (i != columnInfos.size() - 1) {
                sql.append(", ");
            }

            try {
                params[i] = columnInfos.get(i).field.get(object);   // 获取参数
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                params[i] = "";
            }
        }
        sql.append(" WHERE ( ");
        for (int i = 0; i < columns.length; i++) {
            sql.append(columns[i]).append(" = ? ");
            if (i != columns.length - 1) {
                sql.append(" AND ");
            }

            params[columnInfos.size() + i] = keys[i];
        }
        sql.append(" ) ");
        String sqlStr = sql.toString().replace("  ", " ");
        execSQL(sqlStr, params);
    }

}
