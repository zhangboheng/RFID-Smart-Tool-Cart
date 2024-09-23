package com.example.keyadministrator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CategoryControlActivity extends AppCompatActivity implements CategoryAdapter.OnItemClickListener{
    private List<Category> categories;
    DatabaseHelper dbHelper = new DatabaseHelper(this);
    private CategoryAdapter adapter;
    private SocketClient socketClient;
    private Button importButton;
    private static final int REQUEST_CODE_PICK_CSV = 1;
    private boolean canLoad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_control);

        socketClient = SocketClient.getInstance();

        // 返回按钮点击事件
        backButton();
        // 初始化分类管理数据
        initItems();
        // 跳转添加分类详情页面
        goToAddCategory();
        // 执行查询操作
        performQuery();
        // 点击引入CSV文件
        pickFile();
    }
    // 返回按钮点击事件
    private void backButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // 关闭当前 Activity，返回上一级
        });
    }

    // 初始化分类管理数据
    private void initItems() {
        categories = dbHelper.getAllCategories();
        RecyclerView categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        // 设置 RecyclerView 适配器
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(categories, this);
        categoryRecyclerView.setAdapter(adapter);
    }

    // 执行查询操作
    private void performQuery() {
        // 更新 RecyclerView
        Button searchButton = findViewById(R.id.searchButton);
        EditText searchEditText = findViewById(R.id.searchEditText);
        searchButton.setOnClickListener(v-> {
            String searchQuery = searchEditText.getText().toString().trim();
            performSearch(searchQuery);
        });
    }

    private void performSearch(String searchQuery) {
        List<Category> searchResults = dbHelper.searchCategories(searchQuery); // 从数据库中搜索
        // 更新适配器的数据源
        categories.clear();
        categories.addAll(searchResults);
        adapter.notifyDataSetChanged();
    }

    // 执行跳转添加分类详情页面
    private void goToAddCategory() {
        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            // 跳转到添加分类详情页面
            Intent intent = new Intent(this, CategoryManagementActivity.class);
            startActivity(intent);
        });
    }

    // 点击引入CSV文件
    private void pickFile() {
        importButton = findViewById(R.id.importButton);
        importButton.setOnClickListener(v->{
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
        List<Category> all_categories = new ArrayList<>();
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                // 解析每一行
                String[] tokens = line.split(",");
                if(tokens[0].equals("typeName") && tokens[1].equals("typeUnit")){
                    canLoad = true;
                }
                lineCount++;
                if (lineCount == 1) {
                    continue; // 跳过第一行
                }
                if (canLoad){
                    all_categories.add(new Category(1, tokens[0], tokens[1], ""));
                }else{
                    Toast.makeText(this, "请检查文件格式", Toast.LENGTH_SHORT).show();
                }
            }
            if (canLoad) {
                boolean isTrue = dbHelper.addCategories(all_categories);
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
        }
    }


    @Override
    public void onEditClick(Category category) {
        Intent intent = new Intent(this, CategoryEditActivity.class);
        intent.putExtra("categoryId", category.getId());
        intent.putExtra("index", category.getIndex());
        intent.putExtra("typeName", category.getTypeName());
        intent.putExtra("typeUnit", category.getTypeUnit());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Category category) {
        // 从数据库中删除分类
        dbHelper.deleteCategory(category.getId());
        // 从本地数据移除图片
        boolean delete = new File(category.getTypeImage()).delete();
        if (!delete) {
            Log.w("TAG", "deleteFace: failed to delete headImageFile '" + category.getTypeImage() + "'");
        }
        // 在连接成功后，可以安全地使用 writer
        try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                JSONArray idsArray = new JSONArray();
                idsArray.put(category.getId());
                // 创建内部data JSON对象并添加Ids数组
                JSONObject innerData = new JSONObject();
                innerData.put("Ids", idsArray);
                socketClient.sendParameterData(dateFormat.format(new Date()), "delete_goods_category", innerData.toString());
        } catch (JSONException e) {
                throw new RuntimeException(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("editCategorySuccess", MODE_PRIVATE);
        String paramValue = sharedPreferences.getString("paramName", null); // 读取参数
        RecyclerView categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        if (paramValue != null) {
            // 参数存在，更新适配器
            categories.clear();
            categories.addAll(dbHelper.getAllCategories()); // 重新获取所有分类数据
            adapter = new CategoryAdapter(categories, this);
            categoryRecyclerView.setAdapter(adapter); // 通知适配器数据已更新

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
    }
}