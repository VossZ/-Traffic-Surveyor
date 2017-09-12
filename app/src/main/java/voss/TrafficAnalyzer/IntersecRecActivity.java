package voss.TrafficAnalyzer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class IntersecRecActivity extends AppCompatActivity {
    private TextView textName, textDirection, text00, text01, text10, text11, textCountDownMin,
            textCountDownSec, dispL, dispN, dispR;
    // 00：直行 01：右转 10：左转 11：左转控制
    private Button btnSave;
    private ImageButton btnHelp, btnL, btnN, btnR;
    private ImageView arrowImage;
    private String mName, directionCode, fileName;
    private int mDirection, Linfo, Ninfo, Rinfo, countL, countN, countR, CDmin, CDsec,
            tmpSelection, recMin, recSec, timeMil, tmpMin,tmpSec,tmpL,tmpN,tmpR, timingL,
            timingN, timingR;
    private boolean turnControl, Counting, Saved, Located;
    private EditText inputnameText, inputMin, inputSec, inputFileName;
    private Timer timer;
    private Handler handler;
    private DecimalFormat timeFormat, countFormat;
    private AlertDialog.Builder sbuilder,dbuilder;
    private Integer[] DirectionUsed;
    private File folder, outputFile;
    private SimpleDateFormat TimeForm;
    private JSONObject outputObj, tmpObj;
    private JSONArray LtmpAry, NtmpAry, RtmpAry, LtimAry, NtimAry, RtimAry;
    private long baseTime, timingL0, timingN0, timingR0;
    private double Lat, Lon;

    private Vibrator vibe;
    private PowerManager.WakeLock wakeLock;

    public AMapLocationClient mLocationClient;
    public AMapLocationListener mLocationListener;
    public AMapLocationClientOption mLocationOption;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intersec_rec);



        textName = (TextView)findViewById(R.id.textName);
        textDirection = (TextView)findViewById(R.id.textDirection);
        text00 = (TextView)findViewById(R.id.textL00);
        text01 = (TextView)findViewById(R.id.textL01);
        text10 = (TextView)findViewById(R.id.textL10);
        text11 = (TextView)findViewById(R.id.textL11);
        textCountDownMin = (TextView)findViewById(R.id.textCountDownMin);
        textCountDownSec = (TextView)findViewById(R.id.textCountDownSec);
        dispL = (TextView)findViewById(R.id.lTurnNum);
        dispN = (TextView)findViewById(R.id.nTurnNum);
        dispR = (TextView)findViewById(R.id.rTurnNum);
        btnSave = (Button)findViewById(R.id.saveBtn);
        btnHelp = (ImageButton) findViewById(R.id.helpBtn);
        btnL = (ImageButton) findViewById(R.id.lTurnBtn);
        btnN = (ImageButton) findViewById(R.id.nTurnBtn);
        btnR = (ImageButton) findViewById(R.id.rTurnBtn);
        arrowImage = (ImageView)findViewById(R.id.arrowImage);

        vibe = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        timeFormat = new DecimalFormat("00");
        countFormat = new DecimalFormat("000");
        tmpObj = new JSONObject();
        outputObj = new JSONObject();
        folder = new File(Environment.getExternalStorageDirectory() + "/Surveyor/");
        TimeForm = new SimpleDateFormat("yyyy年MM月dd日_HH时mm分ss秒");
        DirectionUsed = null;
        directionCode = "N";
        Saved = true;

        mLocationListener = new AMapLocationListener(){
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {


                        Lat = amapLocation.getLatitude();
                        Lon = amapLocation.getLongitude();
                        Located = true;
                    }
                }
            }
        };
        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationListener(mLocationListener);
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setOnceLocationLatest(true);




        acquireWakeLock();


        textName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Counting) {
                    inputnameText = new EditText(IntersecRecActivity.this);
                    if (mName!=null){
                        inputnameText.setText(mName);
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(IntersecRecActivity.this);
                    builder.setTitle("请输入路口名");
                    builder.setView(inputnameText);
                    builder.setCancelable(true);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mName != inputnameText.getText().toString()){
                                Saved = false;
                            }
                            mName = inputnameText.getText().toString();
                            textName.setText(mName);
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.create().show();
                } else {
                    Toast.makeText(IntersecRecActivity.this, "正在统计中！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        textDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Counting) {

                    dbuilder = new AlertDialog.Builder(IntersecRecActivity.this);
                    dbuilder.setCancelable(true);
                    dbuilder.setTitle("此方向已统计过");

                    dbuilder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setDir();

                            outputObj.remove(directionCode);

                            countL = 0;
                            countN = 0;
                            countR = 0;
                            dispL.setText("000");
                            dispN.setText("000");
                            dispR.setText("000");
                        }
                    });
                    dbuilder.setNegativeButton("否", null);

                    sbuilder = new AlertDialog.Builder(IntersecRecActivity.this);
                    sbuilder.setCancelable(true);
                    sbuilder.setTitle("是否切换方向？");
                    sbuilder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setDir();


                            countL = 0;
                            countN = 0;
                            countR = 0;
                            dispL.setText("000");
                            dispN.setText("000");
                            dispR.setText("000");
                        }
                    });
                    sbuilder.setNegativeButton("否", null);


                    AlertDialog.Builder builder = new AlertDialog.Builder(IntersecRecActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle("请选择方向");
                    builder.setSingleChoiceItems(R.array.intersecDir, tmpSelection, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tmpSelection = which;

                        }
                    });
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (DirectionUsed != null && Arrays.asList(DirectionUsed).contains(tmpSelection)) {

                                        try {
                                            String[] tmpary = getResources().getStringArray(R.array.intersecDirS);
                                            tmpObj = new JSONObject();
                                            tmpObj = outputObj.getJSONObject(tmpary[tmpSelection]);
                                            tmpMin = tmpObj.getInt("Min");
                                            tmpSec = tmpObj.getInt("Sec");
                                            tmpL = tmpObj.getInt("LTurn");
                                            tmpN = tmpObj.getInt("NTurn");
                                            tmpR = tmpObj.getInt("RTurn");

                                            dbuilder.setMessage("是否重新统计？（时长：" + tmpMin + "分 " + tmpSec +
                                                    "秒，左转：" + tmpL + "，直行：" + tmpN + ",右转：" + tmpR + " ）");
                                            dbuilder.create().show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }


                                    }else if (countL == 0 && countN == 0 && countR ==0){
                                        setDir();
                                    } else {
                                        sbuilder.create().show();
                                    }
                                }
                            }
                    );
                    builder.setNegativeButton("取消", null);
                    builder.create().show();
                } else {
                    Toast.makeText(IntersecRecActivity.this, "正在统计中！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 00：直行 01：右转 10：左转 11：左转控制

        text00.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Counting) {
                    tmpSelection = 0;
                    String[] tmpCount = {0 + "", 1 + "", 2 + "", 3 + "", 4 + "", 5 + "", 6 + ""};
                    AlertDialog.Builder builder = new AlertDialog.Builder(IntersecRecActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle("请选择直行车道数量");
                    builder.setSingleChoiceItems(tmpCount, 1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tmpSelection = which;
                        }
                    });
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Ninfo!=tmpSelection) {
                                Saved = false;
                            }
                            Ninfo = tmpSelection;
                            text00.setText(Ninfo + "");
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.create().show();
                } else {
                    Toast.makeText(IntersecRecActivity.this, "正在统计中！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        text01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Counting) {
                    tmpSelection = 0;
                    String[] tmpCount = {0 + "", 1 + "", 2 + "", 3 + ""};
                    AlertDialog.Builder builder = new AlertDialog.Builder(IntersecRecActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle("请选择右转车道数量");
                    builder.setSingleChoiceItems(tmpCount, 1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tmpSelection = which;
                            Toast.makeText(IntersecRecActivity.this, tmpSelection + "", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Rinfo!=tmpSelection) {
                                Saved = false;
                            }
                            Rinfo = tmpSelection;
                            text01.setText(Rinfo + "");
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.create().show();
                } else {
                    Toast.makeText(IntersecRecActivity.this, "正在统计中！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        text10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Counting) {
                    tmpSelection = 0;
                    String[] tmpCount = {0 + "", 1 + "", 2 + "", 3 + ""};
                    AlertDialog.Builder builder = new AlertDialog.Builder(IntersecRecActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle("请选择左转车道数量");
                    builder.setSingleChoiceItems(tmpCount, 1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tmpSelection = which;
                        }
                    });
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Linfo!=tmpSelection) {
                                Saved = false;
                            }
                            Linfo = tmpSelection;
                            text10.setText(Linfo + "");
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.create().show();
                } else {
                    Toast.makeText(IntersecRecActivity.this, "正在统计中！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        text11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Counting) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(IntersecRecActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle("请选择左转控制状态");
                    builder.setPositiveButton("有", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!turnControl){

                                Saved = false;
                            }
                            turnControl = true;
                            text11.setText("有");
                        }
                    });
                    builder.setNeutralButton("取消", null);
                    builder.setNegativeButton("无", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(turnControl){

                                Saved = false;
                            }
                            turnControl = false;
                            text11.setText("无");
                        }
                    });
                    builder.create().show();
                } else {
                    Toast.makeText(IntersecRecActivity.this, "正在统计中！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 00：直行 01：右转 10：左转 11：左转控制

        textCountDownMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Counting) {
                    inputMin = new EditText(IntersecRecActivity.this);
                    inputMin.setHint("分钟（0~99）");
                    AlertDialog.Builder builder = new AlertDialog.Builder(IntersecRecActivity.this);
                    builder.setTitle("请输入倒计时分钟数");
                    builder.setView(inputMin);
                    builder.setCancelable(true);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                CDmin = Integer.parseInt(inputMin.getText().toString());
                            } catch (Exception e) {
                                CDmin = 0;
                            }
                            if (CDmin >= 0 && CDmin <= 99) {
                                textCountDownMin.setText("" + timeFormat.format(CDmin));
                                recMin = CDmin;
                            } else {
                                Toast.makeText(IntersecRecActivity.this, "请输入正确的分钟数", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.create().show();
                } else {
                    Toast.makeText(IntersecRecActivity.this, "正在统计中！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        textCountDownSec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Counting) {
                    inputSec = new EditText(IntersecRecActivity.this);
                    inputSec.setHint("秒（0~59）");
                    AlertDialog.Builder builder = new AlertDialog.Builder(IntersecRecActivity.this);
                    builder.setTitle("请输入倒计时秒数");
                    builder.setView(inputSec);
                    builder.setCancelable(true);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                CDsec = Integer.parseInt(inputSec.getText().toString());
                            } catch (Exception e) {
                                CDsec = 0;
                            }
                            if (CDsec >= 0 && CDsec < 60) {
                                textCountDownSec.setText("" + timeFormat.format(CDsec));
                                recSec = CDsec;
                            } else {
                                Toast.makeText(IntersecRecActivity.this, "请输入正确的秒数！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.create().show();
                } else {
                    Toast.makeText(IntersecRecActivity.this, "正在统计中！", Toast.LENGTH_SHORT).show();
                }
            }
        });




        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Counting){
                    if (mName != null) {

                        fileName = TimeForm.format(new Date());

                        try {
                            tmpObj = new JSONObject();
                            tmpObj.put("Name", mName);
                            tmpObj.put("nLanes", Ninfo);
                            tmpObj.put("rLanes", Rinfo);
                            tmpObj.put("lLanes", Linfo);
                            tmpObj.put("turnConrtol", turnControl);
                            tmpObj.put("Date", fileName);
                            tmpObj.put("Lat", Lat);
                            tmpObj.put("Lon", Lon);

                            outputObj.put("Info", tmpObj);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        fileName = mName + "_" + fileName;

                        inputFileName = new EditText(IntersecRecActivity.this);
                        inputFileName.setText(fileName);
                        AlertDialog.Builder builder = new AlertDialog.Builder(IntersecRecActivity.this);
                        builder.setTitle("请输入文件名");
                        builder.setView(inputFileName);
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                outputObj.remove("Info");
                            }
                        });
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                fileName = inputFileName.getText().toString();
                                saveFile();
                            }
                        });
                        builder.setCancelable(true);
                        builder.show();


                    } else {
                        Toast.makeText(IntersecRecActivity.this, "未输入路口名！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(IntersecRecActivity.this, "正在统计中！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Counting){

                    AlertDialog.Builder builder = new android.app.AlertDialog.Builder(IntersecRecActivity.this);
                    builder.setTitle("说明");
                    builder.setCancelable(true);
                    builder.setItems(getResources().getStringArray(R.array.intersecRecInfo), null);
                    builder.setNegativeButton("返回", null);
                    builder.create().show();

                } else {
                    Toast.makeText(IntersecRecActivity.this, "正在统计中！", Toast.LENGTH_SHORT).show();
                }            }
        });
        btnL.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        startTiming();
                        if (Counting) {

                            timingL0 = SystemClock.elapsedRealtime();

                            timeMil = (int) (timingL0 - baseTime);

                            LtmpAry.put(timeMil);

                            countL++;
                            vibe.vibrate(75);

                            dispL.setText(countFormat.format(countL) + "");
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        if (Counting) {
                            timingL = (int) (SystemClock.elapsedRealtime() - timingL0);
                            LtimAry.put(timingL);
                        }
                        break;
                }

                return false;
            }
        });

        btnN.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        startTiming();
                        if (Counting) {

                            timingN0 = SystemClock.elapsedRealtime();

                            timeMil = (int) (timingN0 - baseTime);
                            NtmpAry.put(timeMil);

                            countN++;
                            vibe.vibrate(75);

                            dispN.setText(countFormat.format(countN) + "");
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        if (Counting) {
                            timingN = (int) (SystemClock.elapsedRealtime() - timingN0);
                            NtimAry.put(timingN);
                        }
                        break;
                }

                return false;
            }
        });

        btnR.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        startTiming();
                        if (Counting) {

                            timingR0 = SystemClock.elapsedRealtime();

                            timeMil = (int) (timingR0 - baseTime);

                            RtmpAry.put(timeMil);

                            countR++;vibe.vibrate(75);

                            dispR.setText(countFormat.format(countR) + "");
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        if (Counting) {
                            timingR = (int) (SystemClock.elapsedRealtime() - timingR0);
                            RtimAry.put(timingR);
                        }
                        break;
                }

                return false;
            }
        });

    }

    @Override
    public void onBackPressed(){
        if (Counting){
            Toast.makeText(IntersecRecActivity.this, "正在统计中！", Toast.LENGTH_SHORT).show();
        }else if(!Saved){
            releaseWakeLock();
            AlertDialog.Builder builder = new AlertDialog.Builder(IntersecRecActivity.this);
            builder.setTitle("是否退出？");
            builder.setMessage("有未保存的变更，是否真的要退出？");
            builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    IntersecRecActivity.this.finish();
                }
            });
            builder.setCancelable(true);
            builder.setNegativeButton("否", null);
            builder.create().show();
        } else {
            super.onBackPressed();

        }
    }

    public void startTiming(){
        if (CDmin == 0 && CDsec ==0 && !Counting) {
            Toast.makeText(this, "已经结束", Toast.LENGTH_SHORT).show();
        } else if  (!Counting){
            Counting = true;
            Saved = false;
            timer = new Timer();
            handler = new Handler();
            baseTime = SystemClock.elapsedRealtime();
            LtmpAry = new JSONArray();
            NtmpAry = new JSONArray();
            RtmpAry = new JSONArray();
            LtimAry = new JSONArray();
            NtimAry = new JSONArray();
            RtimAry = new JSONArray();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (CDmin == 0 && CDsec == 0) {
                                Counting = false;
                                vibe.vibrate(1500);

                                if (DirectionUsed != null) {
                                    DirectionUsed = Arrays.copyOf(DirectionUsed, DirectionUsed.length + 1);
                                    DirectionUsed[DirectionUsed.length - 1] = mDirection;
                                } else {
                                    DirectionUsed = new Integer[]{mDirection};
                                }

                                try {

                                    if (LtmpAry.length() > LtimAry.length()){
                                        LtmpAry.remove(LtimAry.length());
                                    } else if (LtmpAry.length() < LtimAry.length()){
                                        LtimAry.remove(LtmpAry.length());
                                    }
                                    if (NtmpAry.length() != NtimAry.length()){
                                        NtmpAry.remove(NtimAry.length());
                                    } else if (NtmpAry.length() < NtimAry.length()){
                                        NtimAry.remove(NtmpAry.length());
                                    }
                                    if (RtmpAry.length() != RtimAry.length()){
                                        RtmpAry.remove(RtimAry.length());
                                    } else if (RtmpAry.length() < RtimAry.length()){
                                        RtimAry.remove(RtmpAry.length());
                                    }

                                    tmpObj = new JSONObject();
                                    tmpObj.put("LTurn", countL);
                                    tmpObj.put("NTurn", countN);
                                    tmpObj.put("RTurn", countR);
                                    tmpObj.put("Min", recMin);
                                    tmpObj.put("Sec", recSec);
                                    tmpObj.put("LPoints", LtmpAry);
                                    tmpObj.put("LDetails",LtimAry);
                                    tmpObj.put("NPoints", NtmpAry);
                                    tmpObj.put("NDetails", NtimAry);
                                    tmpObj.put("RPoints", RtmpAry);
                                    tmpObj.put("RDetails", RtimAry);


                                    outputObj.put(directionCode, tmpObj);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                recMin = 0;
                                recSec = 0;

                                timer.cancel();
                            } else if (CDsec == 0) {
                                CDmin--;
                                CDsec = 59;
                                textCountDownMin.setText(timeFormat.format(CDmin) + "");
                                textCountDownSec.setText(timeFormat.format(CDsec) + "");
                            } else {
                                CDsec--;
                                textCountDownSec.setText(timeFormat.format(CDsec) + "");
                            }

                        }
                    });
                }
            }, 0, 1000);

            if (!Located) {
                mLocationClient.setLocationOption(mLocationOption);
                mLocationClient.startLocation();
            }
        }
    }

    public void saveFile(){
        if (!folder.exists()) {
            folder.mkdirs();
        }
        outputFile = new File(folder + "/" + fileName + ".ilog.json");
        try {
            FileOutputStream FOS = new FileOutputStream(outputFile);
            FOS.write(outputObj.toString().getBytes());
            FOS.close();
            Toast.makeText(this, "已保存至：" + outputFile, Toast.LENGTH_SHORT).show();
            Saved = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setDir(){
        mDirection = tmpSelection;
        String[] tmpAry0 = getResources().getStringArray(R.array.intersecDir);
        textDirection.setText(tmpAry0[mDirection]);
        String[] tmpAry1 = getResources().getStringArray(R.array.intersecDirS);

        switch (mDirection){
            case 0:
                arrowImage.setImageDrawable(getDrawable(R.drawable.intersec_arrow_n));
                break;
            case 1:
                arrowImage.setImageDrawable(getDrawable(R.drawable.intersec_arrow_e));
                break;
            case 2:
                arrowImage.setImageDrawable(getDrawable(R.drawable.intersec_arrow_s));
                break;
            case 3:
                arrowImage.setImageDrawable(getDrawable(R.drawable.intersec_arrow_w));
                break;
        }
        directionCode = tmpAry1[mDirection];
        Saved = false;
        tmpSelection = 0;

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
    @Override
    public void onDestroy(){
        finish();
        super.onDestroy();
    }

}
