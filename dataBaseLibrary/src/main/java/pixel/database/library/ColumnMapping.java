package pixel.database.library;

/**
 * Created by pixel on 2017/3/27.
 * <p>
 * 数据库表被更新时需要还原的字段数据对应关系
 */

public class ColumnMapping {

    // 旧的数据库字段名
    public String oldColumn;
    // 对应的新的数据库字段名
    public String newColumn;
    // 默认值
//    public Object defValue;
    // 值类型
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
