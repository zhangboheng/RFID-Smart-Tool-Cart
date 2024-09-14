package com.example.keyadministrator;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GoodsControlActivity extends AppCompatActivity implements GoodsAdapter.OnItemClickListener {

    private List<Good> getGoodList;
    private EditText itemNameEditText;
    private EditText rfidEditText;
    private CheckBox overdueCheckBox;
    private Spinner actualCabinetNumberSpinner;
    private TextView startTimeTextView;
    private TextView endTimeTextView;
    private Button queryButton, addButton, importButton;
    private RecyclerView goodsRecyclerView;

    private DatabaseHelper dbHelper;
    private SocketClient socketClient;
    private GoodsAdapter goodsAdapter;

    private static final int REQUEST_CODE_PICK_CSV = 1;
    private boolean canLoad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_control);

        // 初始化控件
        itemNameEditText = findViewById(R.id.itemNameEditText);
        rfidEditText = findViewById(R.id.rfidEditText);
        overdueCheckBox = findViewById(R.id.overdueCheckBox);
        actualCabinetNumberSpinner = findViewById(R.id.actualCabinetNumberSpinner);
        startTimeTextView = findViewById(R.id.startTimeTextView);
        endTimeTextView = findViewById(R.id.endTimeTextView);
        queryButton = findViewById(R.id.queryButton);
        addButton = findViewById(R.id.addButton);
        goodsRecyclerView = findViewById(R.id.goodsRecyclerView);

        socketClient = SocketClient.getInstance();

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
        // 设置点击添加按钮选项
        addClick();
        // 设置批量添加按钮选项
        pickFile();
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
        goodsAdapter = new GoodsAdapter(getGoodList, this); // 初始化为空列表
        goodsRecyclerView.setAdapter(goodsAdapter);
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
            goodsAdapter.updateGoods(goodsList);
        });
    }

    // 设置点击添加按钮选项
    private void addClick() {
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddGoodActivity.class);
            startActivity(intent);
        });
    }

    // 设置批量添加按钮选项
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
        List<Good> all_goods = new ArrayList<>();
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "GBK"))) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                // 解析每一行
                String[] tokens = line.split(",");
                if(tokens[0].equals("rfid") && tokens[1].equals("typeName")){
                    canLoad = true;
                }
                lineCount++;
                if (lineCount == 1) {
                    continue; // 跳过第一行
                }
                if (canLoad){
                    all_goods.add(new Good(tokens[0], tokens[1], "", 99, "", "", 1, Integer.parseInt(tokens[2]), tokens[3] + "号柜", tokens[3] + "号柜", 0));
                }else{
                    Toast.makeText(this, "请检查文件格式", Toast.LENGTH_SHORT).show();
                }
            }
            if (canLoad) {
                boolean isTrue = dbHelper.addGoods(all_goods);
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
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("editGoodsSuccess", MODE_PRIVATE);
        String paramValue = sharedPreferences.getString("paramName", null); // 读取参数
        RecyclerView categoryRecyclerView = findViewById(R.id.goodsRecyclerView);
        if (paramValue != null && paramValue.equals("success")) {
            // 参数存在，更新适配器
            getGoodList.clear();
            getGoodList.addAll(dbHelper.getAllGoods()); // 重新获取所有分类数据
            goodsAdapter = new GoodsAdapter(getGoodList, this);
            categoryRecyclerView.setAdapter(goodsAdapter); // 通知适配器数据已更新

            // 清除 SharedPreferences 中的参数，避免重复更新
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("paramName");
            editor.apply();
        }
    }

    @Override
    public void onEditClick(Good good) {
        // 处理编辑点击事件
        Intent intent = new Intent(this, EditGoodActivity.class);
        intent.putExtra("goodId", good.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Good good, int position) {
        // 创建 AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(GoodsControlActivity.this);
        builder.setTitle("确认删除")
                .setMessage("确定要删除该物品吗？")
                .setPositiveButton("确认", (dialog, which) -> {
                    // 用户点击确认，执行删除操作
                    dbHelper.deleteGood(good.getId());
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        JSONArray idsArray = new JSONArray();
                        idsArray.put(good.getId());
                        JSONObject innerData = new JSONObject();innerData.put("Ids", idsArray);
                        socketClient.sendParameterData(dateFormat.format(new Date()), "delete_goods", innerData.toString());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    goodsAdapter.removeItem(position);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    // 用户点击取消，不做任何操作
                    dialog.dismiss();
                }).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}