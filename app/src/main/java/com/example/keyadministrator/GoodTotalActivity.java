package com.example.keyadministrator;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GoodTotalActivity extends AppCompatActivity {

    private List<Good> getGoodList;
    private EditText itemNameEditText;
    private EditText rfidEditText;
    private CheckBox overdueCheckBox;
    private Spinner actualCabinetNumberSpinner;
    private TextView startTimeTextView;
    private TextView endTimeTextView;
    private Button queryButton;
    private RecyclerView goodsRecyclerView;

    private DatabaseHelper dbHelper;
    private GoodsTotalAdapter goodsTotalAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_total);

        // 初始化控件
        itemNameEditText = findViewById(R.id.itemNameEditText);
        rfidEditText = findViewById(R.id.rfidEditText);
        overdueCheckBox = findViewById(R.id.overdueCheckBox);
        actualCabinetNumberSpinner = findViewById(R.id.actualCabinetNumberSpinner);
        startTimeTextView = findViewById(R.id.startTimeTextView);
        endTimeTextView = findViewById(R.id.endTimeTextView);
        queryButton = findViewById(R.id.queryButton);
        goodsRecyclerView = findViewById(R.id.goodsRecyclerView);

        // 初始化数据库帮助类
        dbHelper = new DatabaseHelper(this);

        // 设置返回按钮事件
        returnButton();
        // 配置默认选项
        defaultOptions();
        // 设置起始时间事件
        dateTimeConfigure();
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

    // 设置起始时间事件
    private void dateTimeConfigure() {
        // 设置起止时间选择框的点击监听器
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        final DatePickerDialog.OnDateSetListener startTimePickerListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            startTimeTextView.setText(dateFormat.format(calendar.getTime()));
        };
        final DatePickerDialog.OnDateSetListener endTimePickerListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            endTimeTextView.setText(dateFormat.format(calendar.getTime()));
        };
        startTimeTextView.setOnClickListener(v -> new DatePickerDialog(this, startTimePickerListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show());
        endTimeTextView.setOnClickListener(v -> new DatePickerDialog(this, endTimePickerListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    // 初始化获取数据
    private void initData() {
        // 设置 RecyclerView 的适配器和布局管理器
        getGoodList = dbHelper.getAllGoods();
        goodsTotalAdapter = new GoodsTotalAdapter(getGoodList); // 初始化为空列表
        goodsRecyclerView.setAdapter(goodsTotalAdapter);
        goodsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    // 设置查询点击事件
    private void queryClick() {
        // 设置查询按钮的点击监听器
        queryButton.setOnClickListener(v -> {
            String itemName = itemNameEditText.getText().toString();
            String rfid = rfidEditText.getText().toString();
            boolean overdue = overdueCheckBox.isChecked();
            String actualCabinetNumber = actualCabinetNumberSpinner.getSelectedItem().toString();
            String startTime = startTimeTextView.getText().toString();
            String endTime = endTimeTextView.getText().toString();
            // 执行查询并更新 RecyclerView
            List<Good> goodsList = dbHelper.searchGoods(itemName, rfid, actualCabinetNumber, overdue, startTime, endTime);
            goodsTotalAdapter.updateGoods(goodsList);
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
