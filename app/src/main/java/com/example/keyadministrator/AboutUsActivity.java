package com.example.keyadministrator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        // 获取返回按钮
        ImageButton backButton = findViewById(R.id.backButton);

        // 获取顶部通栏的RelativeLayout
        RelativeLayout toolbar = findViewById(R.id.toolbar);


        // 设置顶部通栏的透明度
        toolbar.setAlpha(0.9f);

        // 设置返回按钮的点击事件
        backButton.setOnClickListener(v-> {
            finish();
        });
    }
}
