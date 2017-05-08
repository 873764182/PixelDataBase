package pixel.database.app.entity;

import pixel.database.app.annotation.MappField;

/**
 * Created by pixel on 2017/5/8.
 */

public class UserTable {

    private Long _id;

    @MappField(description = "用户名字")
    private String name;

    @MappField("哈哈哈哈哈哈哈")
    private Integer age;

    public UserTable() {
    }

    public UserTable(Long _id, String name, Integer age) {
        this._id = _id;
        this.name = name;
        this.age = age;
    }

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
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
}
