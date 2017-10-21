package pixel.database.library;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pixel on 2017/3/27.
 * <p>
 * SQLite SQL语句操作模版
 */

public abstract class SqlTemplate {

    /* 数据库对象 */
    private static SQLiteDatabase mSqLiteDatabase = null;
    /* 缓存变字典信息 */
    private static Map<String, List<ColumnInfo>> mColumnInfoMap = new Hashtable<>();

    /**
     * 初始化数据库且根据实体创建数据库表
     *
     * @param context 上下文
     * @param dbName  数据库名称
     * @param version 数据库版本
     * @param tables  表实体
     */
    public static void initDataBase(Context context, String dbName, int version, Class<?>... tables) {
        initDataBase(context, dbName, version, tables, null);
    }

    /**
     * @param context            上下文
     * @param dbName             数据库名称
     * @param version            数据库版本
     * @param onDbUpdateCallback 数据库版本更新监听
     * @param tables             表实体
     */
    public static void initDataBase(Context context, String dbName, int version, OnDbUpdateCallback onDbUpdateCallback, Class<?>... tables) {
        initDataBase(context, dbName, version, tables, onDbUpdateCallback);
    }

    /**
     * @param context            上下文
     * @param dbName             数据库名称
     * @param version            数据库版本
     * @param tables             表实体
     * @param onDbUpdateCallback 数据库版本更新监听
     */
    public synchronized static void initDataBase(Context context, String dbName, int version, Class<?>[] tables, OnDbUpdateCallback onDbUpdateCallback) {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(context, dbName, version, tables, onDbUpdateCallback);
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
        return SqlTemplate.mSqLiteDatabase;
    }

