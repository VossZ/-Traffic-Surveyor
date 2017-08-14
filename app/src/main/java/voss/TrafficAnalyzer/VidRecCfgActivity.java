package voss.TrafficAnalyzer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class VidRecCfgActivity extends AppCompatActivity {
    private Button posBtn, negBtn, L1Btn;
    private TextView textL1;
    private JSONObject cfgObj;
    private AlertDialog resMenu;
    private AlertDialog.Builder resMenuBuilder;
    private String[] CFGItems, resItems;
    private int tmpValue, resCfgValue;
    private File filePos;
    private StringBuilder stringBuilder;
    private String tempString;
    private FileOutputStream FOS;
    private int[] cfgValues;
    private boolean cfgVersion;
    private byte[] output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid_rec_cfg);

        posBtn = (Button)findViewById(R.id.confirmVRC);
        negBtn = (Button)findViewById(R.id.cancelVRC);
        textL1 = (TextView)findViewById(R.id.textViewL1);
        L1Btn = (Button)findViewById(R.id.buttonL1);

        resItems = getResources().getStringArray(R.array.resList);
        CFGItems = getResources().getStringArray(R.array.vidRecCfgList);



        filePos = new File(Environment.getExternalStorageDirectory() + "/Surveyor/Config/");
        if (!filePos.exists()){
            filePos.mkdirs();
        }
        filePos = new File(filePos + "/VidRec.cfg");
        if (!filePos.exists()) {
            createCFG();
        } else {
            readCFG();
        }
        if (!cfgVersion) {
            createCFG();
        }




        L1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resCfg();
            }
        });




        posBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cfgValues = new int[]{resCfgValue};
                writeCFG(cfgValues);

                Intent intent = new Intent();
                intent.setClass(VidRecCfgActivity.this, VidRecordActivity.class);
                startActivity(intent);
            }
        });

        negBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(VidRecCfgActivity.this, VidRecordActivity.class);
                startActivity(intent);
            }
        });
    }


//设置项：1.分辨率  2.


    public void createCFG() {
        int[] tmpValues;
        tmpValues = new int[CFGItems.length-1];
        for (int i = 0; i < tmpValues.length; i ++){
            tmpValues[i] = 0;
        }
        writeCFG(tmpValues);
        readCFG();


    }

    public void writeCFG(int[] Values){
        try {
            cfgObj = new JSONObject();
            filePos.createNewFile();
            for (int i = 0; i < Values.length; i++){
                cfgObj.put(CFGItems[i], Values[i]);
            }
            cfgObj.put("Version", getResources().getString(R.string.vercode));

            output = cfgObj.toString().getBytes();
            FOS = new FileOutputStream(filePos);
            FOS.write(output);
            FOS.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }






    }

    public void readCFG(){
        try {
            InputStream is = new FileInputStream(filePos);
            InputStreamReader streamReader = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(streamReader);
            String line = null;
            stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            tempString = stringBuilder.toString();
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            cfgObj = new JSONObject(tempString);

            if (!cfgObj.getString("Version").equals(getResources().getString(R.string.vercode))){
                cfgVersion = false;
                return;
            } else {
                cfgVersion = true;
            }

            resCfgValue = cfgObj.getInt(CFGItems[0]);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        textL1.setText(resItems[resCfgValue]);

    }

    public void resCfg(){
        resMenuBuilder = new AlertDialog.Builder(VidRecCfgActivity.this);
        resMenuBuilder.setTitle("设置分辨率");
        resMenuBuilder.setCancelable(true);
        resMenuBuilder.setSingleChoiceItems(resItems, resCfgValue, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tmpValue = which;
            }
        });
        resMenuBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resCfgValue = tmpValue;
                Toast.makeText(VidRecCfgActivity.this, "已设置分辨率为：" + resItems[resCfgValue]
                        , Toast.LENGTH_SHORT).show();
                textL1.setText(resItems[resCfgValue]);
            }
        });
        resMenu = resMenuBuilder.create();
        resMenu.show();

    }

}
