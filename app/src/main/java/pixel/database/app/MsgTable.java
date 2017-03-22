package pixel.database.app;

/**
 * Created by pixel on 2017/3/22.
 */

public class MsgTable {
    public Long _id;
    public Integer code;
    public String content;

    public MsgTable() {
    }

    public MsgTable(Long _id, Integer code, String content) {
        this._id = _id;
        this.code = code;
        this.content = content;
    }

    @Override
    public String toString() {
        return "MsgTable{" +
                "_id=" + _id +
                ", code=" + code +
                ", content='" + content + '\'' +
                '}';
    }
}
