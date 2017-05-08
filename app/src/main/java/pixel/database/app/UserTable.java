package pixel.database.app;

import java.util.List;

import pixel.database.library.MapField;
import pixel.database.library.OnDbIdCallback;

/**
 * Created by pixel on 2017/3/20.
 */

public class UserTable implements OnDbIdCallback {

    public Long $id;

    @MapField
    public String name;

//    public String username;

    @MapField
    public Integer age;

    public List<String> list;   // 不会被实例化到数据库   目前仅支持 Integer, Long, Double, Byte, String 类型持久化到数据库

    public UserTable() {
    }

    public UserTable(String name, Integer age) {
        this.name = name;
//        this.username = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "UserTable{" +
                "_id=" + $id +
                ", name='" + name + '\'' +
//                ", name='" + username + '\'' +
                ", age=" + age +
                ", list=" + list +
                '}';
    }

    @Override
    public void setId(Long _id) {
        this.$id = _id; // OnDbIdCallback接口方法,回传数据库ID值.
    }
}
