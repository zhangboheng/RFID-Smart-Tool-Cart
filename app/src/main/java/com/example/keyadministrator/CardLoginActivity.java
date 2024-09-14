package com.example.keyadministrator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CardLoginActivity extends AppCompatActivity {

    private EditText cardIdInput;
    private Button cardIdLoginButton;
    private ImageButton backButton;

    private DatabaseHelper dbHelper;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_login);

        dbHelper = new DatabaseHelper(this);

        cardIdInput = findViewById(R.id.cardIdInput);
        cardIdLoginButton = findViewById(R.id.cardIdLoginButton);
        backButton = findViewById(R.id.backButton);

        cardIdInput.setInputType(InputType.TYPE_NULL);

        // 返回之前页面的方法
        returnBack();
        // 扫描卡片后登录
        scanCardLogin();
        // 清空输入框内容
        clearInput();
    }
    // 返回之前页面的方法
    public void returnBack() {
        backButton.setOnClickListener(v-> {
            finish();
        });
    }

    // 扫描卡片后登录
    public void scanCardLogin() {
        cardIdLoginButton.setOnClickListener(v-> {
            String cardId = cardIdInput.getText().toString();
            try {
                handleCardLogin(cardId);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // 清空输入框内容
    public void clearInput() {
        Button clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(v-> {
            cardIdInput.setText("");
        });
    }

    private void handleCardLogin(String cardId) throws ParseException {
        if (cardId.isEmpty()) {
            Toast.makeText(this, "请输入卡 ID", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = dbHelper.getUserByCardId(cardId);

        if (user != null) {
            if (user.getEnabled() == 1) {
                if (user.getStartTime() != null && user.getEndTime() != null) {
                    Date startDate = dateFormat.parse(user.getStartTime()); // 获取开始时间
                    Date endDate = dateFormat.parse(user.getEndTime());; // 获取结束时间
                    boolean isBetween = isCurrentTimeBetween(startDate, endDate);
                    if (isBetween) {
                        int code = user.isCk0();
                        // 卡 ID 登录成功
                        Toast.makeText(this, "卡 ID 登录成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, UserLoginActivity.class);
                        intent.putExtra("userId",user.getId());
                        intent.putExtra("userAdm",code);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(CardLoginActivity.this, "在该时间段用户没有权限", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    int code = user.isCk0();
                    // 卡 ID 登录成功
                    Toast.makeText(this, "卡 ID 登录成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, UserLoginActivity.class);
                    intent.putExtra("userId",user.getId());
                    intent.putExtra("userAdm",code);
                    startActivity(intent);
                    finish();
                }
            }else{
                Toast.makeText(this, "该用户已被禁用", Toast.LENGTH_SHORT).show();
            }
        }else {
            // 卡 ID 登录失败
            Toast.makeText(this, "卡 ID 无效", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isCurrentTimeBetween(Date startDate, Date endDate) {
        Date currentTime = new Date();
        return currentTime.after(startDate) && currentTime.before(endDate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        backButton.setOnClickListener(null);
        cardIdLoginButton.setOnClickListener(null);
    }
}