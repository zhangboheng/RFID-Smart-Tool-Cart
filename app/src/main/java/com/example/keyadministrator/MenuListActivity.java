package com.example.keyadministrator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.keyadministrator.ui.activity.ActivationActivity;
import com.example.keyadministrator.ui.activity.FaceManageActivity;
import com.example.keyadministrator.ui.activity.RegisterAndRecognizeActivity;

public class MenuListActivity extends AppCompatActivity {
    private int userId, userAdm;
    private FrameLayout frameLayout1;
    private FrameLayout frameLayout2;
    private FrameLayout frameLayout5;
    private FrameLayout frameLayout6;
    private ImageButton backButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        // 首先获取 userId 和 userAdm
        userId = getIntent().getIntExtra("userId", -1);
        userAdm = getIntent().getIntExtra("userAdm", -1);

        initClick();
        backReturn();
    }
    // 页面和模块点击
    private void initClick() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // FrameLayout 的 ID 数组
        int[] frameIds = {
                R.id.frame1,
                R.id.frame2,
                R.id.frame5,
                R.id.frame6
        };
        // 遍历所有 FrameLayout 并设置透明度
        for (int id : frameIds) {
            FrameLayout frame = findViewById(id);
            if (frame != null) {
                frame.setAlpha(0.8f); // 设置透明度为 80%
            }
        }
        frameLayout1 = findViewById(R.id.frame1);
        frameLayout2 = findViewById(R.id.frame2);
        frameLayout5 = findViewById(R.id.frame5);
        frameLayout6 = findViewById(R.id.frame6);
        // 设置用户管理点击事件监听器
        frameLayout1.setOnClickListener(v-> {
            Intent intent = new Intent(MenuListActivity.this, UserControl.class);
            startActivity(intent);
        });
        // 设置人脸组件激活点击事件
        frameLayout2.setOnClickListener(v-> {
            Intent intent = new Intent(MenuListActivity.this, ActivationActivity.class);
            startActivity(intent);
        });
        frameLayout5.setOnClickListener(v-> {
            Intent intent = new Intent(MenuListActivity.this, ControlSettingsActivity.class);
            startActivity(intent);
        });
        frameLayout6.setOnClickListener(v-> {
            Intent intent = new Intent(MenuListActivity.this, AboutUsActivity.class);
            startActivity(intent);
        });
    }
    // 跳转到借还页面
    private void backReturn() {
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v-> {
            sharedPreferences = getSharedPreferences("user_data_login", MODE_PRIVATE);
            userId = sharedPreferences.getInt("userId", -1);
            userAdm = sharedPreferences.getInt("userAdm", -1);
            Intent intent = new Intent(this, UserLoginActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userAdm", userAdm);
            startActivity(intent);
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (userId != -1 || userAdm != -1) {
            sharedPreferences = getSharedPreferences("user_data_login", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("userId", userId);
            editor.putInt("userAdm", userAdm);
            editor.apply();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        frameLayout1.setOnClickListener(null);
        frameLayout2.setOnClickListener(null);
        frameLayout5.setOnClickListener(null);
        frameLayout6.setOnClickListener(null);
        backButton.setOnClickListener(null);
    }
}