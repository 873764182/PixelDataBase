package pixel.database.library;

/**
 * Created by pixel on 2017/3/27.
 */

public class TableInfo {

    public String cid;
    public String name;
    public String type;
    public String notnull;
    public String dflt_value;
    public String pk;

    public TableInfo() {
    }

    public TableInfo(String cid, String name, String type, String notnull, String dflt_value, String pk) {
        this.cid = cid;
        this.name = name;
        this.type = type;
        this.notnull = notnull;
        this.dflt_value = dflt_value;
        this.pk = pk;
    }

    @Override
    public String toString() {
        return "TableInfo{" +
                "cid='" + cid + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", notnull='" + notnull + '\'' +
                ", dflt_value='" + dflt_value + '\'' +
                ", pk='" + pk + '\'' +
                '}';
    }
}
