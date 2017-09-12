package voss.TrafficAnalyzer;

import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
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
import com.github.mikephil.charting.charts.LineChart;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class IntersecReplayActivity extends AppCompatActivity {

    private File mLogFile;
    private boolean FileReadiness, hasN, hasE, hasS, hasW;
    private LinearLayout tabIllu, tabDiag, frameIllu, frameDiag;
    private StringBuilder stringBuilder;
    private JSONObject jsOBJ, nObj, eObj, sObj, wObj, iObj;
    private int iNl, iNn, iNr, iEl, iEn, iEr, iSl, iSn, iSr, iWl, iWn, iWr,
            oN, oE, oS, oW, iN, iE, iS, iW, timeN, timeE, timeS, timeW;
    private JSONArray aNL0, aNL1, aNN0, aNN1, aNR0, aNR1, aEL0, aEL1, aEN0, aEN1, aER0, aER1,
            aSL0, aSL1, aSN0, aSN1, aSR0, aSR1, aWL0, aWL1, aWN0, aWN1, aWR0, aWR1;
    private TextView txtIN, txtINR, txtINN, txtINL, txtOW, txtIW, txtIWL, txtIWN, txtIWR,
            txtOS, txtON, txtIER, txtIEN, txtIEL, txtIE, txtOE, txtISL, txtISN, txtISR,
            txtIS, txtIntName, txtIntDate;
    private TextureMapView intMapView;
    private AMap intMap;
    private LatLng coord;
    private String name, date;
    private List<Entry> lNL, lNN, lNR, lEL, lEN, lER, lSL, lSN, lSR, lWL, lWN, lWR;
    private LineDataSet sNL, sNN, sNR, sEL, sEN, sER, sSL, sSN, sSR, sWL, sWN, sWR;
    private Entry tmpEntry0, tmpEntry1, tmpEntry2, tmpEntry3;
    private LineChart intersecChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intersec_replay);

        tabIllu = (LinearLayout)findViewById(R.id.tabIllu);
        tabDiag = (LinearLayout)findViewById(R.id.tabDiag);
        frameIllu = (LinearLayout)findViewById(R.id.frameIllu);
        frameDiag = (LinearLayout)findViewById(R.id.frameDiag);
        txtIN = (TextView)findViewById(R.id.txtIN);
        txtINR = (TextView)findViewById(R.id.txtINR);
        txtINN = (TextView)findViewById(R.id.txtINN);
        txtINL = (TextView)findViewById(R.id.txtINL);
        txtOW = (TextView)findViewById(R.id.txtOW);
        txtIW = (TextView)findViewById(R.id.txtIW);
        txtIWL = (TextView)findViewById(R.id.txtIWL);
        txtIWN = (TextView)findViewById(R.id.txtIWN);
        txtIWR = (TextView)findViewById(R.id.txtIWR);
        txtOS = (TextView)findViewById(R.id.txtOS);
        txtON = (TextView)findViewById(R.id.txtON);
        txtIER = (TextView)findViewById(R.id.txtIER);
        txtIEN = (TextView)findViewById(R.id.txtIEN);
        txtIEL = (TextView)findViewById(R.id.txtIEL);
        txtIE = (TextView)findViewById(R.id.txtIE);
        txtOE = (TextView)findViewById(R.id.txtOE);
        txtISL = (TextView)findViewById(R.id.txtISL);
        txtISN = (TextView)findViewById(R.id.txtISN);
        txtISR = (TextView)findViewById(R.id.txtISR);
        txtIS = (TextView)findViewById(R.id.txtIS);
        txtIntName = (TextView)findViewById(R.id.textIntName);
        txtIntDate = (TextView)findViewById(R.id.textIntDate);
        intMapView = (TextureMapView)findViewById(R.id.intersecMap);
        intersecChart = (LineChart)findViewById(R.id.intersecChart);

        intMapView.onCreate(savedInstanceState);
        if (intMap == null) {
            intMap = intMapView.getMap();
        }


        recvPath();
        //if (FileReadiness){
            unpackJSON(mLogFile);
            procData();
            setDisp();
        //}

        tabIllu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabIllu.setBackgroundColor(getResources().getColor(R.color.leaf));
                tabDiag.setBackgroundColor(getResources().getColor(R.color.darkLeaf));
                frameIllu.setVisibility(View.VISIBLE);
                frameDiag.setVisibility(View.GONE);

            }
        });

        tabDiag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabDiag.setBackgroundColor(getResources().getColor(R.color.leaf));
                tabIllu.setBackgroundColor(getResources().getColor(R.color.darkLeaf));
                frameDiag.setVisibility(View.VISIBLE);
                frameIllu.setVisibility(View.GONE);
            }
        });

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
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "读取失败！", Toast.LENGTH_SHORT).show();
        }
        try {
            jsOBJ = new JSONObject(stringBuilder.toString());
            iObj = jsOBJ.getJSONObject("Info");
            if (jsOBJ.has("N")){
                nObj = jsOBJ.getJSONObject("N");
                hasN = true;
            } else {
                hasN = false;
            }
            if (jsOBJ.has("E")){
                eObj = jsOBJ.getJSONObject("E");
                hasE = true;
            } else {
                hasE = false;
            }
            if (jsOBJ.has("S")){
                sObj = jsOBJ.getJSONObject("S");
                hasS = true;
            } else {
                hasS = false;
            }
            if (jsOBJ.has("W")){
                wObj = jsOBJ.getJSONObject("W");
                hasW = true;
            } else {
                hasW = false;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSON文件格式错误！", Toast.LENGTH_SHORT).show();
        }
    }

    public void procData(){
        try {
            name = iObj.getString("Name");
            date = iObj.getString("Date");
            coord = new LatLng(iObj.getDouble("Lat"), iObj.getDouble("Lon"));

            if (hasN) {
                timeN = nObj.getInt("Min") * 60 + nObj.getInt("Sec");
                iNl = (int) ((double) nObj.getInt("LTurn") / (double) timeN * 3600);
                iNn = (int) ((double) nObj.getInt("NTurn") / (double) timeN * 3600);
                iNr = (int) ((double) nObj.getInt("RTurn") / (double) timeN * 3600);
                iN = iNl + iNn + iNr;

                aNL0 = nObj.getJSONArray("LPoints");
                aNL1 = nObj.getJSONArray("LDetails");
                aNN0 = nObj.getJSONArray("NPoints");
                aNN1 = nObj.getJSONArray("NDetails");
                aNR0 = nObj.getJSONArray("RPoints");
                aNR1 = nObj.getJSONArray("RDetails");
            }
            if (hasE) {
                timeE = eObj.getInt("Min") * 60 + eObj.getInt("Sec");
                iEl = (int) ((double) eObj.getInt("LTurn") / (double) timeE * 3600);
                iEn = (int) ((double) eObj.getInt("NTurn") / (double) timeE * 3600);
                iEr = (int) ((double) eObj.getInt("RTurn") / (double) timeE * 3600);
                iE = iEl + iEn + iEr;

                aEL0 = eObj.getJSONArray("LPoints");
                aEL1 = eObj.getJSONArray("LDetails");
                aEN0 = eObj.getJSONArray("NPoints");
                aEN1 = eObj.getJSONArray("NDetails");
                aER0 = eObj.getJSONArray("RPoints");
                aER1 = eObj.getJSONArray("RDetails");
            }
            if (hasS) {
                timeS = sObj.getInt("Min") * 60 + sObj.getInt("Sec");
                iSl = (int) ((double) sObj.getInt("LTurn") / (double) timeS * 3600);
                iSn = (int) ((double) sObj.getInt("NTurn") / (double) timeS * 3600);
                iSr = (int) ((double) sObj.getInt("RTurn") / (double) timeS * 3600);
                iS = iSl + iSn + iSr;

                aSL0 = sObj.getJSONArray("LPoints");
                aSL1 = sObj.getJSONArray("LDetails");
                aSN0 = sObj.getJSONArray("NPoints");
                aSN1 = sObj.getJSONArray("NDetails");
                aSR0 = sObj.getJSONArray("RPoints");
                aSR1 = sObj.getJSONArray("RDetails");
            }
            if (hasW) {
                timeW = wObj.getInt("Min") * 60 + wObj.getInt("Sec");
                iWl = (int) ((double) wObj.getInt("LTurn") / (double) timeW * 3600);
                iWn = (int) ((double) wObj.getInt("NTurn") / (double) timeW * 3600);
                iWr = (int) ((double) wObj.getInt("RTurn") / (double) timeW * 3600);
                iW = iWl + iWn + iWr;

                aWL0 = wObj.getJSONArray("LPoints");
                aWL1 = wObj.getJSONArray("LDetails");
                aWN0 = wObj.getJSONArray("NPoints");
                aWN1 = wObj.getJSONArray("NDetails");
                aWR0 = wObj.getJSONArray("RPoints");
                aWR1 = wObj.getJSONArray("RDetails");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        oN = iSn + iEr + iWl;
        oE = iWn + iSr + iNl;
        oS = iNn + iWr + iEl;
        oW = iEn + iNr + iSl;

    }

    public void setDisp(){
        txtIN.setText(iN + "");
        txtINL.setText(iNl + "");
        txtINN.setText(iNn + "");
        txtINR.setText(iNr + "");
        txtOW.setText(oW + "");
        txtIW.setText(iW + "");
        txtIWL.setText(iWl + "");
        txtIWN.setText(iWn + "");
        txtIWR.setText(iWr + "");
        txtOS.setText(oS + "");
        txtON.setText(oN + "");
        txtIER.setText(iEr + "");
        txtIEN.setText(iEn + "");
        txtIEL.setText(iEl + "");
        txtIE.setText(iE + "");
        txtOE.setText(oE + "");
        txtISL.setText(iSl + "");
        txtISN.setText(iSn + "");
        txtISR.setText(iSr + "");
        txtIS.setText(iS + "");

        txtIntDate.setText(date);
        txtIntName.setText(name);

        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(coord);
        markerOption.draggable(false);
        Marker marker = intMap.addMarker(markerOption);
        CameraUpdateFactory cameraUpdateFactory = new CameraUpdateFactory();
        CameraUpdate cameraUpdate = cameraUpdateFactory.newCameraPosition(new CameraPosition(coord,15,0,0));
        intMap.animateCamera(cameraUpdate);


        LineData lineData = new LineData();

        if (hasN){
            tmpEntry0 = new Entry(0,1);
            lNL = new ArrayList<>();
            lNL.add(tmpEntry0);
            for (int i = 0; i < aNL0.length(); i++){
                try {
                    int tmpi = aNL0.getInt(i)+aNL1.getInt(i);
                    tmpEntry0 = new Entry(aNL0.getInt(i), 1);
                    tmpEntry1 = new Entry(aNL0.getInt(i), (float)1.5);
                    tmpEntry2 = new Entry(tmpi, (float)1.5);
                    tmpEntry3 = new Entry(tmpi, 1);
                    lNL.add(tmpEntry0);
                    lNL.add(tmpEntry1);
                    lNL.add(tmpEntry2);
                    lNL.add(tmpEntry3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tmpEntry0 = new Entry(timeN*1000, 1);
            lNL.add(tmpEntry0);

            tmpEntry0 = new Entry(0,2);
            lNN = new ArrayList<>();
            lNN.add(tmpEntry0);
            for (int i = 0; i < aNN0.length(); i++){
                try {
                    int tmpi = aNN0.getInt(i)+aNN1.getInt(i);
                    tmpEntry0 = new Entry(aNN0.getInt(i), 2);
                    tmpEntry1 = new Entry(aNN0.getInt(i), (float)2.5);
                    tmpEntry2 = new Entry(tmpi, (float)2.5);
                    tmpEntry3 = new Entry(tmpi, 2);
                    lNN.add(tmpEntry0);
                    lNN.add(tmpEntry1);
                    lNN.add(tmpEntry2);
                    lNN.add(tmpEntry3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tmpEntry0 = new Entry(timeN*1000, 2);
            lNN.add(tmpEntry0);

            tmpEntry0 = new Entry(0,3);
            lNR = new ArrayList<>();
            lNR.add(tmpEntry0);
            for (int i = 0; i < aNR0.length(); i++){
                try {
                    int tmpi = aNR0.getInt(i)+aNR1.getInt(i);
                    tmpEntry0 = new Entry(aNR0.getInt(i), 3);
                    tmpEntry1 = new Entry(aNR0.getInt(i), (float)3.5);
                    tmpEntry2 = new Entry(tmpi, (float)3.5);
                    tmpEntry3 = new Entry(tmpi, 3);
                    lNR.add(tmpEntry0);
                    lNR.add(tmpEntry1);
                    lNR.add(tmpEntry2);
                    lNR.add(tmpEntry3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tmpEntry0 = new Entry(timeN*1000, 3);
            lNR.add(tmpEntry0);

            sNL = new LineDataSet(lNL, "北左");
            sNL.setDrawCircles(false);
            sNL.setColor(getColor(R.color.cirredmag));
            sNL.setDrawValues(false);
            sNN = new LineDataSet(lNN, "北直");
            sNN.setDrawCircles(false);
            sNN.setColor(getColor(R.color.cirred));
            sNN.setDrawValues(false);
            sNR = new LineDataSet(lNR, "北右");
            sNR.setDrawCircles(false);
            sNR.setColor(getColor(R.color.cirredorange));
            sNR.setDrawValues(false);
            lineData.addDataSet(sNL);
            lineData.addDataSet(sNN);
            lineData.addDataSet(sNR);
        }

        if (hasE){
            tmpEntry0 = new Entry(0,5);
            lEL = new ArrayList<>();
            lEL.add(tmpEntry0);
            for (int i = 0; i < aEL0.length(); i++){
                try {
                    int tmpi = aEL0.getInt(i)+aEL1.getInt(i);
                    tmpEntry0 = new Entry(aEL0.getInt(i), 5);
                    tmpEntry1 = new Entry(aEL0.getInt(i), (float)5.5);
                    tmpEntry2 = new Entry(tmpi, (float)5.5);
                    tmpEntry3 = new Entry(tmpi, 5);
                    lEL.add(tmpEntry0);
                    lEL.add(tmpEntry1);
                    lEL.add(tmpEntry2);
                    lEL.add(tmpEntry3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tmpEntry0 = new Entry(timeE*1000, 5);
            lEL.add(tmpEntry0);

            tmpEntry0 = new Entry(0,6);
            lEN = new ArrayList<>();
            lEN.add(tmpEntry0);
            for (int i = 0; i < aEN0.length(); i++){
                try {
                    int tmpi = aEN0.getInt(i)+aEN1.getInt(i);
                    tmpEntry0 = new Entry(aEN0.getInt(i), 6);
                    tmpEntry1 = new Entry(aEN0.getInt(i), (float)6.5);
                    tmpEntry2 = new Entry(tmpi, (float)6.5);
                    tmpEntry3 = new Entry(tmpi, 6);
                    lEN.add(tmpEntry0);
                    lEN.add(tmpEntry1);
                    lEN.add(tmpEntry2);
                    lEN.add(tmpEntry3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tmpEntry0 = new Entry(timeE*1000, 6);
            lEN.add(tmpEntry0);

            tmpEntry0 = new Entry(0,7);
            lER = new ArrayList<>();
            lER.add(tmpEntry0);
            for (int i = 0; i < aER0.length(); i++){
                try {
                    int tmpi = aER0.getInt(i)+aER1.getInt(i);
                    tmpEntry0 = new Entry(aER0.getInt(i), 7);
                    tmpEntry1 = new Entry(aER0.getInt(i), (float)7.5);
                    tmpEntry2 = new Entry(tmpi, (float)7.5);
                    tmpEntry3 = new Entry(tmpi, 7);
                    lER.add(tmpEntry0);
                    lER.add(tmpEntry1);
                    lER.add(tmpEntry2);
                    lER.add(tmpEntry3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tmpEntry0 = new Entry(timeE*1000, 7);
            lER.add(tmpEntry0);


            sEL = new LineDataSet(lEL, "东左");
            sEL.setDrawCircles(false);
            sEL.setColor(getColor(R.color.cirgreenyellow));
            sEL.setDrawValues(false);
            sEN = new LineDataSet(lEN, "东直");
            sEN.setDrawCircles(false);
            sEN.setColor(getColor(R.color.cirgreen));
            sEN.setDrawValues(false);
            sER = new LineDataSet(lER, "东右");
            sER.setDrawCircles(false);
            sER.setColor(getColor(R.color.cirgreendark));
            sER.setDrawValues(false);
            lineData.addDataSet(sEL);
            lineData.addDataSet(sEN);
            lineData.addDataSet(sER);
        }

        if (hasS){
            tmpEntry0 = new Entry(0,9);
            lSL = new ArrayList<>();
            lSL.add(tmpEntry0);
            for (int i = 0; i < aSL0.length(); i++){
                try {
                    int tmpi = aSL0.getInt(i)+aSL1.getInt(i);
                    tmpEntry0 = new Entry(aSL0.getInt(i), 9);
                    tmpEntry1 = new Entry(aSL0.getInt(i), (float)9.5);
                    tmpEntry2 = new Entry(tmpi, (float)9.5);
                    tmpEntry3 = new Entry(tmpi, 9);
                    lSL.add(tmpEntry0);
                    lSL.add(tmpEntry1);
                    lSL.add(tmpEntry2);
                    lSL.add(tmpEntry3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tmpEntry0 = new Entry(timeS*1000, 9);
            lSL.add(tmpEntry0);

            tmpEntry0 = new Entry(0,10);
            lSN = new ArrayList<>();
            lSN.add(tmpEntry0);
            for (int i = 0; i < aSN0.length(); i++){
                try {
                    int tmpi = aSN0.getInt(i)+aSN1.getInt(i);
                    tmpEntry0 = new Entry(aSN0.getInt(i), 10);
                    tmpEntry1 = new Entry(aSN0.getInt(i), (float)10.5);
                    tmpEntry2 = new Entry(tmpi, (float)10.5);
                    tmpEntry3 = new Entry(tmpi, 10);
                    lSN.add(tmpEntry0);
                    lSN.add(tmpEntry1);
                    lSN.add(tmpEntry2);
                    lSN.add(tmpEntry3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tmpEntry0 = new Entry(timeS*1000, 10);
            lSN.add(tmpEntry0);

            tmpEntry0 = new Entry(0,11);
            lSR = new ArrayList<>();
            lSR.add(tmpEntry0);
            for (int i = 0; i < aSR0.length(); i++){
                try {
                    int tmpi = aSR0.getInt(i)+aSR1.getInt(i);
                    tmpEntry0 = new Entry(aSR0.getInt(i), 11);
                    tmpEntry1 = new Entry(aSR0.getInt(i), (float)11.5);
                    tmpEntry2 = new Entry(tmpi, (float)11.5);
                    tmpEntry3 = new Entry(tmpi, 11);
                    lSR.add(tmpEntry0);
                    lSR.add(tmpEntry1);
                    lSR.add(tmpEntry2);
                    lSR.add(tmpEntry3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tmpEntry0 = new Entry(timeS*1000, 11);
            lSR.add(tmpEntry0);


            sSL = new LineDataSet(lSL, "南左");
            sSL.setDrawCircles(false);
            sSL.setColor(getColor(R.color.cirbluegreen));
            sSL.setDrawValues(false);
            sSN = new LineDataSet(lSN, "南直");
            sSN.setDrawCircles(false);
            sSN.setColor(getColor(R.color.cirblue));
            sSN.setDrawValues(false);
            sSR = new LineDataSet(lSR, "南右");
            sSR.setDrawCircles(false);
            sSR.setColor(getColor(R.color.cirbluedark));
            sSR.setDrawValues(false);
            lineData.addDataSet(sSL);
            lineData.addDataSet(sSN);
            lineData.addDataSet(sSR);
        }

        if (hasW){
            tmpEntry0 = new Entry(0,13);
            lWL = new ArrayList<>();
            lWL.add(tmpEntry0);
            for (int i = 0; i < aWL0.length(); i++){
                try {
                    int tmpi = aWL0.getInt(i)+aWL1.getInt(i);
                    tmpEntry0 = new Entry(aWL0.getInt(i), 13);
                    tmpEntry1 = new Entry(aWL0.getInt(i), (float)13.5);
                    tmpEntry2 = new Entry(tmpi, (float)13.5);
                    tmpEntry3 = new Entry(tmpi, 13);
                    lWL.add(tmpEntry0);
                    lWL.add(tmpEntry1);
                    lWL.add(tmpEntry2);
                    lWL.add(tmpEntry3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tmpEntry0 = new Entry(timeW*1000, 13);
            lWL.add(tmpEntry0);

            tmpEntry0 = new Entry(0,14);
            lWN = new ArrayList<>();
            lWN.add(tmpEntry0);
            for (int i = 0; i < aWN0.length(); i++){
                try {
                    int tmpi = aWN0.getInt(i)+aWN1.getInt(i);
                    tmpEntry0 = new Entry(aWN0.getInt(i), 14);
                    tmpEntry1 = new Entry(aWN0.getInt(i), (float)14.5);
                    tmpEntry2 = new Entry(tmpi, (float)14.5);
                    tmpEntry3 = new Entry(tmpi, 14);
                    lWN.add(tmpEntry0);
                    lWN.add(tmpEntry1);
                    lWN.add(tmpEntry2);
                    lWN.add(tmpEntry3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tmpEntry0 = new Entry(timeW*1000, 14);
            lWN.add(tmpEntry0);

            tmpEntry0 = new Entry(0,15);
            lWR = new ArrayList<>();
            lWR.add(tmpEntry0);
            for (int i = 0; i < aWR0.length(); i++){
                try {
                    int tmpi = aWR0.getInt(i)+aWR1.getInt(i);
                    tmpEntry0 = new Entry(aWR0.getInt(i), 15);
                    tmpEntry1 = new Entry(aWR0.getInt(i), (float)15.5);
                    tmpEntry2 = new Entry(tmpi, (float)15.5);
                    tmpEntry3 = new Entry(tmpi, 15);
                    lWR.add(tmpEntry0);
                    lWR.add(tmpEntry1);
                    lWR.add(tmpEntry2);
                    lWR.add(tmpEntry3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tmpEntry0 = new Entry(timeW*1000, 15);
            lWR.add(tmpEntry0);

            sWL = new LineDataSet(lWL, "西左");
            sWL.setDrawCircles(false);
            sWL.setColor(getColor(R.color.cirpurpleblue));
            sWL.setDrawValues(false);
            sWN = new LineDataSet(lWN, "西直");
            sWN.setDrawCircles(false);
            sWN.setColor(getColor(R.color.cirpurple));
            sWN.setDrawValues(false);
            sWR = new LineDataSet(lWR, "西右");
            sWR.setDrawCircles(false);
            sWR.setColor(getColor(R.color.cirpurplered));
            sWR.setDrawValues(false);
            lineData.addDataSet(sWL);
            lineData.addDataSet(sWN);
            lineData.addDataSet(sWR);
        }

        intersecChart.setData(lineData);
        intersecChart.getLegend().setTextColor(getColor(R.color.cyan));
        intersecChart.getXAxis().setGridColor(getColor(R.color.black));
        intersecChart.getAxisLeft().setGridColor(getColor(R.color.black));
        intersecChart.getAxisRight().setGridColor(getColor(R.color.black));
        intersecChart.setBackgroundColor(getColor(R.color.black));
        intersecChart.invalidate();

    }


    @Override
    public void onDestroy(){
        finish();
        super.onDestroy();
    }
}
