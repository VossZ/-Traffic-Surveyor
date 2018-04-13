package voss.TrafficAnalyzer;
//// TODO: 按钮美化，图表显示位置
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;

public class MainMenuActivity extends AppCompatActivity {
    private LinearLayout VidRecBtn, IntersecRecBtn, BrowseBtn, AutoRecBtn;
    private TextView ChangeLogBtn;
    private long exitTime;
    private AlertDialog ChangeLogDialog, ChangeLogHistDialog;
    private AlertDialog.Builder CLDBuilder, CLHDBuilder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        VidRecBtn = (LinearLayout)findViewById(R.id.vidRecBtn);
        IntersecRecBtn = (LinearLayout)findViewById(R.id.intersecRecBtn);
        BrowseBtn = (LinearLayout)findViewById(R.id.dataRepBtn);
        AutoRecBtn = (LinearLayout)findViewById(R.id.autoRecBtn);
        ChangeLogBtn = (TextView)findViewById(R.id.changeLogBtn);


        VidRecBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setClass(MainMenuActivity.this, VidRecordActivity.class);
                startActivity(intent);
            }
        });

        IntersecRecBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setClass(MainMenuActivity.this, IntersecRecActivity.class);
                startActivity(intent);
            }
        });

        BrowseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setClass(MainMenuActivity.this, BrowseActivity.class);
                startActivity(intent);
            }
        });

        AutoRecBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainMenuActivity.this, "功能未完成", Toast.LENGTH_SHORT).show();
            }
        });


        ChangeLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CLHDBuilder = new AlertDialog.Builder(MainMenuActivity.this);
                CLHDBuilder.setTitle("历史记录");
                CLHDBuilder.setCancelable(true);
                CLHDBuilder.setItems(getResources().getStringArray(R.array.changeloghist), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                CLHDBuilder.setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainMenuActivity.this, "返回主菜单", Toast.LENGTH_SHORT).show();
                    }
                });

                CLDBuilder = new AlertDialog.Builder(MainMenuActivity.this);
                CLDBuilder.setTitle("更新记录");
                CLDBuilder.setMessage(getResources().getString(R.string.changelognew));
                CLDBuilder.setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainMenuActivity.this, "返回主菜单", Toast.LENGTH_SHORT).show();
                    }
                });
                CLDBuilder.setPositiveButton("历史记录", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ChangeLogHistDialog = CLHDBuilder.create();
                        ChangeLogHistDialog.show();
                    }
                });
                CLDBuilder.setCancelable(true);
                ChangeLogDialog = CLDBuilder.create();
                ChangeLogDialog.show();

            }
        });
    }

    @Override
    public void onBackPressed(){
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            // ToastUtil.makeToastInBottom("再按一次退出应用", MainMyselfActivity);
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
            return;
        } else {
            finish();
            this.onDestroy();
        }
    }
}
