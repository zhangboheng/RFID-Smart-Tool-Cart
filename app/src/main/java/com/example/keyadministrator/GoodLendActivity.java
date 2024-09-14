package com.example.keyadministrator;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GoodLendActivity  extends AppCompatActivity {
    private List<Good> getGoodList;
    private EditText itemNameEditText;
    private Spinner actualCabinetNumberSpinner;
    private Button queryButton;
    private RecyclerView goodsRecyclerView;

    private DatabaseHelper dbHelper;
    private GoodStoreAdapter  goodsStoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_store);

        // 初始化控件
        itemNameEditText = findViewById(R.id.itemNameEditText);
        actualCabinetNumberSpinner = findViewById(R.id.actualCabinetNumberSpinner);
        queryButton = findViewById(R.id.queryButton);
        goodsRecyclerView = findViewById(R.id.goodsRecyclerView);

        // 初始化数据库帮助类
        dbHelper = new DatabaseHelper(this);

        // 设置返回按钮事件
        returnButton();
        // 配置默认选项
        defaultOptions();
        // 初始化获取数据
        initData();
        // 设置查询点击事件
        queryClick();
    }

    // 设置返回按钮事件
    private void returnButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }
    // 配置默认选项
    private void defaultOptions() {
        // 设置归属柜子号码 Spinner 的适配器
        List<String> cabinetNumbers = dbHelper.getAllCabinets();
        ArrayAdapter<String> cabinetNumberAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, cabinetNumbers);
        cabinetNumberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actualCabinetNumberSpinner.setAdapter(cabinetNumberAdapter);
    }

    // 设置查询点击事件
    private void queryClick() {
        // 设置查询按钮的点击监听器
        queryButton.setOnClickListener(v -> {
            String itemName = itemNameEditText.getText().toString();
            String rfid = "";
            boolean overdue = false;
            String actualCabinetNumber = actualCabinetNumberSpinner.getSelectedItem().toString();
            String startTime = "";
            String endTime = "";
            // 执行查询并更新 RecyclerView
            List<Good> goodsList = dbHelper.searchGoods(itemName, rfid, actualCabinetNumber, overdue, startTime, endTime);
            List<Good> newGoodsList = new ArrayList<>();
            for(Good good : goodsList){
                if (good.getBorrowOrNOt() == 1) {
                    newGoodsList.add(good);
                }
            }
            goodsStoreAdapter.updateGoods(newGoodsList);
        });
    }

    // 初始化获取数据
    private void initData() {
        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText("已借出物品");
        // 设置 RecyclerView 的适配器和布局管理器
        getGoodList = dbHelper.getGoodsByBorrowStatus(1);
        goodsStoreAdapter = new GoodStoreAdapter(getGoodList); // 初始化为空列表
        goodsRecyclerView.setAdapter(goodsStoreAdapter);
        goodsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
