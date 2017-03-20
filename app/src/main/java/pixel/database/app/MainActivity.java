package pixel.database.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import pixel.database.library.PDB;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onViewClick(View view) {
        // 插入
        if (view.getId() == R.id.buttom_1) {
            PDB.insert(new UserTable("测试", 100));
        }
        // 查询
        if (view.getId() == R.id.buttom_2) {
            List<Object> objects = PDB.query(UserTable.class, null, null);
            ((Button) view).setText(objects.toString());
        }
        // 更新
        if (view.getId() == R.id.buttom_3) {
            PDB.update(new UserTable("运行", 200), "1", "_id");
        }
        // 删除
        if (view.getId() == R.id.buttom_4) {
            PDB.delete(UserTable.class, "3", "_id");
        }
    }
}
