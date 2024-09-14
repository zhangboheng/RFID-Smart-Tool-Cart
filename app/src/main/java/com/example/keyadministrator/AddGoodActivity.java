package com.example.keyadministrator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.usbserial.client.AndroidUsbSerialClient;
import com.android.usbserial.client.OnUsbSerialDeviceListener;
import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.dal.HandlerTagEpcLog;
import com.gg.reader.api.dal.HandlerTagEpcOver;
import com.gg.reader.api.protocol.gx.EnumG;
import com.gg.reader.api.protocol.gx.LogBaseEpcInfo;
import com.gg.reader.api.protocol.gx.LogBaseEpcOver;
import com.gg.reader.api.protocol.gx.MsgBaseInventoryEpc;
import com.gg.reader.api.protocol.gx.MsgBaseSetTagLog;
import com.gg.reader.api.protocol.gx.MsgBaseStop;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AddGoodActivity extends AppCompatActivity {

    private static final BlockingQueue<LogBaseEpcInfo> tagQueue = new LinkedBlockingQueue<>();
    private Spinner spinnerProductName;
    private EditText editTextRFID;
    private Button buttonReadCardNumber;
    private EditText editTextManufacturer;
    private EditText editTextLifetime;
    private EditText editTextBorrowStartTime;
    private EditText editTextReturnDueTime;
    private EditText editTextBorrowDuration;
    private CheckBox checkBoxCabinetNumberChecked;
    private Spinner spinnerBelongingCabinetNumber;
    private Spinner spinnerActualBelongingCabinetNumber;
    private Button buttonCancel;
    private Button buttonSave;
    private List<Good> goodsList;
    private DatabaseHelper dbHelper;
    private GClient client;
    private SocketClient socketClient;
    private final HashSet<String> goodsCabinetset = new HashSet<>();
    private boolean isConnected = false;
    private Config getConfig;
    private Handler greyHandler = new Handler(Looper.getMainLooper());
    private boolean isReadCardNumberEnabled = true; // 标志位，初始为可点击

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_good);

        socketClient = SocketClient.getInstance();
        // 初始化控件
        spinnerProductName = findViewById(R.id.spinnerProductName);
        editTextRFID = findViewById(R.id.editTextRFID);
        buttonReadCardNumber = findViewById(R.id.readCardNumber);
        editTextManufacturer = findViewById(R.id.editTextManufacturer);
        editTextLifetime = findViewById(R.id.editTextLifetime);
        editTextBorrowStartTime = findViewById(R.id.editTextBorrowStartTime);
        editTextReturnDueTime = findViewById(R.id.editTextReturnDueTime);
        editTextBorrowDuration = findViewById(R.id.editTextBorrowDuration);
        checkBoxCabinetNumberChecked = findViewById(R.id.checkBoxCabinetNumberChecked);
        spinnerBelongingCabinetNumber = findViewById(R.id.spinnerBelongingCabinetNumber);
        // spinnerActualBelongingCabinetNumber = findViewById(R.id.spinnerActualBelongingCabinetNumber);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonSave = findViewById(R.id.buttonSave);

        // 初始化数据库助手
        dbHelper = new DatabaseHelper(this);

        goodsList = dbHelper.getAllGoods();

        getConfig = dbHelper.getConfigByName("智能标签");

        client = new GClient();

        // 设置返回按钮点击监听器
        returnButton();
        // 获取初始配置数据
        initialDefaultConfig();
        // 点击读卡按钮后运行链接
        readCardNumber();
        // 设置按钮点击取消监听器
        buttonCancelClick();
        // 设置按钮点击保存监听器
        buttonSaveClick();

    }

    // 与读电子标签相连
    private void connectTag() {
        if (getConfig.getSerialPort().startsWith("/dev/ttyS")) {
            String combinePort = getConfig.getSerialPort() + ":" + getConfig.getBaudRate();
            if (!isConnected) {
                isConnected = client.openAndroidSerial(combinePort, 2000);
            }
            if (isConnected) {
                MsgBaseInventoryEpc msgBaseInventoryEpc = new MsgBaseInventoryEpc();
                msgBaseInventoryEpc.setAntennaEnable(EnumG.AntennaNo_1 | EnumG.AntennaNo_2 | EnumG.AntennaNo_3 | EnumG.AntennaNo_4);
                msgBaseInventoryEpc.setInventoryMode(EnumG.InventoryMode_Inventory);
                client.sendSynMsg(msgBaseInventoryEpc);
                MsgBaseSetTagLog msgBaseSetTagLog = new MsgBaseSetTagLog();
                msgBaseSetTagLog.setRepeatedTime(10);
                if (0 == msgBaseInventoryEpc.getRtCode()) {
                    System.out.println("Inventory epc successful.");
                } else {
                    System.out.println("Inventory epc error.");
                }
                subscribeHandler(client);
                new Thread(() -> {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> {
                        MsgBaseStop msgBaseStop = new MsgBaseStop();
                        // 停止读取标签
                        client.sendSynMsg(msgBaseStop);
                        if (0 == msgBaseStop.getRtCode()) {
                            System.out.println("Stop successful.");
                        } else {
                            System.out.println("Stop error.");
                        }

                        // 在 UI 线程上更新结果
                        runOnUiThread(() -> {
                            if (goodsCabinetset != null) {
                                for (String epc : goodsCabinetset) {
                                    Good goodByRFID = dbHelper.getGoodByRfid(String.format("%s", epc));
                                    if (goodByRFID == null) {
                                        editTextRFID.setText(String.format("%s", epc));
                                    }
                                }
                            }

                            Toast.makeText(AddGoodActivity.this, "扫描结束", Toast.LENGTH_SHORT).show();
                        });
                    }, 5000);
                }).start();
            } else {
                Toast.makeText(AddGoodActivity.this, "标签读写器连接失败，请稍后再试或检查设备", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //订阅6c标签信息上报
    private void subscribeHandler(GClient client) {
        client.onTagEpcLog = new HandlerTagEpcLog() {
            @Override
            public void log(String s, LogBaseEpcInfo logBaseEpcInfo) {
                if (null != logBaseEpcInfo && logBaseEpcInfo.getResult() == 0) {
                    String rfid = logBaseEpcInfo.getEpc();
                    goodsCabinetset.add(rfid);
                }
            }
        };

        client.onTagEpcOver = new HandlerTagEpcOver() {
            @Override
            public void log(String s, LogBaseEpcOver logBaseEpcOver) {
                System.out.println("HandlerTagEpcOver");
            }
        };
    }

    // 点击返回按钮点击监听器
    private void returnButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            if (client != null){
                client.close();
            }
            finish();
        });
    }

    // 获取初始配置数据
    private void initialDefaultConfig() {
        // 获取产品类别列表并设置 Spinner 适配器
        List<Category> categories = dbHelper.getAllCategories();
        List<String> typeNames = new ArrayList<>();
        for (Category category : categories) {
            typeNames.add(category.getId() + " " + category.getTypeName());
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, typeNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProductName.setAdapter(categoryAdapter);

        // 获取柜子号码列表并设置 Spinner 适配器（假设你有一个 getAllCabinetNumbers() 方法）
        List<String> cabinetNumbers = dbHelper.getAllCabinets().subList(1, dbHelper.getAllCabinets().size());
        ArrayAdapter<String> cabinetNumberAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, cabinetNumbers);
        cabinetNumberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBelongingCabinetNumber.setAdapter(cabinetNumberAdapter);
        // spinnerActualBelongingCabinetNumber.setAdapter(cabinetNumberAdapter);
    }

    // 点击读卡按钮后运行链接
    private void readCardNumber() {
        buttonReadCardNumber.setOnClickListener(v -> {
            if (isReadCardNumberEnabled) {
                isReadCardNumberEnabled = false; // 禁用按钮
                buttonReadCardNumber.setEnabled(false); // 置灰按钮
                goodsCabinetset.clear();
                connectTag();
                Toast.makeText(AddGoodActivity.this, "读取中，请稍后……", Toast.LENGTH_SHORT).show();

                // 7 秒后重新启用按钮
                greyHandler.postDelayed(() -> {
                    isReadCardNumberEnabled = true;
                    buttonReadCardNumber.setEnabled(true); // 恢复按钮
                    Toast.makeText(AddGoodActivity.this, "扫描结束，等待反馈", Toast.LENGTH_SHORT).show();
                }, 7000);
            }
        });
    }

    // 设置按钮点击取消监听器
    private void buttonCancelClick() {
        buttonCancel.setOnClickListener(v -> {
            client.close();
            finish();
        });
    }

    // 设置按钮点击保存监听器
    private void buttonSaveClick() {
        //断开连接
        buttonSave.setOnClickListener(v -> {
            String categoryId = spinnerProductName.getSelectedItem().toString().split(" ")[0];
            String productName = spinnerProductName.getSelectedItem().toString().split(" ")[1];
            String rfid = editTextRFID.getText().toString();
            String manufacturer = editTextManufacturer.getText().toString();
            int lifetime = editTextLifetime.getText().toString().isEmpty() ? 99 : Integer.parseInt(editTextLifetime.getText().toString());
            String borrowStartTime = editTextBorrowStartTime.getText().toString();
            String returnDueTime = editTextReturnDueTime.getText().toString();
            int borrowDuration = editTextBorrowDuration.getText().toString().isEmpty() ? 1 : Integer.parseInt(editTextBorrowDuration.getText().toString());
            int cabinetNumberChecked = checkBoxCabinetNumberChecked.isChecked() ? 1 : 0;
            String belongingCabinetNumber = spinnerBelongingCabinetNumber.getSelectedItem().toString();
            String actualBelongingCabinetNumber = spinnerBelongingCabinetNumber.getSelectedItem().toString();
            Good goodByRFID = dbHelper.getGoodByRfid(rfid);
            if (goodByRFID != null) {
                Toast.makeText(AddGoodActivity.this, "该RFID已存在", Toast.LENGTH_SHORT).show();
                return;
            }
            // 检查必填字段是否为空
            if (productName.isEmpty() || rfid.isEmpty() || belongingCabinetNumber.isEmpty() || actualBelongingCabinetNumber.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("以下字段不能为空：");
                if (productName.isEmpty()) errorMessage.append("\n物品名称");
                if (rfid.isEmpty()) errorMessage.append("\nRFID");
                if (belongingCabinetNumber.isEmpty()) errorMessage.append("\n所属柜号");
                if (actualBelongingCabinetNumber.isEmpty()) errorMessage.append("\n实际所属柜号");

                Toast.makeText(AddGoodActivity.this, errorMessage.toString(), Toast.LENGTH_SHORT).show();
                return; // 停止保存操作
            }

            long newRowId = dbHelper.addGood(rfid, productName, manufacturer, lifetime, borrowStartTime, returnDueTime, borrowDuration, cabinetNumberChecked, belongingCabinetNumber, actualBelongingCabinetNumber);

            if (newRowId != -1) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    // 创建内部data JSON对象并添加Ids数组
                    JSONObject innerData = new JSONObject();
                    innerData.put("goodsName", productName);
                    innerData.put("goodsCategoryId", categoryId);
                    innerData.put("manufactor", manufacturer);
                    innerData.put("usefulLife", lifetime);
                    innerData.put("rfidCode", rfid);
                    innerData.put("status", 0);
                    String cabinetsTry = "0";
                    if (belongingCabinetNumber.equals("1号柜")) {
                        cabinetsTry = "1";
                    } else if (belongingCabinetNumber.equals("2号柜")) {
                        cabinetsTry = "2";
                    } else if (belongingCabinetNumber.equals("3号柜")) {
                        cabinetsTry = "3";
                    } else if (belongingCabinetNumber.equals("4号柜")) {
                        cabinetsTry = "4";
                    }
                    innerData.put("cabinet", cabinetsTry);
                    int cabinetsNow = 0;
                    if (actualBelongingCabinetNumber.equals("1号柜")) {
                        cabinetsNow = 0;
                    } else if (actualBelongingCabinetNumber.equals("2号柜")) {
                        cabinetsNow = 1;
                    } else if (actualBelongingCabinetNumber.equals("3号柜")) {
                        cabinetsNow = 2;
                    } else if (actualBelongingCabinetNumber.equals("4号柜")) {
                        cabinetsNow = 3;
                    }
                    innerData.put("nowCabinet", cabinetsNow);
                    socketClient.sendParameterData(dateFormat.format(new Date()), "add_goods", innerData.toString());

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                Toast.makeText(AddGoodActivity.this, "物品信息保存成功", Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPreferences = getSharedPreferences("editGoodsSuccess", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("paramName", "success"); // 保存参数
                editor.apply();
                client.close();
                finish(); // 关闭 Activity
            } else {
                Toast.makeText(AddGoodActivity.this, "物品信息保存失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放资源
        if (client != null) {
            client.close();
        }
        goodsCabinetset.clear();
        if (dbHelper != null) {
            dbHelper.close();
        }
        greyHandler.removeCallbacksAndMessages(null);
    }
}