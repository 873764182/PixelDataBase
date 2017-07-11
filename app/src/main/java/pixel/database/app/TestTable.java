package pixel.database.app;

import pixel.database.library.TableColumn;

/**
 * Created by pixel on 2017/7/6.
 */

public class TestTable {

    @TableColumn
    public String name;

    @TableColumn
    public Integer age;

    public TestTable() {
    }

    public TestTable(String name, Integer age) {
        this.name = name;
        this.age = age;
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
}
