package pixel.database.library;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pixel on 2017/3/20.
 * <p>
 * SQLite支持的数据类型: NULL(空)、INTEGER(整数)、REAL（浮点数字）、TEXT(字符串文本)和BLOB(二进制对象)
 * <p>
 * 当前库支持 Integer, Long, Double, Byte, String 类型持久化到数据库
 */

public abstract class PixelDao {
    private volatile static SQLiteDatabase mSqLiteDatabase = null;

    /**
     * 初始化数据库且根据实体创建数据库表
     *
     * @param context 上下文
     * @param dbName  数据库名称
     * @param version 数据库版本
     * @param tables  表实体
     */
    public static void initDataBase(Context context, String dbName, int version, Class<?>... tables) {
        initDataBase(context, dbName, version, null, tables);
    }

    /**
     * @param context            上下文
     * @param dbName             数据库名称
     * @param version            数据库版本
     * @param onDbUpdateCallback 数据库版本更新监听
     * @param tables             表实体
     */
    public synchronized static void initDataBase(Context context, String dbName, int version, OnDbUpdateCallback onDbUpdateCallback, Class<?>... tables) {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(context, dbName, version, onDbUpdateCallback, tables);
        if (mSqLiteDatabase == null) {
            mSqLiteDatabase = dataBaseHelper.getWritableDatabase();
        }
    }

    /**
     * 获取数据库实例
     *
     * @return SQLiteDatabase
     */
    public synchronized static SQLiteDatabase getSQLiteDatabase() {
        return PixelDao.mSqLiteDatabase;
    }

    public synchronized static void setSqLiteDatabase(SQLiteDatabase mSqLiteDatabase) {
        PixelDao.mSqLiteDatabase = mSqLiteDatabase;
    }

    /**
     * 根据实体获取数据库对应的表名
     *
     * @param cls Java实体
     * @return 数据库表名
     */
    public static String getTableName(Class<?> cls) {
        return cls.getName().replace(".", "_").replace(" ", ""); // 数据库表名为对象全路径
    }

