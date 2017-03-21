package pixel.database.app;

import pixel.database.library.OnDbIdCallback;

/**
 * Created by pixel on 2017/3/20.
 */

public class UserTable implements OnDbIdCallback {
    public Long _id;    // 下划线开头的的字段将不会实例化到数据库
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
