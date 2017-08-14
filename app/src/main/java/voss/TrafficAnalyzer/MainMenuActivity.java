package voss.TrafficAnalyzer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainMenuActivity extends AppCompatActivity {
    private Button VidRecBtn, ChangeLogBtn;
    private long exitTime;
    private boolean exit;
    private AlertDialog ChangeLogDialog, ChangeLogHistDialog;
    private AlertDialog.Builder CLDBuilder, CLHDBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        VidRecBtn = (Button)findViewById(R.id.vidRecBtn);
        ChangeLogBtn = (Button)findViewById(R.id.changeLogBtn);

        exit = false;

        VidRecBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setClass(MainMenuActivity.this, VidRecordActivity.class);
                startActivity(intent);
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
            exit = true;
            finish();
        }
    }

    @Override
    protected void onDestroy(){

        super.onDestroy();
        if (exit) {
            System.exit(0);
        }
    }
}
