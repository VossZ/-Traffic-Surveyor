package voss.TrafficAnalyzer;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VidReplayActivity extends AppCompatActivity {
    private File mLogFile, mVideoFile;
    private TextureMapView RepMapView;
    private AMap RepMap;
    private VideoView RepVidView;
    private Polyline RepTraceLine;
    private Marker RepMarker, PosMarker;
    private MarkerOptions RepMarkerOption, PosMarkerOption;
    private ImageButton RepStartBtn;
    //private ImageButton RepMenuBtn;
    private ImageButton RepCenterBtn;
    private boolean Playing, Paused, FileReadiness, ChartPointed, validNote;
    private List<LatLng> locList;
    private JSONObject jsOBJ;
    private JSONArray lineARY, pointsARY;
    private LatLng markerLoc;
    private SeekBar mSeekBar;
    private StringBuilder stringBuilder;
    private String tempString, spdDisp, dirDisp;
    private int VidDur, TargetPos, PtsCount, currentPts, infoPos;
    private Timer RepTimer, RepLocTimer;
    private Handler RepTimerHandler, RepLocTimerHandler;
    private LineChart RepChart;
    private List<Entry> ChartEntries, markerEntries;
    private LineDataSet ChartDataSet, markerDataSet;
    private LineData ChartData;
    private SmoothMoveMarker RepMoveMarker;
    public CameraUpdate RepCameraUpdate;
    public CameraUpdateFactory RepCameraUpdateFactory;
    private TraceOverlay RecTrace;
    private LBSTraceClient nTraceClient;
    private List<TraceLocation> nTraceList;
    private List<LatLng> markerlist;
    private PowerManager.WakeLock wakeLock;
    private TextView brnText, spdText;
    private double rBearing, rSpeed;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid_replay);

        RepStartBtn = (ImageButton) findViewById(R.id.repStartBtn);
        //RepMenuBtn = (ImageButton) findViewById(R.id.repMenuBtn);
        RepCenterBtn = (ImageButton)findViewById(R.id.repCenterBtn);
        RepMapView = (TextureMapView)findViewById(R.id.repMap);
        RepVidView = (VideoView) findViewById(R.id.repVidView);
        mSeekBar = (SeekBar)findViewById(R.id.seekBar);
        RepChart = (LineChart)findViewById(R.id.repChart);
        brnText = (TextView)findViewById(R.id.textRepBearing);
        spdText = (TextView)findViewById(R.id.textRepSpeed);

        mSeekBar.setMax(1000);


        RepMapView.onCreate(savedInstanceState);
        if (RepMap == null) {
            RepMap = RepMapView.getMap();
        }

        recvPath();
        if (FileReadiness) {
            unpackJSON(mLogFile);
            drawMap();
            markerTiming();
        }
        drawChart();


        if (FileReadiness) {
            RepStartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Playing) {
                        if (Paused) {
                            Paused = false;
                            RepVidView.start();
                            //RepVidView.resume();
                        } else {
                            VidDur = RepVidView.getDuration();
                            RepVidView.start();
                            initTimer();
                        }
                        Playing = true;

                        RepVidView.requestFocus();
                        RepStartBtn.setImageDrawable(getDrawable(android.R.drawable.ic_media_pause));
                    } else if (Playing){
                        Playing = false;
                        RepVidView.pause();
                        Paused = true;

                        RepStartBtn.setImageDrawable(getDrawable(android.R.drawable.ic_media_play));

                    }
                }
            });

            RepCenterBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RepMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition(locList.get(currentPts),14,0,0)));
                }
            });
        }
        /*RepMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Playing){
                    Intent intent = new Intent();
                    intent.setClass(VidReplayActivity.this, BrowseActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(VidReplayActivity.this, "正在播放！", Toast.LENGTH_SHORT).show();
                }


            }
        });*/


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

                    if (mSeekBar.getProgress() != 1000) {
                        RepVidView.seekTo(TargetPos);
                        initTimer();
                    } else {
                        RepVidView.seekTo(TargetPos - 1);
                        RepVidView.pause();
                        RepStartBtn.setImageDrawable(getDrawable(android.R.drawable.ic_media_pause));
                        Playing = false;
                    }
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
                                Toast.makeText(VidReplayActivity.this, "回放完毕！", Toast.LENGTH_SHORT).show();

                                RepStartBtn.setImageDrawable(getDrawable(android.R.drawable.ic_media_pause));


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
                Toast.makeText(VidReplayActivity.this, "轨迹纠正失败，显示原始数据", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTraceProcessing(int i, int i1, List<LatLng> list) {

            }

            @Override
            public void onFinished(int i, List<LatLng> list, int i1, int i2) {
                Toast.makeText(VidReplayActivity.this, "轨迹纠正成功，耗时：" + i2 +
                        "ms，显示处理后轨迹", Toast.LENGTH_SHORT).show();
                locList = list;
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
                validNote = true;
                JSONObject tmpPTObj = pointsARY.getJSONObject(i);
                markerLoc = new LatLng(tmpPTObj.getDouble("Lat"), tmpPTObj.getDouble("Lon"));
                int Note = tmpPTObj.getInt("Type");
                RepMarkerOption = new MarkerOptions().position(markerLoc).draggable(false);
                switch (Note){
                    case 0:
                        validNote = false;
                        break;
                    case 10:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s10)));

                        break;
                    case 11:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s11)));

                        break;
                    case 12:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s12)));

                        break;
                    case 13:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s13)));

                        break;
                    case 14:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s14)));

                        break;
                    case 15:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s15)));

                        break;
                    case 20:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s20)));

                        break;
                    case 21:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s21)));

                        break;
                    case 22:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s22)));

                        break;
                    case 23:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s23)));

                        break;
                    case 24:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s24)));

                        break;
                    case 25:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s25)));

                        break;
                    case 30:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s30)));

                        break;
                    case 31:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s31)));

                        break;
                    case 32:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s32)));

                        break;
                    case 33:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s33)));

                        break;
                    case 34:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s34)));

                        break;
                    case 35:
                        RepMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.s35)));

                        break;
                }
                if (validNote) {
                    RepMarker = RepMap.addMarker(RepMarkerOption);
                }
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
                        if (Playing) {
                            if (currentPts != locList.size() * mSeekBar.getProgress() / 1000) {
                                currentPts = locList.size() * mSeekBar.getProgress() / 1000;


                                if (PosMarker != null) {
                                    PosMarker.remove();
                                }
                                PosMarkerOption = new MarkerOptions().draggable(false).position(locList.get(currentPts));
                                PosMarker = RepMap.addMarker(PosMarkerOption);
                            }
                            dispInfo();
                            drawChartMarker();
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    public void dispInfo(){
        infoPos = lineARY.length() * mSeekBar.getProgress() / 1000 - 1;
        JSONObject infoObj;
        try {
            infoObj = lineARY.getJSONObject(infoPos);
            rBearing = infoObj.getDouble("Bearing");
            rSpeed = infoObj.getDouble("Speed") * 3.6;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DecimalFormat decimalFormat=new DecimalFormat("000.0");
        spdDisp = "速度：" + decimalFormat.format(rSpeed) + "km/h";

        dirDisp = "方向：";
        if (rBearing == -1){
            dirDisp += "未确定方向";
        } else if (rBearing <= 22.5 || rBearing > 337.5) {
            dirDisp += "正北";
        } else if (rBearing > 22.5 && rBearing <= 67.5) {
            dirDisp += "东北";
        } else if (rBearing > 67.5 && rBearing <= 112.5){
            dirDisp += "正东";
        } else if (rBearing > 112.5 && rBearing <= 157.5){
            dirDisp += "东南";
        } else if (rBearing > 157.5 && rBearing <= 202.5){
            dirDisp += "正南";
        } else if (rBearing > 202.5 && rBearing <= 247.5){
            dirDisp += "西南";
        } else if (rBearing > 247.5 && rBearing <= 292.5){
            dirDisp += "正西";
        } else if (rBearing > 292.5 && rBearing <= 337.5){
            dirDisp += "西北";
        }

        spdText.setText(spdDisp);
        brnText.setText(dirDisp);
    }

    @Override
    public void onDestroy() {
        RepMap.clear();
        if (RepTraceLine != null) {
            RepTraceLine.remove();
        }
        RepMapView.onDestroy();
        releaseWakeLock();
        finish();
        super.onDestroy();
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


    public void drawChart(){
        ChartEntries = new ArrayList<Entry>();

        if (FileReadiness) {
            for (int i = 0; i < PtsCount; i++) {
                try {
                    JSONObject tmpChtObj = lineARY.getJSONObject(i);
                    Entry tmpEntry = new Entry(tmpChtObj.getInt("ID"), (float) tmpChtObj.getDouble("Distance"));
                    ChartEntries.add(tmpEntry);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            ChartEntries.add(new Entry(0,0));
        }
        ChartDataSet = new LineDataSet(ChartEntries,"距离");
        ChartDataSet.setDrawCircles(false);
        ChartDataSet.setLineWidth(5);
        ChartData = new LineData(ChartDataSet);
        RepChart.setData(ChartData);
        RepChart.setDragDecelerationEnabled(false);
        RepChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        RepChart.setBackgroundColor(getResources().getColor(R.color.leaf));
        RepChart.getAxisLeft().setAxisMinimum(0);
        RepChart.invalidate();

    }

    public void drawChartMarker(){
        JSONObject tempObj;

        if (ChartPointed) {
            markerDataSet.removeLast();
            ChartPointed = false;
        }

        markerEntries  = new ArrayList<Entry>();
        try {
            tempObj = lineARY.getJSONObject(infoPos);
            markerEntries.add(new Entry(tempObj.getInt("ID"), (float) tempObj.getDouble("Distance")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        markerDataSet = new LineDataSet(markerEntries,"");
        markerDataSet.setCircleRadius(9);
        markerDataSet.setCircleColor(R.color.black);
        ChartDataSet.setDrawCircles(false);

        ChartData.addDataSet(markerDataSet);
        ChartPointed = true;
        markerDataSet.notifyDataSetChanged();
        ChartData.notifyDataChanged();
        RepChart.notifyDataSetChanged();
        RepChart.invalidate();

    }

    @Override
    public void onBackPressed(){
        if (Playing) {
            Toast.makeText(this, "正在回放！", Toast.LENGTH_SHORT).show();
        } else {
            finish();
            super.onBackPressed();
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
