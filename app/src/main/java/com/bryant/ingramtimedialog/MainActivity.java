package com.bryant.ingramtimedialog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bryant.ingramtimedialoglibrary.IngramDialog;

public class MainActivity extends AppCompatActivity {

    private IngramDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new IngramDialog(this);
        dialog.setTipsStr("请选择开始时间")
                .setCancelText("取消")
                .setFixText("确定")
                .build();
        dialog.setTimeListener(new IngramDialog.TimeClickListener() {
            @Override
            public void onTimeClick(String time) {
                Toast.makeText(MainActivity.this,time,Toast.LENGTH_SHORT).show();
            }
        });

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();

            }
        });
    }
}
