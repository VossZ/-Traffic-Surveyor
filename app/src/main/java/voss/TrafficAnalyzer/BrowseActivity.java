package voss.TrafficAnalyzer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

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
    private Button BrowseTrueBtn;
    private Button BrowseDeleteBtn;
    private Button Filter0Btn;
    private ImageButton Filter1Btn, Filter2Btn;
    private ListView nListView;
    private File nDirectory, videoFile;
    private File[] nContents, mContents;
    private Drawable nIcon;
    private String[] filters, filterExt;
    private String fileChosen, tempString, fileDuration, fileStart, fileEnd, IntersecName, IntersecDate;
    private StringBuilder IntersecDirections;
    private MyAdapter myAdapter;
    private int selectedPosition, fileType, filter;
    private StringBuilder stringBuilder;
    private JSONObject jsOBJ, jsObj0;
    private JSONArray jsARY;
    private SimpleDateFormat formatter;
    private boolean selected, IntersecDIRS;
    private TextView preViewL1, preViewL2, preViewL3, preViewL4, preViewFilter;
    private AlertDialog DelDialog;
    private AlertDialog.Builder DelDialogB;
    private SimpleDateFormat timeFormat;
    private AMap BrowseMap;
    private TextureMapView BrowseMapView;
    private Marker BrowseMarker;
    private MarkerOptions BrowseMarkerOption;
    private LatLng Location;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        BrowseTrueBtn = (Button)findViewById(R.id.browseConfirm);
        BrowseDeleteBtn = (Button)findViewById(R.id.browseDelete);
        Filter0Btn = (Button)findViewById(R.id.filter0Btn);
        Filter1Btn = (ImageButton)findViewById(R.id.filter1Btn);
        Filter2Btn = (ImageButton)findViewById(R.id.filter2Btn);
        nListView = (ListView)findViewById(R.id.browseList);
        preViewL1 = (TextView)findViewById(R.id.textL1);
        preViewL2 = (TextView)findViewById(R.id.textL2);
        preViewL3 = (TextView)findViewById(R.id.textL3);
        preViewL4 = (TextView)findViewById(R.id.textL4);
        preViewFilter = (TextView)findViewById(R.id.filterText);
        BrowseMapView = (TextureMapView)findViewById(R.id.browseMap);

        nDirectory = new File(Environment.getExternalStorageDirectory() + "/Surveyor/");
        selectedPosition = -1;
        formatter = new SimpleDateFormat("HH:mm:ss");
        filters = getResources().getStringArray(R.array.fileTypes);
        filterExt = getResources().getStringArray(R.array.filterExt);
        timeFormat = new SimpleDateFormat("yyyy年MM月dd日_HH时mm分ss秒");

        nContents = nDirectory.listFiles();

        listTheView();

        BrowseMapView.onCreate(savedInstanceState);
        if(BrowseMap == null){
            BrowseMap = BrowseMapView.getMap();
        }

        BrowseTrueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileChosen != null) {
                    Toast.makeText(BrowseActivity.this, "正在打开：" +
                                    fileChosen.substring(0, fileChosen.length() - 10),
                            Toast.LENGTH_SHORT).show();
                    switch (fileType) {
                        case 1:
                            Intent intent1 = new Intent();
                            intent1.setClass(BrowseActivity.this, VidReplayActivity.class);
                            intent1.putExtra("LogPath", fileChosen);
                            startActivity(intent1);
                            break;
                        case 2:
                            Intent intent2 = new Intent();
                            intent2.setClass(BrowseActivity.this, IntersecReplayActivity.class);
                            intent2.putExtra("LogPath", fileChosen);
                            startActivity(intent2);
                            break;
                    }
                } else {
                    Toast.makeText(BrowseActivity.this, "未选择文件！", Toast.LENGTH_SHORT).show();
                }

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
                            if (videoFile != null) {
                                videoFile.delete();
                            }
                            File delFile = new File(Environment.getExternalStorageDirectory() + "/Surveyor/" + fileChosen);
                            delFile.delete();


                            nContents = nDirectory.listFiles();

                            listTheView();
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
                if (fileChosen.endsWith(filterExt[1])) {
                    fileType = 1;
                    unpackVidJSON(new File(fileChosen));
                }
                /*
                else if (fileChosen.endsWith(filterExt[2])){
                    fileType = 2;
                    unpackIntersecJSON(new File(fileChosen));
                }
                */
                else {
                    Toast.makeText(BrowseActivity.this, "不支持的JSON文件！", Toast.LENGTH_SHORT).show();
                }
                showPreview(fileType);
                selected = true;
            }
        });

        Filter0Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = 0;
                listTheView();

            }
        });

        Filter1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = 1;
                listTheView();

            }
        });

        Filter2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = 2;
                listTheView();

            }
        });
    }

    public void listTheView() {
        List<String> listItems = new ArrayList<String>();
        List<String> listTimes = new ArrayList<String>();

        mContents = new File[0];

        for (int i = 0; i < nContents.length; i++) {

            if (nContents[i].getName().endsWith(filterExt[filter])) {
                listItems.add(nContents[i].getName().substring(0, (int)nContents[i].getName().length() - 10));
                listTimes.add(timeFormat.format(nContents[i].lastModified()));
                mContents = Arrays.copyOf(mContents, mContents.length + 1);
                mContents[mContents.length - 1] = nContents[i];
            }
        }

        myAdapter = new MyAdapter(this, listItems, listTimes);
        nListView.setAdapter(myAdapter);

        preViewFilter.setText("正在显示：" + filters[filter] + "记录文件");

    }




    public void unpackVidJSON(File file){
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
            boolean invalidlocation = true;
            int pointchosen = 0;
            jsOBJ = new JSONObject(tempString);
            videoFile = new File(Environment.getExternalStorageDirectory() + jsOBJ.getString("VideoPath"));
            jsARY = jsOBJ.getJSONArray("MainTable");
            fileStart = jsARY.getJSONObject(0).getString("Time");
            fileEnd = jsARY.getJSONObject(jsARY.length()-1).getString("Time");
            while (invalidlocation){
                Location = new LatLng(jsARY.getJSONObject(pointchosen).getDouble("Lat"),
                        jsARY.getJSONObject(pointchosen).getDouble("Lon"));
                if (Location.equals(new LatLng(0,0))){
                    pointchosen ++;
                } else {
                    invalidlocation = false;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSON文件格式错误！", Toast.LENGTH_SHORT).show();
        }
        if (BrowseMarker != null){
            BrowseMarker.remove();
        }
        BrowseMarkerOption = new MarkerOptions().position(Location).draggable(false);
        BrowseMarker = BrowseMap.addMarker(BrowseMarkerOption);
        BrowseMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition(Location,14,0,0)));

    }


    public void unpackIntersecJSON(File file){
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

        videoFile = null;

        try {
            jsOBJ = new JSONObject(tempString);
            jsObj0 = new JSONObject();
            jsObj0 = jsOBJ.getJSONObject("Info");
            IntersecName = jsObj0.getString("Name");
            IntersecDate = jsObj0.getString("Date");

            IntersecDirections = new StringBuilder();
            IntersecDIRS = false;

            if (jsOBJ.has("N")){
                IntersecDirections.append("北");
                IntersecDIRS = true;
            }
            if (jsOBJ.has("E")){
                if (IntersecDIRS){
                    IntersecDirections.append("，");
                }
                IntersecDirections.append("东");
                IntersecDIRS = true;
            }
            if (jsOBJ.has("S")){
                if (IntersecDIRS){
                    IntersecDirections.append("，");
                }
                IntersecDirections.append("南");
                IntersecDIRS = true;
            }
            if (jsOBJ.has("W")){
                if (IntersecDIRS){
                    IntersecDirections.append("，");
                }
                IntersecDirections.append("西");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSON文件格式错误！", Toast.LENGTH_SHORT).show();
        }
    }

    public void showPreview(int type){
        switch (fileType) {
            case 1:
                long fileDur;
                fileDur = (jsARY.length() - 1 + 57600) * 1000;
                fileDuration = formatter.format(new Date(fileDur));

                preViewL1.setText("长度：" + fileDuration);
                preViewL2.setText("开始于：" + fileStart);
                preViewL3.setText("结束于：" + fileEnd);

                if (videoFile.exists()) {
                    preViewL4.setText("视频文件：有");
                } else {
                    preViewL4.setText("视频文件：无");
                }
                break;
            case 2:
                preViewL1.setText("路口名：" + IntersecName);
                preViewL2.setText("录制于：" + IntersecDate);
                preViewL3.setText("包含方向：" + IntersecDirections.toString());

                break;
        }
    }

    private class MyAdapter extends BaseAdapter {
        Context context;
        List<String> filelist, timelist;
        LayoutInflater mInflater;
        MyAdapter(Context context, List<String> mList, List<String> nList){
            this.context = context;
            this.filelist = mList;
            this.timelist = nList;
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
                viewHolder.modified = (TextView)convertView.findViewById(R.id.timeModified);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.name.setText(filelist.get(position));
            viewHolder.modified.setText(timelist.get(position));
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
        TextView modified;
    }

    @Override
    public void onDestroy(){
        finish();
        super.onDestroy();
    }
}


