package com.example.keyadministrator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.keyadministrator.ui.activity.RegisterAndRecognizeActivity;

public class LoginSelectActivity extends AppCompatActivity {
    private Button faceLoginButton, passwordLoginButton, cardLoginButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_select);

        faceLoginButton();
        passwordLoginButton();
        cardLoginButton();
        backReturn();

    }
    // 为人脸登录按钮设置点击事件监听器
    private void faceLoginButton() {
        faceLoginButton = findViewById(R.id.faceLoginButton);
        faceLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginSelectActivity.this, RegisterAndRecognizeActivity.class);
            intent.putExtra("loginType","FaceRectView");
            startActivity(intent);
        });
    }
    // 为密码登录按钮设置点击事件监听器
    private void passwordLoginButton() {
        passwordLoginButton = findViewById(R.id.passwordLoginButton);
        passwordLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginSelectActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
    // 为刷卡登录按钮设置点击事件监听器
    private void cardLoginButton() {
        cardLoginButton = findViewById(R.id.cardIdLoginButton);
        cardLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginSelectActivity.this, CardLoginActivity.class);
            startActivity(intent);
        });
    }
    // 设置返回按钮
    private void backReturn() {
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginSelectActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}