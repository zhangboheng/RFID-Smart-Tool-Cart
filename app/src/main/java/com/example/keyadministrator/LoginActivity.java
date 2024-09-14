package com.example.keyadministrator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private ImageButton backButton;
    private Button loginButton;
    private EditText usernameEditText;
    private EditText passwordEditText;
    DatabaseHelper dbHelper = new DatabaseHelper(this);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 设置返回按钮事件
        backReturn();
        // 设置登录按钮事件
        loginButtonEvent();
    }

    // 设置返回按钮事件
    private void backReturn() {
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, LoginSelectActivity.class);
            startActivity(intent);
        });
    }

    // 设置登录按钮事件
    private void loginButtonEvent() {
        loginButton = findViewById(R.id.login_button);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);

        loginButton.setOnClickListener(v-> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            try{
                String salt = PasswordUtils.getSalt();
                String hashedPassword = PasswordUtils.hashPassword(password, salt);
                User user = dbHelper.getUserByUsernameAndPassword(username, hashedPassword);
                    if ((boolean) validateLogin(username, hashedPassword,0)) {
                        if (user.getEnabled() == 1) {
                            if (user.getStartTime() != null && user.getEndTime() != null) {
                                Date startDate = dateFormat.parse(user.getStartTime()); // 获取开始时间
                                Date endDate = dateFormat.parse(user.getEndTime());; // 获取结束时间
                                boolean isBetween = isCurrentTimeBetween(startDate, endDate);
                                if (isBetween) {
                                    int code = user.isCk0();
                                    Intent intent;
                                    intent = new Intent(LoginActivity.this, UserLoginActivity.class);
                                    intent.putExtra("userId",user.getId());
                                    intent.putExtra("userAdm",code);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Toast.makeText(LoginActivity.this, "在该时间段用户没有权限", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                int code = user.isCk0();
                                Intent intent;
                                intent = new Intent(LoginActivity.this, UserLoginActivity.class);
                                intent.putExtra("userId",user.getId());
                                intent.putExtra("userAdm",code);
                                startActivity(intent);
                                finish();
                            }
                        }else{
                            Toast.makeText(LoginActivity.this, "用户已被禁用", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "用户名或者密码错误", Toast.LENGTH_SHORT).show();
                    }
            } catch (NoSuchAlgorithmException e) {
                Toast.makeText(this, "密码加密错误", Toast.LENGTH_SHORT).show();
                throw new RuntimeException(e);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean isCurrentTimeBetween(Date startDate, Date endDate) {
        Date currentTime = new Date();
        return currentTime.after(startDate) && currentTime.before(endDate);
    }

    private Object validateLogin(String username, String password, int chooseType) {
        ValidationResult result = dbHelper.validateUser(username, password);
        User user = dbHelper.getUserByUsernameAndPassword(username, password);
        if (user != null){
            return true;
        }else{
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        backButton.setOnClickListener(null);
        loginButton.setOnClickListener(null);
    }
}

