package pixel.database.app;

import pixel.database.library.OnDataBaseIdInterface;

/**
 * Created by pixel on 2017/3/20.
 */

public class UserTable implements OnDataBaseIdInterface {
    public Long _id;    // _id是支持库的特殊字段
    public String name;
    public Integer age;

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
                '}';
    }

    @Override
    public void setId(Long _id) {
        this._id = _id;
    }
}
