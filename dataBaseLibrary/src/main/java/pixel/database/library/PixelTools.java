package pixel.database.library;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by pixel on 2017/3/27.
 * <p>
 * SQLite操作方法
 */

public abstract class PixelTools extends PixelDao {

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
                Object[] params = new Object[columnMappingList.size() + 1];
                params[0] = cursor.getLong(cursor.getColumnIndex("_id"));
                for (int i = 0; i < columnMappingList.size(); i++) {
                    params[i + 1] = cursor.getString(cursor.getColumnIndex(columnMappingList.get(i).oldColumn));
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

                insertSql.append(" ) VALUES ( ?, ");
                for (int i = 0; i < columnMappingList.size(); i++) {
                    insertSql.append(" ? ");
                    if (i < columnMappingList.size() - 1) {
                        insertSql.append(", ");
                    }
                }
                insertSql.append(" ) ");
                getSQLiteDatabase().execSQL(insertSql.toString(), params);
            }

            deleteTable(tableNameTemp);

            getSQLiteDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("PixelTools", "更新表信息异常", e);
        } finally {
            getSQLiteDatabase().endTransaction();
            if (cursor != null) {
                cursor.close();
            }
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
            Log.e("PixelTools", "更新表信息异常", e);
        } finally {
            getSQLiteDatabase().endTransaction();
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
