package com.example.keyadministrator;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.keyadministrator.ui.activity.RegisterAndRecognizeActivity;

import java.security.NoSuchAlgorithmException;

public class AddUserActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        dbHelper = new DatabaseHelper(this);

        TextView cardIdTextView = findViewById(R.id.editTextUserCardId);
        cardIdTextView.setInputType(InputType.TYPE_NULL);
        // 顶部通栏功能
        toolbarSettings();
        // 点击清空按钮事件
        clearIDCard();
        // 点击注册按钮后跳转到人脸识别
        registerFace();
        backMenu();
    }
    // 设置返回属性和点击事件
    private void toolbarSettings() {
        // 获取顶部通栏的 RelativeLayout
        RelativeLayout toolbar = findViewById(R.id.toolbar);
        // 设置顶部通栏的透明度
        toolbar.setAlpha(0.9f);
        // 获取返回按钮
        ImageButton backButton = findViewById(R.id.backButton);
        // 设置返回按钮的点击事件
        backButton.setOnClickListener(v -> {
            // 返回上一个页面
            finish();
        });
    }
    // 点击清空按钮事件
    private void clearIDCard() {
        Button clearButton = findViewById(R.id.buttonClear);
        clearButton.setOnClickListener(v -> {
            EditText cardIdEditText = findViewById(R.id.editTextUserCardId);
            cardIdEditText.setText("");
        });
    }

    // 点击注册按钮后跳转到人脸识别
    private void registerFace() {
        // 需要保存的输入数据
        CheckBox checkBox0 = findViewById(R.id.checkBox0);
        CheckBox checkBox1 = findViewById(R.id.checkBox1);
        CheckBox checkBox2 = findViewById(R.id.checkBox2);
        CheckBox checkBox3 = findViewById(R.id.checkBox3);
        CheckBox checkBox4 = findViewById(R.id.checkBox4);
        EditText userNameEditText = findViewById(R.id.editTextUserName);
        EditText userPartEditText = findViewById(R.id.editTextUserPart);
        EditText userPhoneEditText = findViewById(R.id.editTextUserPhone);
        EditText userPasswordEditText = findViewById(R.id.editTextUserPassword);
        EditText userCardIdEditText = findViewById(R.id.editTextUserCardId);
        // 点击注册进入人脸信息验证页面
        Button saveUserButton = findViewById(R.id.buttonSaveUser);
        // 点击保存用户
        saveUserButton.setOnClickListener(v -> {
            boolean isAnyCheckBoxChecked = checkBox0.isChecked() || checkBox1.isChecked() || checkBox2.isChecked() || checkBox3.isChecked() || checkBox4.isChecked();
            boolean areAllFieldsFilled = !userNameEditText.getText().toString().isEmpty() && !userPartEditText.getText().toString().isEmpty() && !userPhoneEditText.getText().toString().isEmpty() && !userPasswordEditText.getText().toString().isEmpty() && !userCardIdEditText.getText().toString().isEmpty();
            if (isAnyCheckBoxChecked && areAllFieldsFilled) {
                boolean ck0 = checkBox0.isChecked();
                boolean ck1 = checkBox1.isChecked();
                boolean ck2 = checkBox2.isChecked();
                boolean ck3 = checkBox3.isChecked();
                boolean ck4 = checkBox4.isChecked();
                String name = userNameEditText.getText().toString();
                String part = userPartEditText.getText().toString();
                String phone = userPhoneEditText.getText().toString();
                String password = userPasswordEditText.getText().toString();
                String cardId = userCardIdEditText.getText().toString();
                User getUserByCardId = dbHelper.getUserByCardId(cardId);
                if (getUserByCardId != null) {
                    Toast.makeText(AddUserActivity.this, "该卡号已存在", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!CheckPhoneNumber.isPhone(phone)) {
                    Toast.makeText(AddUserActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    // 生成盐值并哈希密码
                    String salt = PasswordUtils.getSalt();
                    String hashedPassword = PasswordUtils.hashPassword(password, salt);

                    // 跳转到人脸识别页面
                    Intent intent = new Intent(this, RegisterAndRecognizeActivity.class);
                    intent.putExtra("registerRec","BackRegister");
                    intent.putExtra("ck0status", ck0);
                    intent.putExtra("ck1status", ck1);
                    intent.putExtra("ck2status", ck2);
                    intent.putExtra("ck3status", ck3);
                    intent.putExtra("ck4status", ck4);
                    intent.putExtra("userName", name);
                    intent.putExtra("partName", part);
                    intent.putExtra("phoneNum", phone);
                    intent.putExtra("password", hashedPassword);
                    intent.putExtra("cardId", cardId);
                    startActivity(intent);

                } catch (NoSuchAlgorithmException e) {
                    Toast.makeText(this, "密码加密错误", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                if (!isAnyCheckBoxChecked) {
                    Toast.makeText(AddUserActivity.this, "请至少选择一个权限", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddUserActivity.this, "请填写所有信息", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 设置底部退出按钮事件
    private void backMenu() {
        Button quitPageButton = findViewById(R.id.buttonNew);
        quitPageButton.setOnClickListener(v -> {
            // 返回上一个页面
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}