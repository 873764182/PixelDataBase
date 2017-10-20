package pixel.database.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import pixel.database.library.SqlTemplate;

public class MainActivity extends Activity {
    private EditText mEditTextUpdateId;
    private EditText mEditTextDeleteId;
    private EditText mEditTextQueryId;

    private TextView mTextVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditTextUpdateId = (EditText) findViewById(R.id.editTextUpdateId);
        mEditTextDeleteId = (EditText) findViewById(R.id.editTextDeleteId);
        mEditTextQueryId = (EditText) findViewById(R.id.editTextQueryId);

        mTextVersion = (TextView) findViewById(R.id.textVersion);

        mTextVersion.postDelayed(new Runnable() {
            @Override
            public void run() {
                mTextVersion.setText("数据库版本号: " + SqlTemplate.getSQLiteDatabase().getVersion());
            }
        }, 1000);
    }

    public void onViewClick(View view) {
        // 插入
        if (view.getId() == R.id.buttom_1) {
            SqlTemplate.insert(new UserTable("测试开发", 100));
//            SqlTemplate.insert(new TestTable("测试新增", 0));
        }
        // 查询
        if (view.getId() == R.id.buttom_2) {
            String _id = mEditTextQueryId.getText().toString();
            List<UserTable> userTables;
            if (_id.length() <= 0) {
                userTables = SqlTemplate.query(UserTable.class);
//                userTables = PixelDao.query(UserTable.class, (Object) null, null, 1L, 0L);   // 分页查找
            } else {
//                userTables = PixelDao.query(UserTable.class, _id, "_id", 1L, 0L);
                userTables = SqlTemplate.querySupport(UserTable.class, new String[]{_id}, new String[]{"name"}, false, true, "name", false, 20, 0);
            }
            ((Button) view).setText(userTables.toString());
//            ((Button) view).setText(SqlTemplate.query(TestTable.class).toString());
        }
        // 更新
        if (view.getId() == R.id.buttom_3) {
            String _id = mEditTextUpdateId.getText().toString();
            SqlTemplate.update(new UserTable("运行", 200), _id, "_id");
        }
        // 删除
        if (view.getId() == R.id.buttom_4) {
            String _id = mEditTextDeleteId.getText().toString();
            SqlTemplate.delete(UserTable.class, _id, "_id");
        }
        if (view.getId() == R.id.buttom_5) {
//            List<TableInfo> tableInfoList = SqlTemplate.getTableInfo(PixelDao.getTableName(UserTable.class));
//            new AlertDialog.Builder(this).setMessage(tableInfoList.toString()).show();

//            SqlTemplate.updateTable(UserTable.class, new ArrayList<ColumnMapping>() {{ // 需要保留原数据,传入旧列名与新列名的对应关系,没变更的列名也需要传入.
//                add(new ColumnMapping("username", "name"));
//                add(new ColumnMapping("age", "age"));
//            }});

            SqlTemplate.updateTable(UserTable.class, null);  // 不需要保留原数据
        }

    }
}
