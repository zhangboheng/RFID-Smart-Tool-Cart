package com.example.keyadministrator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

public class ControlSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_settings); // 替换为你实际的布局文件名
        // 返回按钮点击事件
        backButton();
        // 点击台账信息按钮事件
        itemInformationButton();
        // 点击物品管理按钮事件
        goodsControlButton();
        // 点击类别管理按钮事件
        clarifyControlButton();
        // 点击参数配置按钮时间
        parameterControlButton();
        // 点击关闭应用
        closeApplicationButton();
    }
    // 返回按钮点击事件
    private void backButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // 关闭当前 Activity，返回上一级
        });
    }
    // 点击台账信息按钮事件
    private void itemInformationButton() {
        Button itemInformationButton = findViewById(R.id.itemInformationButton);
        itemInformationButton.setOnClickListener(v -> {
            Intent intent = new Intent(ControlSettingsActivity.this, ItemsInformationActivity.class);
            startActivity(intent);
        });
    }
    // 点击物品管理按钮事件
    private void goodsControlButton() {
        int userId = getIntent().getIntExtra("userId", -1);
        int userAdm = getIntent().getIntExtra("userAdm", -1);
        Button goodsControlButton = findViewById(R.id.goodsControlButton);
        goodsControlButton.setOnClickListener(v -> {
            Intent intent = new Intent(ControlSettingsActivity.this, GoodsControlActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userAdm", userAdm);
            startActivity(intent);
        });
    }
    // 点击类别管理按钮事件
    private void clarifyControlButton() {
        Button clarifyControlButton = findViewById(R.id.clarifyControlButton);
        clarifyControlButton.setOnClickListener(v -> {
            Intent intent = new Intent(ControlSettingsActivity.this, CategoryControlActivity.class);
            startActivity(intent);
        });
    }
    // 点击参数配置按钮时间
    private void parameterControlButton() {
        Button parameterControlButton = findViewById(R.id.parameterControlButton);
        parameterControlButton.setOnClickListener(v -> {
            Intent intent = new Intent(ControlSettingsActivity.this, SerialConfigPageActivity.class);
            startActivity(intent);
        });
    }
    // 点击关闭应用
    private void closeApplicationButton() {
        Button closeApplicationButton = findViewById(R.id.closeApplicationButton);
        closeApplicationButton.setOnClickListener(v -> {
            finishAffinity(); // 关闭所有 Activity
        });
    }
}