package voss.TrafficAnalyzer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrowseActivity extends AppCompatActivity {
    private Button BrowseTrueBtn, BrowseFalseBtn;
    private ListView nListView;
    private List<Map<String,Object>> nList;
    private File nDirectory, currentParent;
    private File[] nContents, mContents;
    private Drawable nIcon;
    private String[] nTypes;
    private String fileChosen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        BrowseTrueBtn = (Button)findViewById(R.id.browseConfirm);
        BrowseFalseBtn = (Button)findViewById(R.id.browseCancel);
        nListView = (ListView)findViewById(R.id.browseList);
        nDirectory = new File(Environment.getExternalStorageDirectory() + "/Surveyor/");
        nContents = nDirectory.listFiles();

        initPerm();
        listTheView(nContents);

        BrowseTrueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileChosen != null) {
                    Toast.makeText(BrowseActivity.this, "正在打开：" + fileChosen, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setClass(BrowseActivity.this, ReplayActivity.class);
                    intent.putExtra("LogPath", fileChosen);
                    startActivity(intent);
                } else {
                    Toast.makeText(BrowseActivity.this, "未选择文件！", Toast.LENGTH_SHORT).show();
                }

            }
        });

        BrowseFalseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(BrowseActivity.this, ReplayActivity.class);

                startActivity(intent);
            }
        });

        nListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                // 如果用户单击了文件，直接返回，不做任何处理
                if (mContents[position].isFile()) {
                    // 也可自定义扩展打开这个文件等
                    if (mContents[position].getName().endsWith(".vlog.json")){
                        fileChosen = mContents[position].getName();
                        Toast.makeText(BrowseActivity.this, "已选定" + fileChosen, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(BrowseActivity.this, "请选择JSON文件！", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

            }
        });
    }

    public void listTheView(File[] files) {
        List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
        mContents = new File[0];

        for (int i = 0; i < files.length; i++) {

            Map<String, Object> listItem = new HashMap<String, Object>();

            if (files[i].getName().endsWith(".json")) {
                listItem.put("icon", android.R.drawable.ic_dialog_map);
                listItem.put("filename", files[i].getName());
                listItems.add(listItem);
                mContents = Arrays.copyOf(mContents, mContents.length + 1);
                mContents[mContents.length - 1] = files[i];
            }
        }

        // 定义一个SimpleAdapter
        SimpleAdapter adapter = new SimpleAdapter(
                BrowseActivity.this, listItems, R.layout.browse_item,
                new String[]{"filename", "icon"}, new int[]{
                R.id.fileName, R.id.fileIcon});

        // 填充数据集
        nListView.setAdapter(adapter);
    }


    public void initPerm(){
            boolean permitted = (ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this,
            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
        if (!permitted) {
            ActivityCompat.requestPermissions(BrowseActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            }, 101);
        }
    }
}


