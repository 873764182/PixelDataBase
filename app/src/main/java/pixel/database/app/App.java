package pixel.database.app;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.facebook.stetho.Stetho;

import java.util.ArrayList;
import java.util.List;

import pixel.database.library.ColumnMapping;
import pixel.database.library.OnDbUpdateCallback;
import pixel.database.library.SqlTemplate;

/**
 * Created by pixel on 2017/3/20.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initializeWithDefaults(this);    // TODO 数据库调试 发布时去掉

        // 需要生成数据表的对象 注意: 数据名是对象的全路径名,不能随意修改对象包名与对象名.
        Class<?>[] tables = new Class[]{UserTable.class, MsgTable.class, TestTable.class, BaseTable.class};

        // 直接修改版本号，不传入回调接口数据库会重建所有表，现有的表数据会被删除。
        // SqlTemplate.initDataBase(this, "pdb.db", 3, tables);

        // 初始化数据库与创建数据库表 version修改后,onUpgrade方法会被回调.
        SqlTemplate.initDataBase(this, "pdb.db", 3, tables, new OnDbUpdateCallback() {
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion, Class<?>... tables) {
                if (oldVersion >= newVersion) {
                    return;
                }
                for (Class<?> table : tables) {
                    // 自定义修改部分表
                    if (table == UserTable.class) {
                        // 如果对象中有2两个属性有映射到数据库，则则两个字段都要声明，即使没有变更也要加入ColumnMapping。
                        List<ColumnMapping> columnMappingList = new ArrayList<ColumnMapping>() {{
                            // add(new ColumnMapping("name", "username")); // 数据库的列名从'user'变为'username',将原来'user'列的数据库,转移到新的'username'列上.
                            add(new ColumnMapping("username", "name")); // 数据库的列名从'username'变为'user',将原来'username'列的数据库,转移到新的'user'列上.
                            add(new ColumnMapping("age", "age")); // 'age'属性未变更也需要声明
                        }};
                        SqlTemplate.updateOrCreateTable(table, columnMappingList);    // 更新表结构,保留原数据.
                    }
                    /*else {
                        SqlTemplate.updateOrCreateTable(table, null);    // 更新表结构,不保留原数据.
                    }*/
                }
            }
        });
    }
}
