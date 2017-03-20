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
        if (view.getId() == R.id.buttom_1) {
            PDB.insert(new UserTable("测试", 100));
        }
        if (view.getId() == R.id.buttom_2) {
            List<Object> objects = PDB.query(UserTable.class, null, null);
            Toast.makeText(this, "总条数: " + objects.size() + "\n" + objects.toString(), Toast.LENGTH_LONG).show();
            ((Button) view).setText(objects.toString());
        }
        if (view.getId() == R.id.buttom_3) {
            PDB.update(new UserTable("运行", 200), "1", "_id");
        }
        if (view.getId() == R.id.buttom_4) {
            PDB.delete(UserTable.class, "3", "_id");
        }
    }
}
