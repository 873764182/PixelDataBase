package pixel.database.app;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

import pixel.database.library.PixelDao;

public class MainActivity extends Activity {
    private EditText mEditTextUpdateId;
    private EditText mEditTextDeleteId;
    private EditText mEditTextQueryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditTextUpdateId = (EditText) findViewById(R.id.editTextUpdateId);
        mEditTextDeleteId = (EditText) findViewById(R.id.editTextDeleteId);
        mEditTextQueryId = (EditText) findViewById(R.id.editTextQueryId);
    }

    public void onViewClick(View view) {
        // 插入
        if (view.getId() == R.id.buttom_1) {
            PixelDao.insert(new UserTable("测试", 100));
        }
        // 查询
        if (view.getId() == R.id.buttom_2) {
            String _id = mEditTextQueryId.getText().toString();
            List<Object> objects;
            if (_id.length() <= 0) {
                objects = PixelDao.query(UserTable.class);
            } else {
                objects = PixelDao.query(UserTable.class, _id, "_id");
            }

            int version = PixelDao.getSQLiteDatabase().getVersion();

            ((Button) view).setText(objects.toString() + "\n" + version);
        }
        // 更新
        if (view.getId() == R.id.buttom_3) {
            String _id = mEditTextUpdateId.getText().toString();
            PixelDao.update(new UserTable("运行", 200), _id, "_id");
        }
        // 删除
        if (view.getId() == R.id.buttom_4) {
            String _id = mEditTextDeleteId.getText().toString();
            PixelDao.delete(UserTable.class, _id, "_id");
        }
    }
}
