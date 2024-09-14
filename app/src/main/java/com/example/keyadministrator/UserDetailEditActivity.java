package com.example.keyadministrator;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.keyadministrator.data.FaceRepository;
import com.example.keyadministrator.facedb.FaceDatabase;
import com.example.keyadministrator.facedb.dao.FaceDao;
import com.example.keyadministrator.ui.activity.RegisterAndRecognizeActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserDetailEditActivity extends Activity {
    private FaceDao faceDao;
    private FaceRepository faceRepository;
    private int currentUserId;
    private long faceId;
    private DatabaseHelper dbHelper;
    private QueryFaceTask queryFaceTask;
    private SocketClient socketClient;
    private byte[] getFeatureData;
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private String globalImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail_edit);
        FaceDatabase faceDatabase = FaceDatabase.getInstance(this);
        faceDao = faceDatabase.faceDao();
        dbHelper = new DatabaseHelper(this);
        queryFaceTask = new QueryFaceTask(faceDao);

        socketClient = SocketClient.getInstance();

        // 初始化数据
        initializeData();
        // 设置返回按钮事件
        returnBack();
        // 设置监听器
        setupListeners();
        // 点击跳转到人脸注册页面
        goToRegisterFace();
    }

    // 设置返回按钮事件
    private void returnBack() {
        // 获取返回按钮
        ImageButton backButton = findViewById(R.id.backButton);
        // 设置返回按钮的点击事件
        backButton.setOnClickListener(v -> {
            // 返回上一个页面
            finish();
        });
    }

    // 设置初始化数据
    private void initializeData() {
        ImageView userImageView = findViewById(R.id.userImageView);
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText departmentEditText = findViewById(R.id.departmentEditText);
        EditText phoneEditText = findViewById(R.id.phoneEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        EditText cardIdEditText = findViewById(R.id.cardIdEditText);
        EditText startTimeTextView = findViewById(R.id.startTimeEditText);
        EditText endTimeTextView = findViewById(R.id.endTimeEditText);
        SwitchCompat enabledSwitch = findViewById(R.id.enabledSwitch);
        LinearLayout cabinetPermissionsLayout = findViewById(R.id.cabinetPermissionsLayout);

        // 可用的开柜权限列表
        List<String> cabinetPermissions = getCabinetPermissionsFromDatabase(); // 从数据库获取可用的开柜权限
        currentUserId = getIntent().getIntExtra("user_id", -1);
        if (currentUserId != -1) {
            User user = dbHelper.getUserById(currentUserId);
            if (user.getFaceId() == null || user.getFaceId().isEmpty()) {
                faceId = -1;
            } else {
                faceId = Long.parseLong(user.getFaceId());
            }
            String imagePath = user.getImagePath();
            globalImagePath = imagePath;
            if (imagePath != null && !imagePath.isEmpty()) {
                Glide.with(this).load(imagePath).into(userImageView);
            }
            String name = user.getUsername();
            String part = user.getUserPart();
            String phone = user.getUserPhone();
            String password = user.getPassword();
            String cardId = user.getCardId();
            String startTime = user.getStartTime();
            String endTime = user.getEndTime();
            boolean isEnabled = user.getEnabled() == 1;
            boolean check0 = user.isCk0() == 1;
            boolean check1 = user.isCk1() == 1;
            boolean check2 = user.isCk2() == 1;
            boolean check3 = user.isCk3() == 1;
            boolean check4 = user.isCk4() == 1;
            boolean check5 = user.isCk5() == 1;

            nameEditText.setText(name);
            departmentEditText.setText(part);
            phoneEditText.setText(phone);
            cardIdEditText.setText(cardId);
            passwordEditText.setText(password);
            enabledSwitch.setChecked(isEnabled);
            startTimeTextView.setText(startTime);
            endTimeTextView.setText(endTime);
            for (int i = 0; i < cabinetPermissions.size(); i++) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(cabinetPermissions.get(i));
                checkBox.setTextColor(ContextCompat.getColor(this, R.color.colorSecondary));
                // 根据用户的权限信息设置 checkbox 的选中状态
                switch (i) {
                    case 0:
                        checkBox.setChecked(check0);
                        break;
                    case 1:
                        checkBox.setChecked(check1);
                        break;
                    case 2:
                        checkBox.setChecked(check2);
                        break;
                    case 3:
                        checkBox.setChecked(check3);
                        break;
                    case 4:
                        checkBox.setChecked(check4);
                        break;
                    case 5:
                        checkBox.setChecked(check5);
                        break;
                }
                cabinetPermissionsLayout.addView(checkBox);
            }
        } else {
            finish();
        }
    }

    // 点击跳转到人脸注册页面
    private void goToRegisterFace() {
        ImageView userImageView = findViewById(R.id.userImageView);
        userImageView.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterAndRecognizeActivity.class);
            intent.putExtra("editRec", "BackEditRegister");
            intent.putExtra("userId", currentUserId);
            intent.putExtra("faceId", faceId);
            intent.putExtra("imagePath", globalImagePath);
            startActivity(intent);
            finish();
        });
    }

    // 设置监听器
    private void setupListeners() {

        // 初始化视图
        Button deleteButton = findViewById(R.id.deleteButton);
        Button saveButton = findViewById(R.id.saveButton);

        // 删除按钮点击事件
        deleteButton.setOnClickListener(v -> {
            // 从数据库中删除当前用户
            deleteUserFromDatabase();
        });

        // 保存按钮点击事件
        saveButton.setOnClickListener(v -> {
            // 保存用户信息到数据库
            saveUserToDatabase();
        });

        EditText startTimeTextView = findViewById(R.id.startTimeEditText);
        EditText endTimeTextView = findViewById(R.id.endTimeEditText);

        // 设置时间选择器
        startTimeTextView.setOnClickListener(v -> showDatePicker(true));

        endTimeTextView.setOnClickListener(v -> showDatePicker(false));
    }

    // 显示日期选择器
    private void showDatePicker(final boolean isStartTime) {
        EditText startTimeTextView = findViewById(R.id.startTimeEditText);
        EditText endTimeTextView = findViewById(R.id.endTimeEditText);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                        UserDetailEditActivity.this,
                        (view1, hourOfDay, minute) -> {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year, month, dayOfMonth, hourOfDay, minute, 0); // 设置秒为 0
                            if (isStartTime) {
                                startCalendar = calendar;
                                startTimeTextView.setText(dateFormat.format(startCalendar.getTime()));
                            } else {
                                endCalendar = calendar;
                                endTimeTextView.setText(dateFormat.format(endCalendar.getTime()));}
                        },
                        (isStartTime ? startCalendar : endCalendar).get(Calendar.HOUR_OF_DAY),
                        (isStartTime ? startCalendar : endCalendar).get(Calendar.MINUTE),
                        true
                );
                    timePickerDialog.show();
                },
                (isStartTime ? startCalendar : endCalendar).get(Calendar.YEAR),
                (isStartTime ? startCalendar : endCalendar).get(Calendar.MONTH),
                (isStartTime ? startCalendar : endCalendar).get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // 从数据库获取可用的开柜权限列表
    private List<String> getCabinetPermissionsFromDatabase() {
        return Arrays.asList("管理员权限", "1号柜权限", "2号柜权限", "3号柜权限", "4号柜权限");
    }

    // 从数据库中删除当前用户
    private void deleteUserFromDatabase() {
        if (currentUserId != 1) {
            // 删除app.db数据
            dbHelper.deleteUser(currentUserId);
            // 删除face.db数据
            queryFaceTask.execute((int) faceId);
            // 在连接成功后，可以安全地使用 writer
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                JSONArray idsArray = new JSONArray();
                idsArray.put(currentUserId);
                // 创建内部data JSON对象并添加Ids数组
                JSONObject innerData = new JSONObject();
                innerData.put("Ids", idsArray);
                socketClient.sendParameterData(dateFormat.format(new Date()), "delete_user", innerData.toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            Toast.makeText(UserDetailEditActivity.this, "用户已删除", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserDetailEditActivity.this, UserControl.class);
            startActivity(intent);
            finish();
        }else{
            Toast.makeText(UserDetailEditActivity.this, "管理员账号不能删除", Toast.LENGTH_SHORT).show();
        }
    }

    // 保存用户信息到数据库
    private void saveUserToDatabase() {
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText departmentEditText = findViewById(R.id.departmentEditText);
        EditText phoneEditText = findViewById(R.id.phoneEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        EditText cardIdEditText = findViewById(R.id.cardIdEditText);
        EditText startTimeTextView = findViewById(R.id.startTimeEditText);
        EditText endTimeTextView = findViewById(R.id.endTimeEditText);
        SwitchCompat enabledSwitch = findViewById(R.id.enabledSwitch);
        LinearLayout cabinetPermissionsLayout = findViewById(R.id.cabinetPermissionsLayout);
        String name = nameEditText.getText().toString();
        String part = departmentEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String cardId = cardIdEditText.getText().toString();
        String startTime = startTimeTextView.getText().toString();
        String endTime = endTimeTextView.getText().toString();
        int enabled = enabledSwitch.isChecked() ? 1 : 0;
        int ck0 = 0, ck1 = 0, ck2 = 0, ck3 = 0, ck4 = 0, ck5 = 0;
        for (int i = 0; i < cabinetPermissionsLayout.getChildCount(); i++) {
            View child = cabinetPermissionsLayout.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                switch (i) {
                    case 0:
                        ck0 = checkBox.isChecked() ? 1 : 0;
                        break;
                    case 1:
                        ck1 = checkBox.isChecked() ? 1 : 0;
                        break;
                    case 2:
                        ck2 = checkBox.isChecked() ? 1 : 0;
                        break;
                    case 3:
                        ck3 = checkBox.isChecked() ? 1 : 0;
                        break;
                    case 4:
                        ck4 = checkBox.isChecked() ? 1 : 0;
                        break;
                    case 5:
                        ck5 = checkBox.isChecked() ? 1 : 0;
                        break;
                }
            }
        }
        try {
            Date startDate = dateFormat.parse(startTime);
            Date endDate = dateFormat.parse(endTime);

            if (startDate != null && endDate != null && startDate.compareTo(endDate) >= 0) {
                Toast.makeText(UserDetailEditActivity.this, "开始时间必须小于结束时间", Toast.LENGTH_SHORT).show();
                return; // 阻止保存操作
            }
        } catch (ParseException e) {
            Toast.makeText(UserDetailEditActivity.this, "时间格式错误", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        if (!CheckPhoneNumber.isPhone(phone)) {
            Toast.makeText(UserDetailEditActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
        } else {
            try {
                // 生成盐值并哈希密码
                User user = dbHelper.getUserById(currentUserId);
                String originPassword = user.getPassword();
                String salt = PasswordUtils.getSalt();
                String hashedPassword;
                if (originPassword.equals(password)) {
                    hashedPassword = originPassword;
                } else {
                    hashedPassword = PasswordUtils.hashPassword(password, salt);
                }
                String imagePath = user.getImagePath();
                User updatedUser = new User(currentUserId, name, hashedPassword, part, phone, user.getFaceId(), imagePath, cardId, ck0, ck1, ck2, ck3, ck4, ck5, startTime, endTime, enabled);
                dbHelper.updateUser(updatedUser);
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    // 创建内部data JSON对象并添加Ids数组
                    JSONObject innerData = new JSONObject();
                    innerData.put("userId", currentUserId);
                    innerData.put("userName", name);
                    innerData.put("nickName", "");
                    innerData.put("email", "");
                    innerData.put("remark", "");
                    innerData.put("department", part);
                    innerData.put("phonenumber", phone);
                    String base64String = ImageUtils.imageToBase64(imagePath);
                    innerData.put("avatar", base64String);
                    innerData.put("sex", 0);
                    innerData.put("password", hashedPassword);
                    innerData.put("cardnumber", cardId);
                    queryFaceTask.getFaceById((int) faceId, faceEntity -> {
                        if (faceEntity != null) {
                            getFeatureData = faceEntity.getFeatureData();
                        }
                    });
                    innerData.put("faceData", getFeatureData);
                    socketClient.sendParameterData(dateFormat.format(new Date()), "update_user", innerData.toString());

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                SharedPreferences sharedPreferences = getSharedPreferences("editUsersSuccess", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("paramName", "success"); // 保存参数
                editor.apply();
            } catch (NoSuchAlgorithmException e) {
                Toast.makeText(this, "密码加密错误", Toast.LENGTH_SHORT).show();
                throw new RuntimeException(e);
            }
            Toast.makeText(UserDetailEditActivity.this, "用户信息已保存", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 关闭数据库连接
        if (dbHelper != null) {
            dbHelper.close();
        }
        // 关闭后台任务线程
        if (queryFaceTask != null) {
            queryFaceTask.cancel();
        }
    }
}