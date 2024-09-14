package com.example.keyadministrator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ItemsInformationActivity extends AppCompatActivity {

    private Spinner userSpinner, operationTypeSpinner, cabinetNumberSpinner;
    private TextView startTimeTextView, endTimeTextView;
    private Button queryButton;
    private RecyclerView itemsRecyclerView;

    private final List<String> users = new ArrayList<>();
    private final List<String> operationTypes = new ArrayList<>();
    private final List<String> cabinetNumbers = new ArrayList<>();
    private final List<Item> items = new ArrayList<>(); // 替换为你实际的台账信息数据结构
    private ItemsAdapter itemsAdapter;

    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static final int PAGE_SIZE = 20; // 每页加载的项目数
    private int currentPage = 1; // 当前页码
    private boolean isLoading = false; // 是否正在加载更多数据

    DatabaseHelper dbHelper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_information);

        userSpinner = findViewById(R.id.userSpinner);
        operationTypeSpinner = findViewById(R.id.operationTypeSpinner);
        cabinetNumberSpinner = findViewById(R.id.cabinetNumberSpinner);
        startTimeTextView = findViewById(R.id.startTimeTextView);
        endTimeTextView = findViewById(R.id.endTimeTextView);
        queryButton = findViewById(R.id.queryButton);
        itemsRecyclerView = findViewById(R.id.itemsRecyclerView);

        // 返回事件方法
        returnBack();
        // 初始选项配置
        configureSettings();
        // 设置查询事件
        queryEvent();
    }
    // 返回事件方法
    private void returnBack() {
        // 初始化控件
        ImageButton backButton = findViewById(R.id.backButton);
        // 设置返回按钮监听器
        backButton.setOnClickListener(v -> finish());
    }
    // 初始选项配置
    private void configureSettings() {

        // 初始化数据
        initUsers();
        initOperationTypes();
        initCabinetNumbers();

        // 设置 Spinner 适配器
        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, users);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(userAdapter);

        ArrayAdapter<String> operationTypeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, operationTypes);
        operationTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        operationTypeSpinner.setAdapter(operationTypeAdapter);

        ArrayAdapter<String> cabinetNumberAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, cabinetNumbers);
        cabinetNumberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cabinetNumberSpinner.setAdapter(cabinetNumberAdapter);

        // 设置时间选择器
        startTimeTextView.setOnClickListener(v -> showDatePicker(true));

        endTimeTextView.setOnClickListener(v -> showDatePicker(false));

        initItems();
    }
    // 设置查询事件
    private void queryEvent() {
        // 设置查询按钮监听器
        queryButton.setOnClickListener(v -> {
            // 处理查询结果
            performQuery();
        });
    }

    // 初始化用户数据
    private void initUsers() {
        List<User> user = dbHelper.getAllUsers();
        users.add("0 所有");
        for (User u : user) {
            users.add(u.getId() + " " + u.getUsername());
        }
    }

    // 初始化操作类型数据
    private void initOperationTypes() {
        operationTypes.add("所有");
        operationTypes.add("借出");
        operationTypes.add("归还");
    }

    // 初始化柜号数据
    private void initCabinetNumbers() {
        cabinetNumbers.add("所有");
        cabinetNumbers.add("1号柜");
        cabinetNumbers.add("2号柜");
        cabinetNumbers.add("3号柜");
        cabinetNumbers.add("4号柜");
    }

    // 初始化台账信息数据
    private void initItems() {
        isLoading = true;
        List<BorrowReturnRecord> records = dbHelper.getAllBorrowReturnRecords(PAGE_SIZE, 0);
        processRecords(records);
        isLoading = false;

        // 设置 RecyclerView 适配器
        itemsAdapter = new ItemsAdapter(items, this); // 替换为你实际的 RecyclerView 适配器
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemsRecyclerView.setAdapter(itemsAdapter);

        // 添加滚动监听器以加载更多数据
        itemsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                    loadMoreItems();
                }
            }
        });
    }

    private void loadMoreItems() {
        isLoading = true;
        currentPage++;
        List<BorrowReturnRecord> records = dbHelper.getAllBorrowReturnRecords(PAGE_SIZE, currentPage * PAGE_SIZE);
        processRecords(records);
        isLoading = false;
    }

    private void processRecords(List<BorrowReturnRecord> records) {
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
        if (itemsAdapter != null) { // 检查 itemsAdapter 是否为空
            itemsAdapter.notifyDataSetChanged();
        }
    }

    // 显示日期选择器
    private void showDatePicker(final boolean isStartTime) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, dayOfMonth);
                    if (isStartTime) {
                        startCalendar = calendar;
                        startTimeTextView.setText(dateFormat.format(startCalendar.getTime()));
                    } else {
                        endCalendar = calendar;
                        endTimeTextView.setText(dateFormat.format(endCalendar.getTime()));
                    }
                },
                (isStartTime ? startCalendar : endCalendar).get(Calendar.YEAR),
                (isStartTime ? startCalendar : endCalendar).get(Calendar.MONTH),
                (isStartTime ? startCalendar : endCalendar).get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // 执行查询操作
    private void performQuery() {
        String selectedUserWithId = userSpinner.getSelectedItem().toString();
        String selectedOperationType = operationTypeSpinner.getSelectedItem().toString();
        String selectedCabinetNumber = cabinetNumberSpinner.getSelectedItem().toString();
        String startTime = startTimeTextView.getText().toString();
        String endTime = endTimeTextView.getText().toString();
        if (!startTime.isEmpty()) {
            startTime += " 00:00:00";
        }
        if (!endTime.isEmpty()) {
            endTime += " 23:59:59";
        }
        Integer userId = null;
        if (!selectedUserWithId.equals("0 所有")){
            userId = Integer.parseInt(selectedUserWithId.split(" ")[0]);
        }
        String operationType = selectedOperationType.equals("所有") ? null : selectedOperationType;
        String cabinetNumber = selectedCabinetNumber.equals("所有") ? null : selectedCabinetNumber;
        startTime = startTime.isEmpty() ? null : startTime;
        endTime = endTime.isEmpty() ? null : endTime;
        List<BorrowReturnRecord> queryResult = dbHelper.queryBorrowReturnRecords(userId, operationType, cabinetNumber, startTime, endTime);
        items.clear();
        for (BorrowReturnRecord record : queryResult) {
            String itemType = record.getItemType();
            String user = record.getUser();
            String operationTypeTrue = record.getOperationType();
            String cabinetNumberTrue = record.getCabinetNumber();
            String operationDate = record.getOperationDate();
            if (!itemType.isEmpty()) {
                List<Category> categoryList = dbHelper.searchCategories(itemType);
                String imagePath = "";
                if (!categoryList.isEmpty()) {
                    imagePath = categoryList.get(0).getTypeImage();
                }
                items.add(new Item(itemType, operationTypeTrue, user, cabinetNumberTrue, operationDate, imagePath));
            }
        }

        // 更新 RecyclerView
        itemsAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 清空列表
        items.clear();
        users.clear();
        operationTypes.clear();
        cabinetNumbers.clear();

        // 释放适配器
        itemsAdapter = null;

        // 关闭数据库连接
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
