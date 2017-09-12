package voss.TrafficAnalyzer;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.trace.TraceOverlay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VidRecordActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private TextureMapView mMapView;
    private AMap aMap;
    private AMapLocationClientOption mLocationOption;
    private AMapLocationClient mLocationClient;
    private AMapLocation MapLocation;
    private String userFileName;
    private File FileName;
    private File Folder;
    private File VideoFile;
    private List<LatLng> locList;
    private AMapLocationListener mLocationListener;
    private Handler TimerHandler = new Handler();
    private Runnable Timer = null;
    private Timer nTimer;
    private Handler nHandler;
    private JSONObject jsObj;
    private JSONArray jsAry, pointsAry;
    private FileOutputStream FOS;
    private boolean Started, Noted, mStartedFlg;
    private ImageButton StartBtn, CfgBtn, InfoBtn;
    private double Lat, Lon, Dist;
    private float Spd, Brn;
    private String Time, dirDisp, spdDisp;
    private int Note, tmpNote;
    private int DataID, PointsID;
    private int resCfgValue;
    private byte[] output;
    private SurfaceView nCameraView;
    private Polyline nTraceLine;
    private LatLng latLng;
    private MarkerOptions markerOptions;
    private Marker marker;
    private EditText inputDialog;
    private Camera nCamera;
    private Camera.Parameters nParameters;
    private Camera.AutoFocusCallback mAutoFocusCallback=null;
    private SurfaceHolder mSurfaceHolder;
    private MediaRecorder mRecorder;
    private Button Note1Btn,Note2Btn, Note3Btn, Note4Btn, Note5Btn, MenuBtn;
    private List<Camera.Size> videoSizeList;
    private Chronometer nChronometer;
    private long exitTime;
    private TextView dirText, spdText;
    private TraceOverlay nTrace;
    private PowerManager.WakeLock wakeLock;
    private android.app.AlertDialog.Builder Dialog1B, Dialog2B, Dialog3B, Dialog4B, Dialog5B;
    private String[] Dialog1List, Dialog2List, Dialog3List, Dialog4List, Dialog5List, CFGItems;
    private CamcorderProfile vidQuality;


    private android.app.AlertDialog ChangeLogDialog, ChangeLogHistDialog, infoDialog;
    private android.app.AlertDialog.Builder CLDBuilder, CLHDBuilder, infoDialogBuilder;

    private SimpleDateFormat TimeForm = new SimpleDateFormat("yyyy年MM月dd日_HH时mm分ss秒");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_vid_record);

        //if (Build.VERSION.SDK_INT >= 23) {initPerm();}

        nTimer = new Timer();
        nHandler = new Handler();
        mMapView = (TextureMapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);// 此方法必须重写
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        StartBtn = (ImageButton) findViewById(R.id.startBtn);
        CfgBtn = (ImageButton) findViewById(R.id.cfgBtn);
        InfoBtn = (ImageButton)findViewById(R.id.infoBtn);
        MenuBtn = (Button) findViewById(R.id.menuBtn);
        Note1Btn = (Button) findViewById(R.id.note1Btn);
        Note2Btn = (Button) findViewById(R.id.note2Btn);
        Note3Btn = (Button) findViewById(R.id.note3Btn);
        Note4Btn = (Button) findViewById(R.id.note4Btn);
        //Note5Btn = (Button) findViewById(R.id.note5Btn);
        nChronometer = (Chronometer) findViewById(R.id.chronometer);
        dirText = (TextView) findViewById(R.id.dirText);
        spdText = (TextView) findViewById(R.id.spdText);
//1:标线 2:标志 3:设施 4:特殊
        Dialog1List = getResources().getStringArray(R.array.dialoglist1);
        Dialog2List = getResources().getStringArray(R.array.dialoglist2);
        Dialog3List = getResources().getStringArray(R.array.dialoglist3);
        Dialog4List = getResources().getStringArray(R.array.dialoglist4);
        //Dialog5List = getResources().getStringArray(R.array.dialoglist5);

        nCameraView =  (SurfaceView)findViewById(R.id.cameraView);




        mSurfaceHolder = nCameraView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mSurfaceHolder.addCallback(this); // holder加入回调接口

        Folder = new File(Environment.getExternalStorageDirectory() + "/Surveyor/");
        Started = false;
        jsObj = null;
        jsAry = null;
        pointsAry = null;
        FOS = null;
        markerOptions = null;
        FileName = null;



        readCFG();
        prepareInfo();

        mAutoFocusCallback=new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if(success){
                    Log.i("focus", "true");
                }
            }
        };

        mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                MapLocation = aMapLocation;

            }
        };



        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        // 连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(1000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW) ;//连续定位、且将视角移动到地图中心点，定位蓝点跟随设备移动。（1秒1次定位）
//aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.getUiSettings().setTiltGesturesEnabled(false);


        mLocationClient = new AMapLocationClient(getApplicationContext());
//设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        initLocOpt();

        mLocationClient.startLocation();

        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));


        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if (!Folder.exists()) {
            Folder.mkdirs();
        }

// 1 信号灯，2 让行，3 禁停，4 行人，5 摄像头
        Note1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Started && !Noted) {
                    Dialog1B = new android.app.AlertDialog.Builder(VidRecordActivity.this);
                    Dialog1B.setSingleChoiceItems(Dialog1List, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tmpNote = which;
                        }
                    });
                    Dialog1B.setCancelable(true);
                    Dialog1B.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Note = 10 + tmpNote;
                            Noted = true;
                            Toast.makeText(VidRecordActivity.this, "已标记为：" + Dialog1List[tmpNote], Toast.LENGTH_SHORT).show();
                        }
                    });
                    Dialog1B.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(VidRecordActivity.this, "取消标记", Toast.LENGTH_SHORT).show();
                        }
                    });
                    android.app.AlertDialog tmpDialog = Dialog1B.create();
                    tmpDialog.show();
                } else if (!Started){
                    Toast.makeText(VidRecordActivity.this, "未开始录制", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Note2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Started && !Noted) {
                    Dialog2B = new android.app.AlertDialog.Builder(VidRecordActivity.this);
                    Dialog2B.setSingleChoiceItems(Dialog2List, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tmpNote = which;
                        }
                    });
                    Dialog2B.setCancelable(true);
                    Dialog2B.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Note = 20 + tmpNote;
                            Noted = true;
                            Toast.makeText(VidRecordActivity.this, "已标记为：" + Dialog2List[tmpNote], Toast.LENGTH_SHORT).show();
                        }
                    });
                    Dialog2B.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(VidRecordActivity.this, "取消标记", Toast.LENGTH_SHORT).show();
                        }
                    });
                    android.app.AlertDialog tmpDialog = Dialog2B.create();
                    tmpDialog.show();
                } else if (!Started){
                    Toast.makeText(VidRecordActivity.this, "未开始录制", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Note3Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Started && !Noted) {
                    Dialog3B = new android.app.AlertDialog.Builder(VidRecordActivity.this);
                    Dialog3B.setSingleChoiceItems(Dialog3List, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tmpNote = which;
                        }
                    });
                    Dialog3B.setCancelable(true);
                    Dialog3B.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Note = 30 + tmpNote;
                            Noted = true;
                            Toast.makeText(VidRecordActivity.this, "已标记为：" + Dialog3List[tmpNote], Toast.LENGTH_SHORT).show();
                        }
                    });
                    Dialog3B.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(VidRecordActivity.this, "取消标记", Toast.LENGTH_SHORT).show();
                        }
                    });
                    android.app.AlertDialog tmpDialog = Dialog3B.create();
                    tmpDialog.show();
                } else if (!Started){
                    Toast.makeText(VidRecordActivity.this, "未开始录制", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Note4Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Started && !Noted) {
                Dialog4B = new android.app.AlertDialog.Builder(VidRecordActivity.this);
                Dialog4B.setSingleChoiceItems(Dialog4List, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tmpNote = which;
                    }
                });
                Dialog4B.setCancelable(true);
                Dialog4B.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Note = 20 + tmpNote;
                        Noted = true;
                        Toast.makeText(VidRecordActivity.this, "已标记为：" + Dialog4List[tmpNote], Toast.LENGTH_SHORT).show();
                    }
                });
                Dialog4B.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(VidRecordActivity.this, "取消标记", Toast.LENGTH_SHORT).show();
                    }
                });
                android.app.AlertDialog tmpDialog = Dialog4B.create();
                tmpDialog.show();
            } else if (!Started){
                Toast.makeText(VidRecordActivity.this, "未开始录制", Toast.LENGTH_SHORT).show();
            }
            }
        });



        MenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Started){
                    Intent intent = new Intent();
                    intent.setClass(VidRecordActivity.this, VidReplayActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(VidRecordActivity.this, "正在录制！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        CfgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!Started) {
                    Intent intent = new Intent();
                    intent.setClass(VidRecordActivity.this, VidRecCfgActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(VidRecordActivity.this, "正在录制！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        InfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!Started) {
                    //check prepareInfo
                    ChangeLogHistDialog = CLHDBuilder.create();
                    ChangeLogDialog = CLDBuilder.create();
                    infoDialog = infoDialogBuilder.create();

                    infoDialog.show();
                } else {
                    Toast.makeText(VidRecordActivity.this, "正在录制！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        acquireWakeLock();
    }


    @Override
    protected void onDestroy() {
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        File tempfile = new File(Folder + "/temp.mp4");
        if (tempfile.exists()){
            tempfile.delete();
        }

        TimerHandler.removeCallbacks(Timer);
        releaseWakeLock();
        finish();
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
        TimerHandler.postDelayed(Timer, 2000);
        acquireWakeLock();

    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();

        TimerHandler.removeCallbacks(Timer);
        releaseWakeLock();

    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
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
            ActivityCompat.requestPermissions(VidRecordActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            }, 101);
        }
    }

    public void initLocOpt(){
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setInterval(1000);
        mLocationOption.setMockEnable(true);
        mLocationClient.setLocationOption(mLocationOption);
    }

    private void startAction(){
        if (!Started) {
            Started = true;
            locList = new ArrayList<LatLng>();
            jsAry = new JSONArray();
            pointsAry = new JSONArray();
            jsObj = new JSONObject();
            output = null;
            FOS = null;
            initTimer();
            DataID = 0;
            PointsID = 0;
            Dist = 0;
            if (marker != null){
                marker.destroy();
            }



            if (mRecorder == null) {
                mRecorder = new MediaRecorder(); // Create MediaRecorder
            }
            try {
                nCamera.unlock();
                mRecorder.setCamera(nCamera);
                // Set audio and video source and encoder
                // 这两项需要放在setOutputFormat之前
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

                mRecorder.setProfile(vidQuality);



                mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());


                    Folder.mkdirs();
                    VideoFile = new File(Folder + "/temp.mp4");
                    mRecorder.setOutputFile(VideoFile.toString());
                    mRecorder.prepare();
                    mRecorder.start();   // Recording is now started
                    mStartedFlg = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
            nChronometer.setBase(SystemClock.elapsedRealtime());
            nChronometer.start();
        }
    }

    private void stopAction(){
        if (Started) {

            try {
                //下面三个参数必须加，不加的话会奔溃，在mediarecorder.stop();
                //报错为：RuntimeException:stop failed
                mRecorder.setOnErrorListener(null);
                mRecorder.setOnInfoListener(null);
                mRecorder.setPreviewDisplay(null);
                mRecorder.stop();
            } catch (IllegalStateException e) {
                Log.i("Exception", Log.getStackTraceString(e));
            }catch (RuntimeException e) {
                Log.i("Exception", Log.getStackTraceString(e));
            }catch (Exception e) {
                Log.i("Exception", Log.getStackTraceString(e));
            }
            mRecorder.reset();

            inputDialog = new EditText(this);
            inputDialog.setFocusable(true);
            inputDialog.setText(Time);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("请输入文件名");
            builder.setView(inputDialog);
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    userFileName = Time;
                    saveFile();
                }
            });
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    userFileName = inputDialog.getText().toString();
                    saveFile();
                }
            });
            builder.setCancelable(true);
            builder.show();

            if (nTimer != null) {
                nTimer.cancel();
                nTimer.purge();
            }

            if (mRecorder != null) {
                mRecorder.release();
                mRecorder = null;
            }
            nChronometer.stop();
            Started = false;
        }
    }

    private void initTimer(){
        nTimer = new Timer();
        nTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (Started){
                    nHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            getLoc();
                            dispInfo();
                            writeLog();
                            Note = 0;
                            Noted = false;

                        }
                    });
                }
            }
        }, 0, 1000);

    }

    private void getLoc(){
        if (MapLocation != null) {
            if (MapLocation.getLocationType() != 0) {
                Lat = MapLocation.getLatitude();
                Lon = MapLocation.getLongitude();
                Brn = MapLocation.getBearing();
                Spd = MapLocation.getSpeed();
            }
        }
        LatLng locTmp = latLng;

        latLng = new LatLng(Lat, Lon);

        Dist += AMapUtils.calculateLineDistance(locTmp, latLng);

        locList.add(latLng);

        /*
        if (nTraceLine != null){
            nTraceLine.remove();
        }

        nTraceLine = aMap.addPolyline((new PolylineOptions())
                .addAll(locList)
                .width(10)
                .color(Color.argb(255, 1, 1, 1)));
*/

        nTrace = new TraceOverlay(aMap, locList);

        if (Noted) {
           placePoint();
        }

    }




    private void writeLog(){
        Time = TimeForm.format(new java.util.Date());
        JSONObject tmpJS = new JSONObject();
        JSONObject tmpJS2 = new JSONObject();

        try {
            tmpJS.put("ID", DataID++);
            tmpJS.put("Lat", Lat);
            tmpJS.put("Lon", Lon);
            tmpJS.put("Bearing", Brn);
            tmpJS.put("Speed", Spd);
            tmpJS.put("Time", Time);
            tmpJS.put("Distance", Dist);
            jsAry.put(tmpJS);
            if (Noted) {
                tmpJS2.put("ID", PointsID++);
                tmpJS2.put("Type", Note);
                tmpJS2.put("Lat", Lat);
                tmpJS2.put("Lon", Lon);
                tmpJS2.put("Distance", Dist);
                pointsAry.put(tmpJS2);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void saveFile(){

        if (!Folder.exists()) {
            Folder.mkdirs();
        }
        FileName = new File(Folder + "/" + userFileName + ".vlog.json");
        Toast.makeText(this, "已保存为：" + FileName, Toast.LENGTH_SHORT).show();
        try {
            File file = VideoFile;
            VideoFile = new File(Folder + "/" + userFileName + ".vlog.mp4");
            file.renameTo(VideoFile);

            FileName.createNewFile();
            jsObj.put("MainTable", jsAry);
            jsObj.put("PointsTable", pointsAry);
            jsObj.put("VideoPath", "/Surveyor/" + userFileName + ".vlog.mp4");
            output = jsObj.toString().getBytes();
            FOS = new FileOutputStream(FileName);
            FOS.write(output);
            FOS.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void initCamera()
    {

        if(nCamera == null)
        {
            nCamera = Camera.open();
        }
        if(nCamera != null) {
            try {
                nParameters = nCamera.getParameters();
                nParameters.setPreviewSize(960, 544);
                nParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);


                nCamera.setParameters(nParameters);
                nCamera.setPreviewDisplay(mSurfaceHolder);
                nCamera.startPreview();

                Camera.Parameters parameter=nCamera.getParameters();
                videoSizeList = parameter.getSupportedVideoSizes();

//查找出最接近的视频录制分辨率


            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(VidRecordActivity.this, "初始化相机错误", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        initCamera();
        StartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Started){
                    stopAction();
                    StartBtn.setImageDrawable(getDrawable(android.R.drawable.ic_notification_overlay));
                } else {
                    startAction();
                    StartBtn.setImageDrawable(getDrawable(R.drawable.stop));
                }
            }
        });

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // surfaceDestroyed的时候同时对象设置为null
        nCameraView = null;
        mSurfaceHolder = null;
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    @Override
    public void onBackPressed(){
        if (Started) {
            Toast.makeText(this, "正在录制！", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }

    }

    public void dispInfo(){
        float convertedSpd = Spd * 3.6f;
        DecimalFormat decimalFormat=new DecimalFormat("000");
        spdDisp = "速度：" + decimalFormat.format(convertedSpd) + "km/h";

        dirDisp = Brn + "方向：";
        if (Brn == -1){
            dirDisp += "未确定方向";
        } else if (Brn <= 22.5 || Brn > 337.5) {
            dirDisp += "正北";
        } else if (Brn > 22.5 && Brn <= 67.5) {
            dirDisp += "东北";
        } else if (Brn > 67.5 && Brn <= 112.5){
            dirDisp += "正东";
        } else if (Brn > 112.5 && Brn <= 157.5){
            dirDisp += "东南";
        } else if (Brn > 157.5 && Brn <= 202.5){
            dirDisp += "正南";
        } else if (Brn > 202.5 && Brn <= 247.5){
            dirDisp += "西南";
        } else if (Brn > 247.5 && Brn <= 292.5){
            dirDisp += "正西";
        } else if (Brn > 292.5 && Brn <= 337.5){
            dirDisp += "西北";
        }

        spdText.setText(spdDisp);
        dirText.setText(dirDisp);

    }

    public void acquireWakeLock()
    {
        if (wakeLock == null)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock)
            {
                wakeLock.acquire();
            }
        }
    }

    public void releaseWakeLock()
    {
        if (wakeLock != null)
        {
            wakeLock.release();
            wakeLock = null;
        }
    }

    public void readCFG(){
        File cfgFile = new File(Environment.getExternalStorageDirectory()
                + "/Surveyor/Config/VidRec.cfg");
        if (!cfgFile.exists()) {
            resCfgValue = 0;
            Toast.makeText(this, "视频质量默认为480P，请设置视频质量", Toast.LENGTH_LONG).show();
            vidQuality = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
            return;
        }
        CFGItems = getResources().getStringArray(R.array.vidRecCfgList);
        String tempString = new String();
        try {
            InputStream is = new FileInputStream(cfgFile);
            InputStreamReader streamReader = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(streamReader);
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            tempString = stringBuilder.toString();
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject cfgObj = new JSONObject(tempString);

            if (!cfgObj.getString("Version").equals(getResources().getString(R.string.vercode))){
                resCfgValue = 0;
                Toast.makeText(this, "视频质量默认为480P", Toast.LENGTH_SHORT).show();
            } else {
                resCfgValue = cfgObj.getInt(CFGItems[0]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        switch (resCfgValue){
            case 0:
                vidQuality = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                Toast.makeText(this, "视频质量为480P", Toast.LENGTH_SHORT).show();

                break;
            case 1:
                vidQuality = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
                Toast.makeText(this, "视频质量为720P", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                vidQuality = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
                Toast.makeText(this, "视频质量为1080P", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    private void placePoint() {
        markerOptions = new MarkerOptions().position(latLng).draggable(false);
        switch (Note){
            case 10:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s10)));

                break;
            case 11:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s11)));

                break;
            case 12:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s12)));

                break;
            case 13:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s13)));

                break;
            case 14:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s14)));

                break;
            case 15:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s15)));

                break;
            case 20:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s20)));

                break;
            case 21:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s21)));

                break;
            case 22:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s22)));

                break;
            case 23:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s23)));

                break;
            case 24:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s24)));

                break;
            case 25:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s25)));

                break;
            case 30:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s30)));

                break;
            case 31:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s31)));

                break;
            case 32:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s32)));

                break;
            case 33:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s33)));

                break;
            case 34:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s34)));

                break;
            case 35:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s35)));

                break;
            case 40:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s40)));

                break;
            case 41:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s41)));

                break;
            case 42:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s42)));

                break;
            case 43:
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),R.drawable.s43)));

                break;

        }
        marker = aMap.addMarker(markerOptions);
    }

    public void prepareInfo(){
        CLHDBuilder = new android.app.AlertDialog.Builder(VidRecordActivity.this);
        CLHDBuilder.setTitle("历史记录");
        CLHDBuilder.setItems(getResources().getStringArray(R.array.changeloghist), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        CLHDBuilder.setNegativeButton("返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(VidRecordActivity.this, "返回录制", Toast.LENGTH_SHORT).show();
            }
        });

        CLDBuilder = new android.app.AlertDialog.Builder(VidRecordActivity.this);
        CLDBuilder.setTitle("更新内容");
        CLDBuilder.setMessage(getResources().getString(R.string.changelognew));
        CLDBuilder.setNegativeButton("返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(VidRecordActivity.this, "返回录制", Toast.LENGTH_SHORT).show();
            }
        });
        CLDBuilder.setPositiveButton("历史更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ChangeLogHistDialog.show();
            }
        });

        infoDialogBuilder = new android.app.AlertDialog.Builder(VidRecordActivity.this);
        infoDialogBuilder.setTitle("说明");
        infoDialogBuilder.setCancelable(true);
        infoDialogBuilder.setItems(getResources().getStringArray(R.array.vidRecInfo), null);
        infoDialogBuilder.setNegativeButton("返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(VidRecordActivity.this, "返回录制", Toast.LENGTH_SHORT).show();
            }
        });
        infoDialogBuilder.setPositiveButton("更新内容", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ChangeLogDialog.show();
            }
        });

        infoDialogBuilder.setCancelable(true);
        CLDBuilder.setCancelable(true);
        CLHDBuilder.setCancelable(true);



    }
}
