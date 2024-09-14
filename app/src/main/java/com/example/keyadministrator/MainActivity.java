package com.example.keyadministrator;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.Window;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private List<Category> categoryList;
    private final Handler mHandler = new Handler();
    private RecyclerView mRecyclerView, sRecyclerView, dRecyclerView, fullBoxRecyclerView;
    private SimpleAdapter mAdapter;
    private FullBoxAdapter fullAdapter;
    private TableAdapter adapter;
    private int mCurrentPosition = 0;
    private HashMap<String, int[]> countMap;
    private HashMap<String, int[]> countCanMap;
    private DatabaseHelper dbHelper;
    private SerialManageSecond serialManageBattery;
    private Timer timer;
    private TimerTask timerTask;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private TextView text_total, text_cabinet_1, text_cabinet_2, text_cabinet_3, text_cabinet_4;
    private TextView selectedTextView = null;
    private TextView batteryValueText;
    private List<FullBoxData> fullItems = new ArrayList<>();
    private boolean hasNotifiedLowBattery = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 创建首页模拟数据结束
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        SocketClient socketClient = SocketClient.getInstance();
        socketClient.setContext(getApplicationContext());
        socketClient.connect();

        dbHelper = new DatabaseHelper(this);

        // 只执行一次的前置操作
        preferenceSettings();
        // 获取顶部图片工具栏点击事件
        goToLoginSelect();
        // 点击物品统计跳转页面
        goToGoodTotal();
        // 点击物品库存跳转页面
        goToGoodStore();
        // 点击借出跳转页面
        goToBorrow();
    }
    // 点击物品统计跳转页面
    private void goToGoodTotal() {
        LinearLayout firstRectangle = findViewById(R.id.firstRectangle);
        firstRectangle.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GoodTotalActivity.class);
            startActivity(intent);
        });
    }
    // 点击物品库存跳转页面
    private void goToGoodStore() {
        LinearLayout secondRectangle = findViewById(R.id.secondRectangle);
        secondRectangle.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GoodStoreActivity.class);
            startActivity(intent);
        });
    }
    // 点击借出跳转页面
    private void goToBorrow() {
        LinearLayout thirdRectangle = findViewById(R.id.thirdRectangle);
        thirdRectangle.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GoodLendActivity.class);
            startActivity(intent);
        });
    }

    // 持续获取电量数据
    private void initializeBackgroundThread() {
        backgroundThread = new HandlerThread("BackgroundThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        // 创建定时任务
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // 在后台线程中执行发送指令的操作
                backgroundHandler.post(() -> {
                    serialManageBattery.send("01040000000131CA");});
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    // 停止获取电量数据
    private void stopBackgroundThread() {
        if (serialManageBattery != null){
            serialManageBattery.close();
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
        }
        backgroundHandler = null;
    }

    // 只执行一次的前置操作
    private void preferenceSettings() {
        // 设置只运行一次的方法开始
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        boolean hasRun = sharedPreferences.getBoolean("has_run_once", false);
        if (!hasRun) {
            // 在这里执行只运行一次的方法
            runOnceMethod();
            // 设置标志，指示该方法已经运行过
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("has_run_once", true);
            editor.apply();
        }
    }
    // 只执行一次的操作
    private void runOnceMethod() {
        try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
            // 在这里使用 dbHelper 对象
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            dbHelper.addUser("admin", "eb7717bb52b2d7b4c220dd0f5c8cc0e5fbebcc9c9447ef1fbe4026fa7a9df366", "", "", null, "", 1, 0, 0, 0, 0, 0, "", null,null, 1);
            db.close();
        }
    }
    // 获取当前的台账信息
    private void getCurrentBorrowData() {
        // 获取所有物品数据
        List<Good> goods = dbHelper.getAllGoods();
        List<Good> filteredGoods = new ArrayList<>();

        List<Notify> notifications = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        long currentTime = System.currentTimeMillis();

        if(goods!=null && !goods.isEmpty()){
            for(Good good : goods){
                if(good.getBorrowOrNOt() == 1){
                    filteredGoods.add(good);
                }
            }
            for (int i = 0; i < filteredGoods.size(); i++){
                String borrowStartTime = filteredGoods.get(i).getBorrowStartTime();
                int borrowDuration = filteredGoods.get(i).getBorrowDuration();
                // 检查是否需要添加通知
                if (borrowStartTime != null && !borrowStartTime.isEmpty() && borrowDuration >= 0) {
                    try {
                        Date startTime = dateFormat.parse(borrowStartTime);
                        long dueTime = startTime.getTime() + borrowDuration * 24 * 60 * 60 * 1000L;

                        if (dueTime < currentTime) {
                            // 添加通知
                            notifications.add(new Notify(i, "超期未还", filteredGoods.get(i).getTypeName(), borrowStartTime));
                            // 通过rfid找到借走之人
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (!notifications.isEmpty()){
            // 系统通知列表开始
            mRecyclerView = findViewById(R.id.tableRecyclerView);
            GridLayoutManager layoutManager = new GridLayoutManager(this, 3); // 初始列数设置为 1
            mRecyclerView.setLayoutManager(layoutManager);
            mAdapter = new SimpleAdapter(notifications);
            mRecyclerView.addItemDecoration(new GridDividerItemDecoration(this, 2, Color.BLUE)); // 2dp 灰色竖线
            mRecyclerView.setAdapter(mAdapter);

            // 自动滚动逻辑
            Runnable mScrollRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mCurrentPosition == mAdapter.getItemCount()) mCurrentPosition = 0;
                    mRecyclerView.smoothScrollToPosition(mCurrentPosition++);
                    mHandler.postDelayed(this, 1000); // 每秒滚动一次
                }
            };
            mHandler.postDelayed(mScrollRunnable, 1000);
            // 系统通知列表结束
        }
    }
    // 获取顶部图片工具栏点击事件
    private void goToLoginSelect() {
        // 获取 toolbar_icon 的引用
        ImageView toolbarIcon = findViewById(R.id.toolbar_icon);
        // 为toolbar_icon 设置点击事件监听器
        toolbarIcon.setOnClickListener(v -> {
            // 创建 Intent 对象，指定要跳转的目标 Activity
            Intent intent = new Intent(MainActivity.this, LoginSelectActivity.class);
            // 启动新的 Activity
            startActivity(intent);
        });
    }
    // 获取初始数据
    private void initialData() {
        categoryList = dbHelper.getAllCategories();
        // 提取分类需要数据
        HashMap<String, String> categoryMap = new HashMap<>();
        for(Category name : categoryList) {
            categoryMap.put(name.getTypeName(), name.getTypeImage());
        }
        // 储存物品所在的列表
        List<TableData> items = new ArrayList<>();
        countMap = dbHelper.countGoodsByTypeAndBorrowStatusForAllCabinets();
        for (Map.Entry<String, int[]> entry : countMap.entrySet()) {
            String typeName = entry.getKey();
            int[] counts = entry.getValue();
            String typeImage = categoryMap.get(typeName);
            items.add(new TableData(R.mipmap.test, typeName, "库存：" + (counts[0] - counts[1]), "借出：" + counts[1], "总计：" + counts[0], "", typeImage, ""));
        }
        // 统计列表
        sRecyclerView = findViewById(R.id.tableRecyclerView2);
        sRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TableAdapter(items, this);
        sRecyclerView.setAdapter(adapter);
        sRecyclerView.setVisibility(View.VISIBLE);

        // 第三个 Table 的表格内容
        dRecyclerView = findViewById(R.id.detailRecyclerView);
        dRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dRecyclerView.setVisibility(View.GONE);
        // 第四个 Table 的表格内容
        fullBoxRecyclerView = findViewById(R.id.firstBoxRecyclerView);
        // 第四个表格逻辑
        fullBoxRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        // 创建 Adapter 并设置点击监听器
        fullAdapter = new FullBoxAdapter(fullItems, item -> {
            // 处理 item 点击事件
            // 例如，跳转到详情页面
        });
        fullBoxRecyclerView.setAdapter(fullAdapter);
        fullBoxRecyclerView.setVisibility(View.GONE);

        FloatingActionButton roundButton = findViewById(R.id.roundButton);

        text_total = findViewById(R.id.text_total);
        text_cabinet_1 = findViewById(R.id.text_cabinet_1);
        text_cabinet_2 = findViewById(R.id.text_cabinet_2);
        text_cabinet_3 = findViewById(R.id.text_cabinet_3);
        text_cabinet_4 = findViewById(R.id.text_cabinet_4);

        text_total.setOnClickListener(v->{
            setSelectedTextView((TextView) v);
            sRecyclerView.setVisibility(View.VISIBLE);
            dRecyclerView.setVisibility(View.GONE);
            fullBoxRecyclerView.setVisibility(View.GONE);
        });

        text_cabinet_1.setOnClickListener(v->{
            setSelectedTextView((TextView) v);
            sRecyclerView.setVisibility(View.GONE);
            dRecyclerView.setVisibility(View.GONE);
            fullBoxRecyclerView.setVisibility(View.VISIBLE);
            reloadCabinetData("1号柜");
            roundButton.setVisibility(View.GONE);
        });

        text_cabinet_2.setOnClickListener(v->{
            setSelectedTextView((TextView) v);
            sRecyclerView.setVisibility(View.GONE);
            dRecyclerView.setVisibility(View.GONE);
            fullBoxRecyclerView.setVisibility(View.VISIBLE);
            reloadCabinetData("2号柜");
            roundButton.setVisibility(View.GONE);
        });

        text_cabinet_3.setOnClickListener(v->{
            setSelectedTextView((TextView) v);
            sRecyclerView.setVisibility(View.GONE);
            dRecyclerView.setVisibility(View.GONE);
            fullBoxRecyclerView.setVisibility(View.VISIBLE);
            reloadCabinetData("3号柜");
            roundButton.setVisibility(View.GONE);
        });

        text_cabinet_4.setOnClickListener(v->{
            setSelectedTextView((TextView) v);
            sRecyclerView.setVisibility(View.GONE);
            dRecyclerView.setVisibility(View.GONE);
            fullBoxRecyclerView.setVisibility(View.VISIBLE);
            reloadCabinetData("4号柜");
            roundButton.setVisibility(View.GONE);
        });

        // 隐藏第三个 RecyclerView，显示第二个 RecyclerView
        roundButton.setOnClickListener(v -> {
            dRecyclerView.setVisibility(View.GONE);
            sRecyclerView.setVisibility(View.VISIBLE);
            fullBoxRecyclerView.setVisibility(View.GONE);

            // 隐藏圆形按钮
            roundButton.setVisibility(View.GONE);
        });

        text_total.performClick();
    }

    // 添加一个方法来更新 TextView 的选中状态
    private void setSelectedTextView(TextView textView) {
        if (selectedTextView != null) {
            // 取消之前选中的 TextView 的选中状态
            selectedTextView.setBackgroundColor(Color.TRANSPARENT); // 替换为你未选中状态的背景资源
        }
        selectedTextView = textView;
        if (selectedTextView != null) {
            // 设置当前选中的 TextView 的选中状态
            selectedTextView.setBackgroundColor(Color.parseColor("#0A1F56")); // 替换为你选中状态的背景资源
        }
    }

    public List<TableData> getThirdTableDataForTypeName(String text) {
        categoryList = dbHelper.getAllCategories();
        HashMap<String, String> categoryMap = new HashMap<>();
        for(Category name : categoryList) {
            categoryMap.put(name.getTypeName(), name.getTypeImage());
        }
        List<TableData> items = new ArrayList<>();
        HashMap<String, int[]> goodItemsCount = dbHelper.countGoodsByTypeAndCabinetWithBorrow(text);
        for (Map.Entry<String, int[]> entry : goodItemsCount.entrySet()) {
            String cabinetName = entry.getKey();
            int[] counts = entry.getValue();
            String typeImage = categoryMap.get(text);
            items.add(new TableData(R.mipmap.test, cabinetName, "库存：" + counts[0], "借出：" + counts[1], "总计：" + (counts[0] + counts[1]), "", typeImage, cabinetName));
        }
        return items;
    }
    // 加载串口配置
    private void loadConfig() {

        batteryValueText = findViewById(R.id.toolbar_right_text);

        List<Config> getConfig = dbHelper.getAllConfigs();
        String clockPort = getConfig.get(2).getSerialPort();
        int clockBaudRate = getConfig.get(2).getBaudRate();

        serialManageBattery = SerialManageSecond.getInstance();
        serialManageBattery.init(new SerialInter() {
            @Override
            public void connectMsg(String path, boolean isSucc) {
                Log.i("", "串口 " + path + " 连接状态: " + (isSucc ? "成功" : "失败"));
            }

            @Override
            public void readData(String path, byte[] bytes, int size) {
                String receivedData = bytesToHex(bytes, size);
                String batteryValue = String.valueOf(hexToDecimal(receivedData.substring(6, 10)));
                int batteryValueInt = (Integer.parseInt(batteryValue) - 50) * 2;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        batteryValueText.setText(String.format("%s%%", batteryValueInt));
                        if (batteryValueInt < 20 && !hasNotifiedLowBattery) {
                            Toast.makeText(MainActivity.this, "电量不足，请及时充电", Toast.LENGTH_LONG).show();
                            MediaMp3Player.playMp3(MainActivity.this, R.raw.lowttery);
                            hasNotifiedLowBattery = true;
                        }
                        if (batteryValueInt < 10) {
                            try {
                                Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot -p"});
                                proc.waitFor();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
        serialManageBattery.open(clockPort, clockBaudRate);//打开串口
    }

    // 获取数据统计
    private void getDataAnalysis() {
        // 获取数据统计
        TextView totalThings = findViewById(R.id.totalThings);
        TextView storageThings = findViewById(R.id.storageThings);
        TextView lendThings = findViewById(R.id.lendThings);

        int[] countList = dbHelper.countTotalGoodsAndBorrowed();
        totalThings.setText(countList.length > 0 ? String.valueOf(countList[0]) : "0");
        storageThings.setText(countList.length > 0 ? String.valueOf(countList[0] - countList[1]) : "0");
        lendThings.setText(countList.length > 0 ? String.valueOf(countList[1]) : "0");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 获取当前的台账信息
        getCurrentBorrowData();
        // 获取数据统计
        getDataAnalysis();
        // 获取初始数据
        new Handler(Looper.getMainLooper()).postDelayed(()->{
            initialData();
        }, 500);
        // 获取数据
        loadConfig();
        // 持续获取电量数据
        initializeBackgroundThread();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBackgroundThread();
    }

    private void reloadCabinetData(String cabinetName) {
        categoryList = dbHelper.getAllCategories();
        // 提取分类需要数据
        HashMap<String, String> categoryMap = new HashMap<>();
        for(Category name : categoryList) {
            categoryMap.put(name.getTypeName(), name.getTypeImage());
        }
        if (fullAdapter != null) {
            fullItems.clear();
            countCanMap = dbHelper.countGoodsByTypeAndBorrowStatus(cabinetName);
            for (Map.Entry<String, int[]> entry : countCanMap.entrySet()) {
                String typeName = entry.getKey();
                int[] counts = entry.getValue();
                String typeImage = categoryMap.get(typeName);
                fullItems.add(new FullBoxData(R.mipmap.test, "名称：" + typeName,"库存：" + (counts[0] - counts[1]), "借出：" + counts[1], "总计：" + counts[0], typeImage));
            }
            fullAdapter.setItems(fullItems);
            fullAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mAdapter = null;
        adapter = null;
        fullAdapter = null;
        if (dbHelper != null){
            dbHelper.close();
        }
        if (serialManageBattery != null){
            serialManageBattery.close();
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
        }
        backgroundHandler = null;
    }

    // 将字节数组转换为十六进制字符串
    private String bytesToHex(byte[] bytes, int size) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }

    private int hexToDecimal(String hexString) {
        try {
            return Integer.parseInt(hexString, 16);
        } catch (NumberFormatException e) {
            // 处理无效的十六进制字符串
            System.err.println("Invalid hexadecimal string: " + hexString);return -1; // 或抛出异常，具体取决于您的需求
        }
    }
}