package pixel.database.app;

import pixel.database.library.MapField;

/**
 * Created by pixel on 2017/3/22.
 */

public class MsgTable {
    public Long $id;

    @MapField
    public Integer code;

    @MapField
    public String content;

    public MsgTable() {
    }

    public MsgTable(Long $id, Integer code, String content) {
        this.$id = $id;
        this.code = code;
        this.content = content;
    }

    @Override
    public String toString() {
        return "MsgTable{" +
                "_id=" + $id +
                ", code=" + code +
                ", content='" + content + '\'' +
                '}';
    }
}
