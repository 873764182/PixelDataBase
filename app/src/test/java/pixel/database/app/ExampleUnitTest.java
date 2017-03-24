package pixel.database.app;

import android.database.Cursor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import pixel.database.library.ColumnInfo;
import pixel.database.library.OnDbIdCallback;
import pixel.database.library.PixelDao;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    /**
     * 查询表数据
     *
     * @param cls Java实体
     * @return 表数据
     */
    public static List<Object> query(Class<?> cls) {
        return query(cls, (Object) null, null);
    }

    /**
     * 查询表数据
     *
     * @param cls    Java实体
     * @param key    参数值
     * @param column 数据库列名
     * @return 表数据
     */
    public static List<Object> query(Class<?> cls, Object key, String column) {
        return query(cls, key != null ? new Object[]{key} : null, column != null ? new String[]{column} : null);
    }

    /**
     * 查询表数据
     *
     * @param cls     Java实体
     * @param keys    参数值集合
     * @param columns 数据库列名集合
     * @return 表数据
     */
    public static List<Object> query(Class<?> cls, Object[] keys, String[] columns) {
        return query(cls, keys, columns, false);
    }

    /**
     * 查询表数据
     *
     * @param cls     Java实体
     * @param keys    参数值集合
     * @param columns 数据库列名集合
     * @param or      是否是 或
     * @return 表数据
     */
    public static List<Object> query(Class<?> cls, Object[] keys, String[] columns, boolean or) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(PixelDao.getTableName(cls));
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
        Cursor cursor = PixelDao.getSQLiteDatabase().rawQuery(sqlStr, params);
        return cursorToList(cls, cursor);
    }

    /**
     * 数据库数据集与Java实体数据转化
     *
     * @param cls    Java实体
     * @param cursor 数据库数据集
     * @return Java数据实体
     */
    public static List<Object> cursorToList(Class<?> cls, Cursor cursor) {
        List<Object> objects = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                Object object = cls.newInstance();
                List<ColumnInfo> columnInfos = PixelDao.getColumnInfo(object.getClass());
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
                }
                objects.add(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return objects;
    }
}