package com.example.keyadministrator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.keyadministrator.ui.activity.RegisterAndRecognizeActivity;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class UserControl extends AppCompatActivity {
    private ImageView searchImageView;
    private UserAdapter adapter;
    private XRecyclerView userDataRecyclerView;
    private TextView noDataTextView, addTextView, mutalAddTextView;
    private ImageButton backButton;
    private Handler mHandler = new Handler();
    private DatabaseHelper dbHelper;
    private List<User> allUsers = new ArrayList<>(); // 原始数据列表
    private final List<User> filteredUsers = new ArrayList<>(); // 搜索过滤后的数据列表
    private static final int REQUEST_CODE_PICK_CSV = 1;
    private boolean canLoad = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_control);

        userDataRecyclerView = findViewById(R.id.rv_data);
        noDataTextView = findViewById(R.id.tv_no_data);

        dbHelper = new DatabaseHelper(this);
        // 设置返回属性和点击事件
        toolbarSettings();
        // 用户数据监听
        setupListeners();
        // 用户表格列表配置
        configureRecyclerView();
        // 加载数据库数据
        loadSampleUsers();
        // 点击引入CSV文件
        pickFile();
    }
    // 设置返回属性和点击事件
    private void toolbarSettings() {
        // 获取顶部通栏的 RelativeLayout
        RelativeLayout toolbar = findViewById(R.id.toolbar);
        // 设置顶部通栏的透明度
        toolbar.setAlpha(0.9f);
        // 获取返回按钮
        backButton = findViewById(R.id.backButton);
        // 设置返回按钮的点击事件
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuListActivity.class);
            startActivity(intent);
            // 返回上一个页面
            finish();
        });
    }
    // 用户数据监听
    private void setupListeners() {
        searchImageView = findViewById(R.id.iv_search);
        searchImageView.setOnClickListener(v -> performSearch());  // 执行搜索操作
        addTextView = findViewById(R.id.tv_add);
        addTextView.setOnClickListener(v -> addUser());  // 添加新用户
    }

    private void performSearch() {
        EditText searchEditText = findViewById(R.id.et_fragment_search);
        String query = searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            filteredUsers.clear(); // 清空之前的搜索结果
            for (User user : allUsers) {
                // 假设搜索可以基于名称或部门，根据需要修改
                if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                    filteredUsers.add(user);
                }
            }
            adapter.updateUserList(filteredUsers); // 更新适配器的数据
            checkDataEmpty(filteredUsers); // 检查是否有搜索结果
        } else {
            // 如果查询为空，则显示全部数据
            loadSampleUsers();
        }
    }
    // 点击添加用户进入表单
    private void addUser() {
        int userId = getIntent().getIntExtra("userId", -1);
        int userAdm = getIntent().getIntExtra("userAdm", -1);
        Intent intent = new Intent(this, AddUserActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("userAdm", userAdm);
        startActivity(intent);
        finish();
    }

    private void configureRecyclerView() {
        userDataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 假设 UserAdapter 是你的自定义适配器
        adapter = new UserAdapter(new ArrayList<>());  // 初始为空列表
        userDataRecyclerView.setAdapter(adapter);
        userDataRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                // 模拟从数据源加载新数据
                refreshUserData();
            }

            @Override
            public void onLoadMore() {
                userDataRecyclerView.refreshComplete();
            }
        });
    }

    private void refreshUserData() {
        mHandler.postDelayed(() -> {
            List<User> users = dbHelper.getAllUsers();
            allUsers = users;
            adapter.updateUserList(users);
            checkDataEmpty(users);
            userDataRecyclerView.refreshComplete(); // 数据加载完成后调用
        }, 500); // 模拟数据加载延迟
    }

    // 加载更多用户
    private void loadSampleUsers() {
        List<User> users = dbHelper.getAllUsers();
        allUsers = users;
        adapter.updateUserList(users);
        checkDataEmpty(users);
    }

    private void checkDataEmpty(List<User> users) {
        if (users.isEmpty()) {
            noDataTextView.setVisibility(View.VISIBLE);
            userDataRecyclerView.setVisibility(View.GONE);
        } else {
            noDataTextView.setVisibility(View.GONE);
            userDataRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    // 点击引入CSV文件
    private void pickFile() {
        mutalAddTextView = findViewById(R.id.mutal_tv_add);
        mutalAddTextView.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("text/comma-separated-values"); // 设置类型为 text/csv
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_CODE_PICK_CSV);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_CSV && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            readCSV(uri);
        }
    }

    private void readCSV(Uri uri) {
        List<User> all_user = new ArrayList<>();
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                // 解析每一行
                String[] tokens = line.split(",");
                if(tokens[0].equals("userName") && tokens[1].equals("password")){
                    canLoad = true;
                }
                lineCount++;
                if (lineCount == 1) {
                    continue; // 跳过第一行
                }
                if (canLoad){
                    // 生成盐值并哈希密码
                    String salt = PasswordUtils.getSalt();
                    String hashedPassword = PasswordUtils.hashPassword(tokens[1], salt);
                    all_user.add(new User(tokens[0], hashedPassword, tokens[2], tokens[3], tokens[4], Integer.parseInt(tokens[5]), Integer.parseInt(tokens[6]), Integer.parseInt(tokens[7]), Integer.parseInt(tokens[8]), Integer.parseInt(tokens[9]), 0, "", "", Integer.parseInt(tokens[10])));
                }else{
                    Toast.makeText(this, "请检查文件格式", Toast.LENGTH_SHORT).show();
                }
            }
            if (canLoad) {
                boolean isTrue = dbHelper.addUsers(all_user);
                if (isTrue) {
                    Toast.makeText(this, "导入成功", Toast.LENGTH_SHORT).show();
                    recreate();
                } else {
                    Toast.makeText(this, "导入失败", Toast.LENGTH_SHORT).show();
                }
                canLoad = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("editUsersSuccess", MODE_PRIVATE);
        String paramValue = sharedPreferences.getString("paramName", null); // 读取参数
        if (paramValue != null && paramValue.equals("success")) {
            // 参数存在，更新适配器
            loadSampleUsers();

            // 清除 SharedPreferences 中的参数，避免重复更新
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("paramName");
            editor.apply();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        searchImageView.setOnClickListener(null);
        addTextView.setOnClickListener(null);
        backButton.setOnClickListener(null);
        userDataRecyclerView.setLoadingListener(null);
        mHandler.removeCallbacksAndMessages(null);
        adapter = null;
    }

}