    /**
     * 获取Java实体属性信息
     *
     * @param cls Java实体
     * @return 属性描述信息
     */
    public static List<ColumnInfo> getColumnInfo(Class<?> cls) {
        List<ColumnInfo> columnInfos = new ArrayList<>();
        Field[] fields = cls.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                String fieldName = field.getName();
                if (fieldName.contains("$change") || fieldName.contains("serialVersionUID") || fieldName.startsWith("_")) {
                    continue;   // 过滤掉特殊字符 所有下划线开头的属性都不实例化到数据库
                }
                String fieldType = field.getType().getName();
                if (!fieldType.contains("int") && !fieldType.contains("Integer") &&
                        !fieldType.contains("long") && !fieldType.contains("Long") &&
                        !fieldType.contains("double") && !fieldType.contains("Double") &&
                        !fieldType.contains("byte") && !fieldType.contains("Byte") &&
                        !fieldType.contains("char") && !fieldType.contains("String")) {
                    continue;   // 过滤掉不支持的类型
                }
                field.setAccessible(true); // 不过滤私有的属性
                columnInfos.add(new ColumnInfo(field.getName(), field.getType().getName(), field));
            }
        }
        return columnInfos;
    }

    /**
     * 直接执行SQL语句到数据库
     *
     * @param sql    SQL语句
     * @param params 参数集合
     */
    public static void execSQL(String sql, Object[] params) {
        try {
            getSQLiteDatabase().beginTransaction();
            getSQLiteDatabase().execSQL(sql, params);
            getSQLiteDatabase().setTransactionSuccessful();
        } finally {
            getSQLiteDatabase().endTransaction();
        }
    }

    /**
     * 根据Java创建数据库表
     *
     * @param tables Java实体
     */
    public static void createTable(Class<?>... tables) {
        for (Class<?> cls : tables) {
            createTable(getTableName(cls), getColumnInfo(cls));
        }
    }

    /**
     * 根据表名与实体属性描述信息创建数据库表
     *
     * @param tableName   数据库表名
     * @param columnInfos Java实体属性描述信息
     */
    public static void createTable(String tableName, List<ColumnInfo> columnInfos) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append(tableName).append(" ( _id INTEGER PRIMARY KEY AUTOINCREMENT, ");
        for (ColumnInfo info : columnInfos) {
            sql.append(info.columnName).append(" TEXT ").append(", ");    // info.typeString
        }
        sql.append(" ) ");
        String sqlStr = sql.toString().replace("  ", " ").replace(", )", " )");  // 去掉多余字符
        getSQLiteDatabase().execSQL(sqlStr);
    }

    /**
     * 删除数据库表
     *
     * @param clss Java实体
     */
    public static void deleteTable(Class<?>... clss) {
        for (Class<?> cls : clss) {
            getSQLiteDatabase().execSQL("DROP TABLE " + getTableName(cls));
        }
    }

    /**
     * 插入一行数据
     *
     * @param object 数据实体
     */
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

    /**
     * 插入数据列表
     *
     * @param objects 数据实体列表
     */
    public static void insert(List<Object> objects) {
        for (Object object : objects) {
            insert(object);
        }
    }

    /**
     * 删除表数据
     *
     * @param cls Java实体
     */
    public static void delete(Class<?> cls) {
        delete(cls, (Object) null, null);
    }

    /**
     * 删除表数据
     *
     * @param cls    Java实体
     * @param key    参数值
     * @param column 数据库列名
     */
    public static void delete(Class<?> cls, Object key, String column) {
        delete(cls, key != null ? new Object[]{key} : null, column != null ? new String[]{column} : null);
    }

    /**
     * 删除表数据
     *
     * @param cls     Java实体
     * @param keys    参数值集合
     * @param columns 数据库列名集合
     */
    public static void delete(Class<?> cls, Object[] keys, String[] columns) {
        delete(cls, keys, columns, false);
    }

    /**
     * 删除表数据
     *
     * @param cls     Java实体
     * @param keys    参数值集合
     * @param columns 数据库列名集合
     * @param or      是否是 或
     */
    public static void delete(Class<?> cls, Object[] keys, String[] columns, boolean or) {
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(getTableName(cls));
        String[] params = null;
        if (keys != null && columns != null) {
            params = new String[columns.length];
            if (keys.length != columns.length) {
                throw new IllegalArgumentException("参数与数据库列长度不一致");
            }
            sql.append(" WHERE ( ");
            for (int i = 0; i < columns.length; i++) {
                sql.append(columns[i]).append(" = ").append(" ? ");
                if (i != columns.length - 1) {
                    if (or) {
                        sql.append(" OR ");
                    } else {
                        sql.append(" AND ");
                    }
                }
                params[i] = keys[i].toString();
            }
            sql.append(" ) ");
        }
        String sqlStr = sql.toString().replace("  ", " ");
        execSQL(sqlStr, params);
    }

    /**
     * 更新表数据
     *
     * @param object Java数据实体
     * @param key    参数值
     * @param column 数据库列名
     */
    public static void update(Object object, Object key, String column) {
        update(object, new Object[]{key}, new String[]{column});
    }

    /**
     * 更新表数据
     *
     * @param object  Java数据实体
     * @param keys    参数值集合
     * @param columns 数据库列名集合
     */
    public static void update(Object object, Object[] keys, String[] columns) {
        update(object, keys, columns, false);
    }

    /**
     * 更新表数据
     *
     * @param object  Java数据实体
     * @param keys    参数值集合
     * @param columns 数据库列名集合
     * @param or      是否是 或
     */
    public static void update(Object object, Object[] keys, String[] columns, boolean or) {
        if (keys == null || columns == null) {
            throw new IllegalArgumentException("更新的参数与数据库列不能为空");
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
                if (or) {
                    sql.append(" OR ");
                } else {
                    sql.append(" AND ");
                }
            }

            params[columnInfos.size() + i] = keys[i];
        }
        sql.append(" ) ");
        String sqlStr = sql.toString().replace("  ", " ");
        execSQL(sqlStr, params);
    }


    /**
     * 查询表数据
     *
     * @param cls Java实体
     * @return 表数据
     */
    public static <T extends Object> List<T> query(Class<T> cls) {
        return query(cls, null, null);
    }

    /**
     * 查询表数据
     *
     * @param cls  Java实体
     * @param size 页条数
     * @param page 页数(从0开始)
     * @return 表数据
     */
    public static <T extends Object> List<T> query(Class<T> cls, long size, long page) {
        return query(cls, (Object) null, null, size, page);
    }

    /**
     * 查询表数据
     *
     * @param cls    Java实体
     * @param key    参数
     * @param column 列名
     * @return 表数据
     */
    public static <T extends Object> List<T> query(Class<T> cls, Object key, String column) {
        return query(cls, key, column, -1, -1);
    }

    /**
     * 查询表数据
     *
     * @param cls    Java实体
     * @param key    参数值
     * @param column 数据库列名
     * @param size   页条数
     * @param page   页数
     * @return 表数据
     */
    public static <T extends Object> List<T> query(Class<T> cls, Object key, String column, long size, long page) {
        return query(cls, key != null ? new Object[]{key} : null, column != null ? new String[]{column} : null, size, page);
    }

    /**
     * 查询表数据
     *
     * @param cls     Java实体
     * @param keys    参数值集合
     * @param columns 数据库列名集合
     * @param size    页条数
     * @param page    页数(从0开始)
     * @return 表数据
     */
    public static <T extends Object> List<T> query(Class<T> cls, Object[] keys, String[] columns, long size, long page) {
        return query(cls, keys, columns, false, size, page);
    }

    /**
     * 查询表数据
     *
     * @param cls     Java实体
     * @param keys    参数值集合
     * @param columns 数据库列名集合
     * @param size    页条数
     * @param page    页数(从0开始)
     * @param or      是否是 或
     * @return 表数据
     */
    public static <T extends Object> List<T> query(
            Class<T> cls, Object[] keys, String[] columns, boolean or, long size, long page) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(getTableName(cls));
        String[] params = null;
        if (keys != null && columns != null) {
            params = new String[columns.length];
            if (keys.length != columns.length) {
                throw new IllegalArgumentException("参数与数据库列长度不一致");
            }
            sql.append(" WHERE ( ");
            for (int i = 0; i < columns.length; i++) {
                sql.append(columns[i]).append(" = ").append(" ? ");
                if (i != columns.length - 1) {
                    if (or) {
                        sql.append(" OR ");
                    } else {
                        sql.append(" AND ");
                    }
                }
                params[i] = keys[i].toString();
            }
            sql.append(" ) ");
        }
        if (size >= 0 && page >= 0) {
            sql.append(" LIMIT ").append(size).append(" OFFSET ").append(page * size);
        }
        String sqlStr = sql.toString().replace("  ", " ");
        Cursor cursor = getSQLiteDatabase().rawQuery(sqlStr, params);
        return cursorToList(cls, cursor);
    }

    /**
     * 根据列模糊查询
     *
     * @param cls    Java实体
     * @param key    参数值
     * @param column 数据库列名
     * @param <T>    数据库模型
     * @return 查询结果
     */
    public static <T extends Object> List<T> search(Class<T> cls, Object key, String column) {
        return search(cls, key, column, null, -1);
    }

    /**
     * 根据列模糊查询
     *
     * @param cls        Java实体
     * @param key        参数值
     * @param column     数据库列名
     * @param resultSize 返回最大条数
     * @param <T>        数据库模型
     * @return 查询结果
     */
    public static <T extends Object> List<T> search(Class<T> cls, Object key, String column, long resultSize) {
        return search(cls, key, column, null, resultSize);
    }

    /**
     * 根据列模糊查询
     *
     * @param cls         Java实体
     * @param key         参数值
     * @param column      数据库列名
     * @param sortColumns 排序字段
     * @param <T>         数据库模型
     * @return 查询结果
     */
    public static <T extends Object> List<T> search(Class<T> cls, Object key, String column, String sortColumns) {
        return search(cls, key, column, sortColumns, -1);
    }

    /**
     * 根据列模糊查询
     *
     * @param cls         Java实体
     * @param key         参数值
     * @param column      数据库列名
     * @param sortColumns 排序字段
     * @param resultSize  返回最大条数
     * @param <T>         数据库模型
     * @return 查询结果
     */
    public static <T extends Object> List<T> search(Class<T> cls, Object key, String column, String sortColumns, long resultSize) {
        return querySupport(cls, new Object[]{key}, new String[]{column}, false, true, sortColumns, false, resultSize, resultSize == -1 ? -1 : 0);
    }

    /**
     * 查询表数据
     *
     * @param cls         Java实体
     * @param keys        参数值集合
     * @param columns     数据库列名集合
     * @param isOr        是否是 或
     * @param isFuzzy     是否模糊查询 默认否
     * @param sortColumns 排序列
     * @param isDesc      是否倒叙 默认否
     * @param size        页条数(-1不分页)
     * @param page        页数(从0开始 -1不分页)
     * @param <T>         数据库模型
     * @return 表数据
     */
    public static <T extends Object> List<T> querySupport(
            Class<T> cls, Object[] keys, String[] columns, boolean isOr, boolean isFuzzy, String sortColumns, boolean isDesc, long size, long page) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(getTableName(cls));
        String[] params = null;
        if (keys != null && columns != null) {
            params = new String[columns.length];
            if (keys.length != columns.length) {
                throw new IllegalArgumentException("参数与数据库列长度不一致");
            }
            sql.append(" WHERE ( ");
            for (int i = 0; i < columns.length; i++) {
                if (isFuzzy) {
                    sql.append(columns[i]).append(" LIKE ").append(" ? ");
                } else {
                    sql.append(columns[i]).append(" = ").append(" ? ");
                }
                if (i != columns.length - 1) {
                    if (isOr) {
                        sql.append(" OR ");
                    } else {
                        sql.append(" AND ");
                    }
                }
                params[i] = "%" + keys[i].toString() + "%";
            }
            sql.append(" ) ");
        }
        if (sortColumns != null) {
            sql.append(" ORDER BY ").append(sortColumns);
            if (isDesc) {
                sql.append(" DESC ");
            } else {
                sql.append(" ASC ");
            }
        }
        if (size >= 0 && page >= 0) {
            sql.append(" LIMIT ").append(size).append(" OFFSET ").append(page * size);
        }
        String sqlStr = sql.toString().replace("  ", " ");
        Cursor cursor = getSQLiteDatabase().rawQuery(sqlStr, params);
        return cursorToList(cls, cursor);
    }

    /**
     * 数据库数据集与Java实体数据转化
     *
     * @param cls    Java实体
     * @param cursor 数据库数据集
     * @return Java数据实体
     */
    public static <T extends Object> List<T> cursorToList(Class<T> cls, Cursor cursor) {
        List<T> objects = new ArrayList<>();
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
                    } else {    // 默认 String 类型
                        info.field.set(object, cursor.getString(cursor.getColumnIndex(info.columnName)));
                    }
                    if (object instanceof OnDbIdCallback) {
                        ((OnDbIdCallback) object).setId(cursor.getLong(cursor.getColumnIndex("_id")));
                    }
                }
                objects.add((T) object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return objects;
    }

}
