package com.example.keyadministrator;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GoodUserLendActivity extends AppCompatActivity {
    private List<Item> items = new ArrayList<>();
    private RecyclerView itemsRecyclerView;

    private DatabaseHelper dbHelper;
    private ItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_good_total);

        itemsRecyclerView = findViewById(R.id.goodsRecyclerView);

        // 初始化数据库帮助类
        dbHelper = new DatabaseHelper(this);

        // 设置返回按钮事件
        returnButton();
        // 初始化获取数据
        initData();
    }

    // 设置返回按钮事件
    private void returnButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    // 初始化获取数据
    private void initData() {
        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText("物品统计");
        int userId = getIntent().getIntExtra("userId", -1);
        // 设置 RecyclerView 的适配器和布局管理器
        List<BorrowReturnRecord> records = dbHelper.getUnreturnedRecordsByUserId(userId);
        for (BorrowReturnRecord record : records) {
            String itemType = record.getItemType();
            String user = record.getUser();
            String operationType = record.getOperationType();
            String cabinetNumber = record.getCabinetNumber();
            String operationDate = record.getOperationDate();
            if (!itemType.isEmpty()) {
                List<Category> categoryList = dbHelper.searchCategories(itemType);
                String imagePath = "";
                if (!categoryList.isEmpty()) {
                    imagePath = categoryList.get(0).getTypeImage();
                }
                items.add(new Item(itemType, operationType, user, cabinetNumber, operationDate, imagePath));
            }
        }
        itemsAdapter = new ItemsAdapter(items, this); // 初始化为空列表
        itemsRecyclerView.setAdapter(itemsAdapter);
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (itemsAdapter != null) { // 检查 itemsAdapter 是否为空
            itemsAdapter.notifyDataSetChanged();
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
