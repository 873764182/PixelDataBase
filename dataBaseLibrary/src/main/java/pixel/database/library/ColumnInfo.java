package pixel.database.library;

import java.lang.reflect.Field;

/**
 * Created by pixel on 2017/3/20.
 */

public class ColumnInfo {
    public String columnName;
    public String typeString;
    public Field field;

    public ColumnInfo(String columnName, String typeString, Field field) {
        this.columnName = columnName;
        this.typeString = typeString;
        this.field = field;
    }
}
