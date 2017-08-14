package voss.TrafficAnalyzer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.utils.overlay.SmoothMoveMarker;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceListener;
import com.amap.api.trace.TraceLocation;
import com.amap.api.trace.TraceOverlay;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ReplayActivity extends AppCompatActivity {
    private File mLogFile, mVideoFile;
    private TextureMapView RepMapView;
    private AMap RepMap;
    private VideoView RepVidView;
    private Polyline RepTraceLine;
    private Marker RepMarker, PosMarker1, PosMarker2;
    private MarkerOptions RepMarkerOption, PosMarkerOption;
    private Button RepStartBtn;
    private Button RepMenuBtn;
    private Button RepPauseBtn;
    private Button RepReturnBtn;
    private boolean Playing, Paused, FileReadiness, PosMarkerStat;
    private List<LatLng> locList;
    private JSONObject jsOBJ;
    private JSONArray lineARY, pointsARY;
    private LatLng markerLoc;
    private SeekBar mSeekBar;
    private StringBuilder stringBuilder;
    private String tempString;
    private int VidDur, TargetPos, PtsCount, currentPts;
    private Timer RepTimer, RepLocTimer;
    private Handler RepTimerHandler, RepLocTimerHandler;
    private LineChart RepChart;
    private List<Entry> ChartEntries;
    private LineDataSet ChartDataSet;
    private LineData ChartData;
    private SmoothMoveMarker RepMoveMarker;
    public CameraUpdate RepCameraUpdate;
    public CameraUpdateFactory RepCameraUpdateFactory;
    private TraceOverlay RecTrace;
    private LBSTraceClient nTraceClient;
    private List<TraceLocation> nTraceList;
    private List<LatLng> markerlist;
    private PowerManager.WakeLock wakeLock;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay);

        RepStartBtn = (Button)findViewById(R.id.repStartBtn);
        RepMenuBtn = (Button)findViewById(R.id.repMenuBtn);
        RepPauseBtn = (Button)findViewById(R.id.repPauseBtn);
        RepReturnBtn = (Button)findViewById(R.id.repReturnBtn);
        RepMapView = (TextureMapView)findViewById(R.id.repMap);
        RepVidView = (VideoView) findViewById(R.id.repVidView);
        mSeekBar = (SeekBar)findViewById(R.id.seekBar);
        RepChart = (LineChart)findViewById(R.id.repChart);

        mSeekBar.setMax(1000);


        RepMapView.onCreate(savedInstanceState);
        if (RepMap == null) {
            RepMap = RepMapView.getMap();
        }

        recvPath();
        if (FileReadiness) {
            initPerm();
            unpackJSON(mLogFile);
            drawMap();
            drawChart();
            markerTiming();
        }


        if (FileReadiness) {
            RepStartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Playing) {
                        Playing = true;
                        if (Paused) {
                            Paused = false;
                            RepVidView.resume();
                        } else {
                            RepVidView.start();
                        }
                        RepVidView.requestFocus();


                        VidDur = RepVidView.getDuration();
                        Toast.makeText(ReplayActivity.this, "" + VidDur, Toast.LENGTH_SHORT).show();

                        initTimer();


                        RepStartBtn.setText("暂停");
                        RepStartBtn.setCompoundDrawablesRelative(getDrawable(android.R.drawable.ic_media_pause),null,null,null);
                    } else if (Playing) {
                        Playing = false;
                        RepVidView.pause();
                        Paused = true;

                        RepStartBtn.setText("回放");
                        RepStartBtn.setCompoundDrawablesRelative(getDrawable(android.R.drawable.ic_media_play),null,null,null);
                    }
                }
            });

            RepPauseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
        RepMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Playing){
                    Intent intent = new Intent();
                    intent.setClass(ReplayActivity.this, BrowseActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(ReplayActivity.this, "正在播放！", Toast.LENGTH_SHORT).show();
                }


            }
        });


        RepReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Playing){
                    Intent intent = new Intent();
                    intent.setClass(ReplayActivity.this, VidRecordActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(ReplayActivity.this, "正在播放！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (FileReadiness) {
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    TargetPos = VidDur * progress / 1000;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                    RepVidView.seekTo(TargetPos);
                    initTimer();

                }
            });
        }
        acquireWakeLock();
    }

    private void initTimer() {
        RepTimer = new Timer();
        RepTimerHandler = new Handler();
        RepTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                RepTimerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (Playing) {

                            if (mSeekBar.getProgress() < 1000) {
                                mSeekBar.setProgress(mSeekBar.getProgress() + 1);
                            } else {
                                RepVidView.pause();
                                Playing = false;
                                Toast.makeText(ReplayActivity.this, "回放完毕！", Toast.LENGTH_SHORT).show();

                                RepStartBtn.setCompoundDrawablesRelative(getDrawable(android.R.drawable.ic_media_play),null,null,null);
                                RepStartBtn.setText("回放");

                                RepVidView.seekTo(1);
                                mSeekBar.setProgress(0);

                            }


                        }
                    }
                });

            }
        }, 0, VidDur / 1000);
    }

    public void recvPath(){
        try {

            Intent intent = getIntent();
            mLogFile = new File(intent.getStringExtra("LogPath"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "打开文件错误", Toast.LENGTH_SHORT).show();
            FileReadiness = false;
        }

        if (mLogFile == null){
            Toast.makeText(this, "未打开文件", Toast.LENGTH_SHORT).show();
            FileReadiness = false;
        } else {
            Toast.makeText(this, "正在载入:  " + mLogFile, Toast.LENGTH_SHORT).show();
            FileReadiness = true;
            Log.e("file", mLogFile + "");
            Log.e("isThere", mLogFile.exists() + "");
        }

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
            mVideoFile = new File(jsOBJ.getString("VideoPath"));
            lineARY = jsOBJ.getJSONArray("MainTable");
            pointsARY = jsOBJ.getJSONArray("PointsTable");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSON文件格式错误！", Toast.LENGTH_SHORT).show();
        }
        RepVidView.setVideoPath(Environment.getExternalStorageDirectory() +  mVideoFile.toString());
    }

    public void drawMap(){
        locList = new ArrayList<LatLng>();

        nTraceClient = new LBSTraceClient(this);
        nTraceList = new ArrayList<TraceLocation>();
        PtsCount = lineARY.length();
        for (int i = 0; i < PtsCount; i++){
            try {
                JSONObject tmpPLObj = lineARY.getJSONObject(i);
                TraceLocation tmpLocation = new TraceLocation();
                tmpLocation.setLatitude(tmpPLObj.getDouble("Lat"));
                tmpLocation.setLongitude(tmpPLObj.getDouble("Lon"));
                tmpLocation.setSpeed((float)tmpPLObj.getDouble("Speed"));
                tmpLocation.setBearing((float)tmpPLObj.getDouble("Bearing"));
                tmpLocation.setTime(tmpPLObj.getInt("ID") * 1000);
                nTraceList.add(tmpLocation);
                locList.add(new LatLng(tmpPLObj.getDouble("Lat"), tmpPLObj.getDouble("Lon")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        nTraceClient.queryProcessedTrace(1, nTraceList, LBSTraceClient.TYPE_AMAP, new TraceListener() {
            @Override
            public void onRequestFailed(int i, String s) {
                Toast.makeText(ReplayActivity.this, "轨迹纠正失败，显示原始数据", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTraceProcessing(int i, int i1, List<LatLng> list) {

            }

            @Override
            public void onFinished(int i, List<LatLng> list, int i1, int i2) {
                Toast.makeText(ReplayActivity.this, "轨迹纠正成功，耗时：" + i2 +
                        "ms，显示处理后轨迹", Toast.LENGTH_SHORT).show();
                locList = list;
                PosMarkerStat = true;
            }
        });

        RecTrace = new TraceOverlay(RepMap, locList);

        /*
        RepTraceLine = RepMap.addPolyline((new PolylineOptions())
                .addAll(locList)
                .width(8)
                .color(Color.argb(255, 1, 1, 1)));
*/
        for (int i = 0; i < pointsARY.length(); i++){
            try {
                JSONObject tmpPTObj = pointsARY.getJSONObject(i);
                markerLoc = new LatLng(tmpPTObj.getDouble("Lat"), tmpPTObj.getDouble("Lon"));
                int Note = tmpPTObj.getInt("Type");
                RepMarkerOption = new MarkerOptions().position(markerLoc).draggable(false);
                switch (Note){
                    case 1:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),android.R.drawable.presence_online)));

                        break;
                    case 2:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),android.R.drawable.ic_media_pause)));

                        break;
                    case 3:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),android.R.drawable.ic_delete)));

                        break;
                    case 4:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),android.R.drawable.ic_menu_myplaces)));

                        break;
                    case 5:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),android.R.drawable.ic_menu_camera)));

                        break;
                }
                RepMarker = RepMap.addMarker(RepMarkerOption);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        RepCameraUpdateFactory = new CameraUpdateFactory();

        RepCameraUpdate =RepCameraUpdateFactory.
                newCameraPosition(new CameraPosition(locList.get(0),15,0,0));
        RepMap.animateCamera(RepCameraUpdate);

        RecTrace.zoopToSpan();
    }

    public void markerTiming(){
        RepLocTimer = new Timer();
        RepLocTimerHandler = new Handler();
        RepLocTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                RepLocTimerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (PosMarkerStat) {
                            if (currentPts != locList.size() * mSeekBar.getProgress() / 1000) {
                                currentPts = locList.size() * mSeekBar.getProgress() / 1000;

                                markerlist = new ArrayList<LatLng>();
                                for (int i = currentPts; i < locList.size(); i++) {
                                    markerlist.add(locList.get(i));
                                }

                            if (Playing) {

                                if (RepMoveMarker != null) {
                                    RepMoveMarker.destroy();
                                }
                                RepMoveMarker = new SmoothMoveMarker(RepMap);
                                if (markerlist != null && markerlist.size() >= 2) {
                                    RepMoveMarker.setPoints(markerlist);
                                    RepMoveMarker.setTotalDuration(VidDur / 1000 * (1 - mSeekBar.getProgress() / 1000));
                                    RepMoveMarker.startSmoothMove();
                                    RepCameraUpdate = RepCameraUpdateFactory.
                                            newCameraPosition(new CameraPosition(markerlist.get(0), 15, 0, 0));
                                    RepMap.animateCamera(RepCameraUpdate);
                                }
                            }
                            } else if (RepMoveMarker != null && !Playing) {
                                RepMoveMarker.stopMove();
                            }
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RepMap.clear();
        if (RepTraceLine != null) {
            RepTraceLine.remove();
        }
        RepMapView.onDestroy();
        releaseWakeLock();
    }
    @Override
    public void onPause() {
        super.onPause();
        RepMapView.onPause();
        releaseWakeLock();
    }
    @Override
    public void onResume() {
        super.onResume();
        RepMapView.onResume();
        acquireWakeLock();
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
            ActivityCompat.requestPermissions(ReplayActivity.this, new String[]{
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

    public void drawChart(){
        RepChart.setDragDecelerationEnabled(false);
        ChartEntries = new ArrayList<Entry>();
        for (int i = 0; i < PtsCount; i++){
            try {
                JSONObject tmpChtObj = lineARY.getJSONObject(i);
                Entry tmpEntry = new Entry(tmpChtObj.getInt("ID"), (float) tmpChtObj.getDouble("Distance"));
                ChartEntries.add(tmpEntry);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ChartDataSet = new LineDataSet(ChartEntries,"距离");
        ChartDataSet.setDrawCircles(false);
        ChartDataSet.setDrawFilled(true);
        ChartData = new LineData(ChartDataSet);
        RepChart.setData(ChartData);
        RepChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        RepChart.getAxisLeft().setAxisMinimum(0);
        RepChart.invalidate();


    }

    @Override
    public void onBackPressed(){
        if (Playing) {
            Toast.makeText(this, "正在回放！", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent();
            intent.setClass(ReplayActivity.this, VidRecordActivity.class);
            startActivity(intent);
        }
    }


    public void acquireWakeLock()
    {
        if (null == wakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock)
            {
                wakeLock.acquire();
            }
        }
    }

    //释放设备电源锁
    public void releaseWakeLock()
    {
        if (null != wakeLock)
        {
            wakeLock.release();
            wakeLock = null;
        }
    }

}
