package pixel.database.app;

import java.util.List;

import pixel.database.library.OnDbIdCallback;

/**
 * Created by pixel on 2017/3/20.
 */

public class UserTable implements OnDbIdCallback {

    public Long _id;    // 下划线开头的的字段将不会实例化到数据库
    public String name;
    public Integer age;

    public List<String> list;   // 不会被实例化到数据库   目前仅支持 Integer, Long, Double, Byte, String 类型持久化到数据库

    public UserTable() {
    }

    public UserTable(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "UserTable{" +
                "_id=" + _id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", list=" + list +
                '}';
    }

    @Override
    public void setId(Long _id) {
        this._id = _id; // OnDbIdCallback接口方法,回传数据库ID值.
    }
}
