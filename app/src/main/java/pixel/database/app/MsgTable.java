package pixel.database.app;

/**
 * Created by pixel on 2017/3/22.
 */

public class MsgTable {
    public Long $id;    // 美元符号开头的属性将不会被实例化到数据库
    public Integer code;
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
