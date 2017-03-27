package pixel.database.library;

/**
 * Created by pixel on 2017/3/27.
 */

public class ColumnMapping {

    public String oldColumn;
    public String newColumn;
//    public Object defValue;
//    public String valueType;

    public ColumnMapping() {
    }

    public ColumnMapping(String oldColumn, String newColumn) {
        this.oldColumn = oldColumn;
        this.newColumn = newColumn;
    }

    @Override
    public String toString() {
        return "ColumnMapping{" +
                "oldColumn='" + oldColumn + '\'' +
                ", newColumn='" + newColumn + '\'' +
                '}';
    }

}