    /**
     * 设置数据库实例
     *
     * @param mSqLiteDatabase 数据库对象
     */
    public synchronized static void setSqLiteDatabase(SQLiteDatabase mSqLiteDatabase) {
        SqlTemplate.mSqLiteDatabase = mSqLiteDatabase;
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
        List<ColumnInfo> columnInfos = mColumnInfoMap.get(cls.getName());
        if (columnInfos != null) {
            return columnInfos;
        }
        columnInfos = new ArrayList<>();

        Field[] fields = cls.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                field.setAccessible(true); // 不过滤私有的属性
                String fieldType = field.getType().getName();
                // 支持Java的8大基本类型存储 boolean, char, byte、short、int、long, float、double
                if (!fieldType.contains("boolean") && !fieldType.contains("Boolean") &&
                        !fieldType.contains("char") && !fieldType.contains("String") && // 字符串也支持
                        !fieldType.contains("byte") && !fieldType.contains("Byte") &&
                        !fieldType.contains("short") && !fieldType.contains("Short") &&
                        !fieldType.contains("int") && !fieldType.contains("Integer") &&
                        !fieldType.contains("long") && !fieldType.contains("Long") &&
                        !fieldType.contains("float") && !fieldType.contains("Float") &&
                        !fieldType.contains("double") && !fieldType.contains("Double")) {
                    continue;   // 过滤掉不支持的类型
                }
                // TODO 暂时不支持数组类型 如果有数组请暂时转为String存储
                if (fieldType.contains("[") || fieldType.contains("]")) {
                    continue;
                }
                // 是否存需要映射到数据库
                if (field.isAnnotationPresent(TableColumn.class)) {
                    TableColumn tableColumn = field.getAnnotation(TableColumn.class);
                    if (tableColumn != null && tableColumn.enable()) {
                        columnInfos.add(new ColumnInfo(field.getName(), field.getType().getName(), field));
                    }
                }
            }
        }
        mColumnInfoMap.put(cls.getName(), columnInfos);
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
            if (params == null || params.length <= 0) { // 避免出现  java.lang.IllegalArgumentException: Empty bindArgs 异常
                getSQLiteDatabase().execSQL(sql);
            } else {
                getSQLiteDatabase().execSQL(sql, params);
            }
            getSQLiteDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("SqlTemplate", "执行SQL语句异常", e);
        } finally {
            getSQLiteDatabase().endTransaction();
        }
    }

    /**
     * 直接执行查询语句
     *
     * @param sql           查询语句
     * @param selectionArgs 查询参数
     * @return Cursor
     */
    public static Cursor rawQuery(String sql, String[] selectionArgs) {
        return getSQLiteDatabase().rawQuery(sql, selectionArgs);
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

            Field field = columnInfos.get(i).field;
            TableColumn tableColumn = field.getAnnotation(TableColumn.class);

            try {
                Object obj = field.get(object); // 获取参数
                if (obj != null) {
                    String parStr = obj.toString();
                    if (parStr.length() > tableColumn.maxLength()) {
                        parStr = parStr.substring(0, tableColumn.maxLength());  // 超出限定长度时则裁剪掉超出部分
                    }
                    params[i] = parStr;
                } else {
                    params[i] = null;
                }
            } catch (Exception e) {
                params[i] = null;
            } finally {
                if (params[i] == null) {   // 要是字段未赋值则获取默认值
                    params[i] = tableColumn.defValue();
                }
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
                        byte[] byteValue = cursor.getBlob(cursor.getColumnIndex(info.columnName));
                        if (byteValue != null && byteValue.length > 0) {
                            info.field.set(object, byteValue[0]);   // TODO 需要优化 支持byte[]
                        }
                    } else if (info.typeString.contains("float") || info.typeString.contains("Float")) {
                        info.field.set(object, cursor.getFloat(cursor.getColumnIndex(info.columnName)));
                    } else if (info.typeString.contains("short") || info.typeString.contains("Short")) {
                        info.field.set(object, cursor.getShort(cursor.getColumnIndex(info.columnName)));
                    } else if (info.typeString.contains("boolean") || info.typeString.contains("Boolean")) {
                        try {
                            info.field.set(object, Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(info.columnName))));
                        } catch (Exception e) {
                            Log.e("SqlTemplate", "数据库存储'boolean'值得字段不正确,结果应该只为: true, false", e);
                        }
                    } else if (info.typeString.contains("char")) {
                        String stringValue = cursor.getString(cursor.getColumnIndex(info.columnName));
                        if (stringValue != null && stringValue.length() > 0) {
                            info.field.set(object, stringValue.charAt(0));  // char[] strChar = stringValue.toCharArray();
                        }
                    } else {    // 默认String类型
                        info.field.set(object, cursor.getString(cursor.getColumnIndex(info.columnName)));
                    }
                }
                // 获取数据库自增长字段
                if (object instanceof OnDbIdCallback) {
                    ((OnDbIdCallback) object).setId(cursor.getLong(cursor.getColumnIndex("_id")));
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

    /**
     * 获取数据库表有多少行数据
     *
     * @param table 表对象
     * @return 行数
     */
    public static long getTableRowCount(Class<?> table) {
        return getTableRowCount(table, null, null);
    }

    /**
     * 获取对应的表中有多少行数据 带条件的查询
     *
     * @param table  表对象
     * @param column 列名
     * @param value  条件
     * @return 行数
     */
    public static long getTableRowCount(Class<?> table, String column, Object value) {
        long count = 0L;
        Cursor cursor = null;
        try {
            getSQLiteDatabase().beginTransaction();

            String[] parameter = null;
            StringBuilder sql = new StringBuilder("SELECT count(*) FROM ");
            sql.append(getTableName(table));
            if (column != null && value != null) {
                sql.append(" WHERE ( ");
                sql.append(column).append(" = ").append(" ? ");
                sql.append(" ) ");

                parameter = new String[]{value.toString()};
            }
            String sqlStr = sql.toString().replace("  ", " ");
            cursor = getSQLiteDatabase().rawQuery(sqlStr, parameter);
            if (cursor != null && cursor.moveToNext()) {
                count = cursor.getLong(0);
            }

            getSQLiteDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            getSQLiteDatabase().endTransaction();
        }
        return count;
    }

    // ============================================================================================= 华丽的分割线

    /**
     * 创建一张表,默认会创建一个INTEGER类型的_id字段为主键
     *
     * @param tableName 表名
     * @param columns   列名
     */
    public static void createTable(String tableName, String... columns) {
        StringBuilder sql = new StringBuilder(" CREATE TABLE ");
        sql.append(tableName);
        sql.append(" ( _id INTEGER PRIMARY KEY AUTOINCREMENT ").append(" , ");
        for (int i = 0; i < columns.length; i++) {
            sql.append(columns[i]);
            if (i < columns.length - 1) {
                sql.append(" , ");
            }
        }
        sql.append(" ) ");
        getSQLiteDatabase().execSQL(sql.toString().replace("  ", " "));
    }

    /**
     * 删除一张表
     *
     * @param tableName 表名
     */
    public static void deleteTable(String tableName) {
        getSQLiteDatabase().execSQL(" DROP TABLE " + tableName);
    }

    /**
     * 数据库表改名
     *
     * @param oldName 旧名称
     * @param newName 新名称
     */
    public static void tableRename(String oldName, String newName) {
        getSQLiteDatabase().execSQL(" ALTER TABLE " + oldName + " RENAME TO " + newName);
    }

    /**
     * 为表添加一列,只能添加在列的末尾.
     *
     * @param tableName 表名
     * @param column    列名
     */
    public static void addTableColumn(String tableName, Object column) {
        getSQLiteDatabase().execSQL(" ALTER TABLE " + tableName + " ADD COLUMN " + column + " TEXT ");    // 只能在表的末尾添加字段
    }

    /**
     * 获取表的信息
     *
     * @param tableName 表名
     * @return 表每一列的信息
     */
    public static List<TableInfo> getTableInfo(String tableName) {
        List<TableInfo> tableInfoList = new ArrayList<>();
        Cursor cursor = getSQLiteDatabase().rawQuery(" PRAGMA table_info( " + tableName + " ) ", null);
        while (cursor.moveToNext()) {
            TableInfo tableInfo = new TableInfo();
            tableInfo.cid = cursor.getString(cursor.getColumnIndex("cid"));
            tableInfo.name = cursor.getString(cursor.getColumnIndex("name"));
            tableInfo.type = cursor.getString(cursor.getColumnIndex("type"));
            tableInfo.notnull = cursor.getString(cursor.getColumnIndex("notnull"));
            tableInfo.dflt_value = cursor.getString(cursor.getColumnIndex("dflt_value"));
            tableInfo.pk = cursor.getString(cursor.getColumnIndex("pk"));
            tableInfoList.add(tableInfo);
        }
        cursor.close();
        return tableInfoList;
    }

    /**
     * 复制一个表的数据到另一个表,要求原表与新表字段完全一样.
     * INSERT INTO Subscription SELECT OrderId, "", ProductId FROM __temp__Subscription;
     * 　　或者
     * INSERT INTO Subscription() SELECT OrderId, "", ProductId FROM __temp__Subscription;
     * 　　* 注意 双引号"" 是用来补充原来不存在的数据的
     *
     * @param oldTableName 旧表
     * @param newTableName 新表
     */
    public static void copyTableData(String oldTableName, String newTableName) {
        getSQLiteDatabase().execSQL("INSERT INTO " + newTableName + " SELECT * FROM " + oldTableName);
    }

    /**
     * 更新表信息
     *
     * @param table             表对应的实体
     * @param columnMappingList 表的列的对应关系
     */
    public static void updateTable(Class<?> table, List<ColumnMapping> columnMappingList) {
        Cursor cursor = null;
        try {
            getSQLiteDatabase().beginTransaction();
            // 获取对应表名
            String tableName = getTableName(table);
            // 获取临时表名
            String tableNameTemp = tableName + "_temp";
            // 将即将修改的表修改名称
            tableRename(tableName, tableNameTemp);
            // 创建新表
            createTable(table);
            // 判断是否需要还原旧的数据库到新表中
            if (columnMappingList == null || columnMappingList.size() <= 0) {
                deleteTable(tableNameTemp);
                getSQLiteDatabase().setTransactionSuccessful();
                return;
            }
            // 开始数据还原操作
            cursor = getSQLiteDatabase().rawQuery("SELECT * FROM " + tableNameTemp, null);
            while (cursor.moveToNext()) {
                Object[] params = new Object[columnMappingList.size() + 1]; // 加一,是因为数据库的_id字段是系统自动生成的,非用户对象指定生成.
                params[0] = cursor.getLong(cursor.getColumnIndex("_id"));
                for (int i = 0; i < columnMappingList.size(); i++) {
                    params[i + 1] = cursor.getString(cursor.getColumnIndex(columnMappingList.get(i).oldColumn));
                }
                // 拼接字段
                StringBuilder insertSql = new StringBuilder("INSERT INTO ");
                insertSql.append(tableName);
                insertSql.append(" ( _id, ");
                for (int i = 0; i < columnMappingList.size(); i++) {
                    insertSql.append(columnMappingList.get(i).newColumn);
                    if (i < columnMappingList.size() - 1) {
                        insertSql.append(", ");
                    }
                }
                // 拼接参数
                insertSql.append(" ) VALUES ( ?, ");
                for (int i = 0; i < columnMappingList.size(); i++) {
                    insertSql.append(" ? ");
                    if (i < columnMappingList.size() - 1) {
                        insertSql.append(", ");
                    }
                }
                insertSql.append(" ) ");
                // 插入数据到新表
                getSQLiteDatabase().execSQL(insertSql.toString(), params);
            }
            // 删除旧的表
            deleteTable(tableNameTemp);

            getSQLiteDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("SqlTemplate", "更新表信息异常", e);
        } finally {
            getSQLiteDatabase().endTransaction();
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 判断表是否存在
     */
    public static boolean existTable(Class<?> table) {
        String sql = " SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name = ? ";  // '" + getTableName(table) + "'
        Cursor cursor = rawQuery(sql, new String[]{getTableName(table)});
        if (cursor.moveToNext()) {
            int count = cursor.getInt(0);
            if (count > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 创建或者更新表
     */
    public static void updateOrCreateTable(Class<?> table, List<ColumnMapping> columnMappingList) {
        if (existTable(table)) {
            updateTable(table, columnMappingList);
        } else {
            createTable(table);
        }
    }

    /**
     * 更新表信息
     * 未做参数化查询 参数有特殊字符会出现异常
     *
     * @param table             表对应的实体
     * @param columnMappingList 表的列的对应关系
     */
    public static void updateTableNoFormatParam(Class<?> table, List<ColumnMapping> columnMappingList) {
        Cursor cursor = null;
        try {
            getSQLiteDatabase().beginTransaction();

            String tableName = getTableName(table);
            String tableNameTemp = tableName + "_temp";
            tableRename(tableName, tableNameTemp);
            createTable(table);

            if (columnMappingList == null || columnMappingList.size() <= 0) {
                deleteTable(tableNameTemp);
                getSQLiteDatabase().setTransactionSuccessful();
                return;
            }

            cursor = getSQLiteDatabase().rawQuery("SELECT * FROM " + tableNameTemp, null);
            while (cursor.moveToNext()) {
                LinkedHashMap<String, Object> valueMapping = new LinkedHashMap<>();

                valueMapping.put("_id", cursor.getLong(cursor.getColumnIndex("_id")));

                for (ColumnMapping columnMapping : columnMappingList) {
                    valueMapping.put(columnMapping.newColumn, cursor.getString(cursor.getColumnIndex(columnMapping.oldColumn)));
                }

                StringBuilder insertSql = new StringBuilder("INSERT INTO ");
                insertSql.append(tableName);
                insertSql.append(" ( _id, ");

                for (int i = 0; i < columnMappingList.size(); i++) {
                    insertSql.append(columnMappingList.get(i).newColumn);
                    if (i < columnMappingList.size() - 1) {
                        insertSql.append(", ");
                    }
                }

                insertSql.append(" ) VALUES ( " + valueMapping.get("_id") + ", ");
                for (int i = 0; i < columnMappingList.size(); i++) {
                    Object value = valueMapping.get(columnMappingList.get(i).newColumn);
                    if (value != null) {
                        insertSql.append(" '").append(valueMapping.get(columnMappingList.get(i).newColumn)).append("' ");
                    } else {
                        insertSql.append(" '' ");
                    }
                    if (i < columnMappingList.size() - 1) {
                        insertSql.append(", ");
                    }
                }
                insertSql.append(" ) ");
                getSQLiteDatabase().execSQL(insertSql.toString());
            }

            deleteTable(tableNameTemp);

            getSQLiteDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("SqlTemplate", "更新表信息异常", e);
        } finally {
            getSQLiteDatabase().endTransaction();
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 获取数据库版本号
     *
     * @return 版本号
     */
    public static int getDbVersion() {
        return getSQLiteDatabase().getVersion();
    }
}
