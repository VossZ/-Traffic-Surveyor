package voss.TrafficAnalyzer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BrowseActivity extends AppCompatActivity {
    private Button BrowseTrueBtn, BrowseFalseBtn, BrowseDeleteBtn;
    private ListView nListView;
    private File nDirectory, videoFile;
    private File[] nContents, mContents;
    private Drawable nIcon;
    private String[] nTypes;
    private String fileChosen, tempString, fileDuration, fileStart, fileEnd;
    private MyAdapter myAdapter;
    private int selectedPosition;
    private StringBuilder stringBuilder;
    private JSONObject jsOBJ;
    private JSONArray jsARY;
    private SimpleDateFormat formatter;
    private boolean selected;
    private TextView fileDurPreView, fileStartPreView, fileEndPreView, fileVidPreView;
    private AlertDialog DelDialog;
    private AlertDialog.Builder DelDialogB;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid_browse);

        BrowseTrueBtn = (Button)findViewById(R.id.browseConfirm);
        BrowseFalseBtn = (Button)findViewById(R.id.browseCancel);
        BrowseDeleteBtn = (Button)findViewById(R.id.browseDelete);
        nListView = (ListView)findViewById(R.id.browseList);
        fileDurPreView = (TextView)findViewById(R.id.textDuration);
        fileStartPreView = (TextView)findViewById(R.id.textStartAt);
        fileEndPreView = (TextView)findViewById(R.id.textEndAt);
        fileVidPreView = (TextView)findViewById(R.id.textVidExist);
        nDirectory = new File(Environment.getExternalStorageDirectory() + "/Surveyor/");
        selectedPosition = -1;
        formatter = new SimpleDateFormat("HH:mm:ss");

        nContents = nDirectory.listFiles();

        listTheView(nContents);

        BrowseTrueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileChosen != null) {
                    Toast.makeText(BrowseActivity.this, "正在打开：" + fileChosen,
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setClass(BrowseActivity.this, VidReplayActivity.class);
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
                intent.setClass(BrowseActivity.this, VidReplayActivity.class);

                startActivity(intent);
            }
        });

        BrowseDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected){
                    DelDialogB = new AlertDialog.Builder(BrowseActivity.this);
                    DelDialogB.setCancelable(true);
                    DelDialogB.setTitle("是否删除？");
                    DelDialogB.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            videoFile.delete();
                            File delFile = new File(Environment.getExternalStorageDirectory() + "/Surveyor/" + fileChosen);
                            delFile.delete();


                            nContents = nDirectory.listFiles();

                            listTheView(nContents);
                            selected = false;
                        }
                    });
                    DelDialogB.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    DelDialog = DelDialogB.create();
                    DelDialog.show();
                }
            }
        });

        nListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectedPosition = position;
                myAdapter.notifyDataSetChanged();
                fileChosen = mContents[position].getName();
                Toast.makeText(BrowseActivity.this, "已选定" + fileChosen, Toast.LENGTH_SHORT).show();
                unpackJSON(new File(fileChosen));
                showPreview();
                selected = true;
            }
        });
    }

    public void listTheView(File[] files) {
        List<String> listItems = new ArrayList<String>();
        mContents = new File[0];

        for (int i = 0; i < files.length; i++) {


            if (files[i].getName().endsWith(".json")) {
                listItems.add(files[i].getName());
                mContents = Arrays.copyOf(mContents, mContents.length + 1);
                mContents[mContents.length - 1] = files[i];
            }
        }


        // 填充数据集
        myAdapter = new MyAdapter(this, listItems);
        nListView.setAdapter(myAdapter);
    }




    public void unpackJSON(File file){
        try {

            InputStream is = new FileInputStream(Environment.getExternalStorageDirectory() + "/Surveyor/" +file);
            InputStreamReader streamReader = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(streamReader);
            String line = null;
            stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                // stringBuilder.append(line);
                stringBuilder.append(line);
            }
            tempString = stringBuilder.toString();
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "读取失败！", Toast.LENGTH_SHORT).show();
        }
        try {
            jsOBJ = new JSONObject(tempString);
            videoFile = new File(Environment.getExternalStorageDirectory() + jsOBJ.getString("VideoPath"));
            jsARY = jsOBJ.getJSONArray("MainTable");
            fileStart = jsARY.getJSONObject(0).getString("Time");
            fileEnd = jsARY.getJSONObject(jsARY.length()-1).getString("Time");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSON文件格式错误！", Toast.LENGTH_SHORT).show();
        }
    }

    public void showPreview(){
        long fileDur;
        fileDur = (jsARY.length()-1+57600)*1000;
        fileDuration = formatter.format(new Date(fileDur));

        fileDurPreView.setText("长度：" + fileDuration);
        fileStartPreView.setText("开始于：" + fileStart);
        fileEndPreView.setText("结束于：" + fileEnd);

        if (videoFile.exists()){
            fileVidPreView.setText("视频文件：有");
        } else {
            fileVidPreView.setText("视频文件：无");
        }

    }

    private class MyAdapter extends BaseAdapter {
        Context context;
        List<String> filelist;
        LayoutInflater mInflater;
        MyAdapter(Context context, List<String> mList){
            this.context = context;
            this.filelist = mList;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            return filelist.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder = null;
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.browse_item,parent,false);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView)convertView.findViewById(R.id.fileName);
                viewHolder.select = (RadioButton)convertView.findViewById(R.id.radioButton);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.name.setText(filelist.get(position));
            if(selectedPosition == position){
                viewHolder.select.setChecked(true);
            }
            else{
                viewHolder.select.setChecked(false);
            }
            return convertView;
        }
    }
    private class ViewHolder{
        TextView name;
        RadioButton select;
    }

}


