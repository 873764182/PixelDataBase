package pixel.database.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import pixel.database.library.ColumnMapping;
import pixel.database.library.PixelDao;
import pixel.database.library.PixelTools;

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
//            PixelDao.insert(new UserTable("测试", 100));

//            List<TableInfo> tableInfoList = PixelTools.getTableInfo(PixelDao.getTableName(UserTable.class));
//            new AlertDialog.Builder(this).setMessage(tableInfoList.toString()).show();

            PixelTools.updateTable(UserTable.class, new ArrayList<ColumnMapping>() {{
                add(new ColumnMapping("username", "name"));
                add(new ColumnMapping("age", "age"));
            }});
        }
        // 查询
        if (view.getId() == R.id.buttom_2) {
            String _id = mEditTextQueryId.getText().toString();
            List<UserTable> userTables;
            if (_id.length() <= 0) {
                userTables = PixelDao.query(UserTable.class);
//                userTables = PixelDao.query(UserTable.class, (Object) null, null, 1L, 0L);   // 分页查找
            } else {
                userTables = PixelDao.query(UserTable.class, _id, "_id", 1L, 0L);
            }
            ((Button) view).setText(userTables.toString());
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
