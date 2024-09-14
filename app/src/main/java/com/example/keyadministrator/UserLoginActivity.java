package com.example.keyadministrator;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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

import com.bumptech.glide.Glide;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class UserLoginActivity extends AppCompatActivity implements TableBorrowAdapter.OnBorrowButtonClickListener {
    private final Handler mHandler = new Handler();
    private final HashMap<String, String> goodsCabinetset = new HashMap<>();
    private DatabaseHelper dbHelper;
    private List<String> cabinetNames;
    private String userName;
    private int userGlobalId;
    private List<Category> categoryList;
    private List<Good> goodsList;
    private List<String> userCabinetNames;
    private RecyclerView mRecyclerView, sRecyclerView;
    private LinearLayout tabLayout;
    private SimpleAdapter mAdapter;
    private TableBorrowAdapter adapter;
    private int mCurrentPosition = 0;
    private HashMap<String, int[]> countMap;
    private HashMap<String, int[]> countMapDetail = new HashMap<>();
    private GClient client;
    private List<String> borrowedRfids = new ArrayList<>();
    private List<String> returnedRfids = new ArrayList<>();
    private String smartTagConfig;
    private boolean isCanOut = true;
    private boolean canOpenLock = true;
    private boolean canAllClose = true;
    private TextView countdownTextView;
    private CountDownTimer countDownTimer;
    private MediaPlayer mediaPlayer;
    private SerialManage serialManage;
    private Timer timer;
    private TimerTask timerTask;
    private SerialManageSecond serialManageBattery;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private SocketClient socketClient;
    private Timer lockTimer;
    private Timer logoutDetect = new Timer();
    private TimerTask lockTimerTask, logoutDetectTimerTask;
    private boolean isLockClosed = false; // 用于标记是否已关锁
    private TextView rightTextView,textView;
    private Button backToMain;
    private boolean isTimerRunning = false;
    private boolean firstUpdate = true;
    private boolean isConnected = false;
    private boolean isDetectRunning = true;// 控制线程运行的标志
    private List<String> missingInfo = new ArrayList<>();
    private List<String> missingAnt = new ArrayList<>();
    private boolean hasNotifiedLowBattery = false;
    private Handler greyHandler = new Handler(Looper.getMainLooper());
    private boolean isReadCardNumberEnabled = true; // 标志位，初始为可点击

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 创建首页模拟数据结束
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.active_user_login);

        dbHelper = new DatabaseHelper(this);

        countdownTextView = findViewById(R.id.countdownTextView);
        rightTextView = findViewById(R.id.rightTextView);
        backToMain = findViewById(R.id.logoutButton);

        cabinetNames = dbHelper.getAllCabinets();
        goodsList = dbHelper.getAllGoods();
        categoryList = dbHelper.getAllCategories();
        userCabinetNames = new ArrayList<>();

        sRecyclerView = findViewById(R.id.tableGoodListRecyclerView);
        tabLayout = findViewById(R.id.tabLayout);

        client = new GClient();

        // 初始化信息
        initialConfig();
        // 持续获取电量数据
        initializeBackgroundThread();
        // 退出后的操作
        backToMainAction();
    }
    // 点击物品统计跳转到物品统计页面
    private void goToTotal() {
        LinearLayout rectanglesLayout_1 = findViewById(R.id.rectanglesLayout_1);
        int userId = getIntent().getIntExtra("userId", -1);
        rectanglesLayout_1.setOnClickListener(v -> {
            Intent intent = new Intent(UserLoginActivity.this, GoodUserLendActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }
    // 判断管理员是否有能力返回主菜单
    private void backMenu() {
        ImageView userImageView = findViewById(R.id.userImageView);
        userImageView.setOnClickListener(v -> {
            int userId = getIntent().getIntExtra("userId", -1);
            int userAdm = getIntent().getIntExtra("userAdm", -1);
            User user = dbHelper.getUserById(userId);
            int code = user.isCk0();
            if (code == 1){
                Intent intent = new Intent(UserLoginActivity.this, MenuListActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userAdm", userAdm);
                startActivity(intent);
                finish();
            }
        });
    }

    // 初始化信息
    private void initialConfig() {
        socketClient = SocketClient.getInstance();
        List<Config> getConfig = dbHelper.getAllConfigs();
        String lockerPort = getConfig.get(0).getSerialPort();
        int lockerBaudRate = getConfig.get(0).getBaudRate();
        String clockPort = getConfig.get(2).getSerialPort();
        int clockBaudRate = getConfig.get(2).getBaudRate();

        smartTagConfig = getConfig.get(1).getSerialPort() + ":" + getConfig.get(1).getBaudRate();

        serialManage = SerialManage.getInstance();
        serialManage.init(new SerialInter() {
            @Override
            public void connectMsg(String path, boolean isSucc) {
                String msg = isSucc ? "成功" : "失败";
                Log.e("串口连接回调", "串口 " + path + " -连接" + msg);
            }

            @Override
            public void readData(String path, byte[] bytes, int size) {
                String receivedData = bytesToHex(bytes, size);
                // 锁关锁成功
                if (receivedData.startsWith("82")) {
                    if (receivedData.equals("8201011193") || receivedData.equals("8201021190") || receivedData.equals("8201031191") || receivedData.equals("8201041196")) {
                        runOnUiThread(() -> {
                            Toast.makeText(UserLoginActivity.this, "关锁成功", Toast.LENGTH_SHORT).show();
                            serialManage.send("8A0106119C");
                            canOpenLock = true;
                            isLockClosed = true; // 标记已关锁
                            cancelLockTimer(); // 取消定时器
                        });
                    }
                    if (receivedData.equals("8201011193")) {
                        MediaMp3Player.playMp3(UserLoginActivity.this, R.raw.close_one);
                    }else if(receivedData.equals("8201021190")){
                        MediaMp3Player.playMp3(UserLoginActivity.this, R.raw.close_two);
                    }else if(receivedData.equals("8201031191")){
                        MediaMp3Player.playMp3(UserLoginActivity.this, R.raw.close_three);
                    }else if(receivedData.equals("8201041196")){
                        MediaMp3Player.playMp3(UserLoginActivity.this, R.raw.close_four);
                    }

                }
                if (receivedData.startsWith("8A")) {
                    if (receivedData.equals("8A0101008A") || receivedData.equals("8A01020089") || receivedData.equals("8A01030088") || receivedData.equals("8A0104008F")) {
                        runOnUiThread(() -> {
                            Toast.makeText(UserLoginActivity.this, "开锁成功", Toast.LENGTH_SHORT).show();
                            serialManage.send("8A0105119F");
                            canOpenLock = false;
                            isLockClosed = false; // 标记未关锁
                            startLockTimer(); // 启动定时器
                        });
                    }
                    if (receivedData.equals("8A0101008A")) {
                        MediaMp3Player.playMp3(UserLoginActivity.this, R.raw.one);
                    }else if(receivedData.equals("8A01020089")){
                        MediaMp3Player.playMp3(UserLoginActivity.this, R.raw.two);
                    }else if (receivedData.equals("8A01030088")) {
                        MediaMp3Player.playMp3(UserLoginActivity.this, R.raw.three);
                    }else if (receivedData.equals("8A0104008F")) {
                        MediaMp3Player.playMp3(UserLoginActivity.this, R.raw.four);
                    }
                }
                // 锁状态反馈读取数值
                if (receivedData.startsWith("80")) {
                    String getBinary = Integer.toBinaryString(Integer.valueOf(receivedData.substring(4, 10), 16));

                    while (getBinary.length() < 4) {
                        getBinary= "0" + getBinary;
                    }

                    int firstBox = getBinary.charAt(3);
                    int secondBox = getBinary.charAt(2);
                    int thirdBox = getBinary.charAt(1);
                    int fourthBox = getBinary.charAt(0);

                    if (firstBox == 49 && secondBox == 49 && thirdBox == 49 && fourthBox == 49) {
                        canOpenLock = true;
                        isLockClosed = true;
                        canAllClose = true;
                        isCanOut = true;
                    }else{
                        canOpenLock = false;
                        isLockClosed = false;
                        isCanOut = false;
                        canAllClose = false;
                    }
                }
            }
        });
        //串口初始化
        serialManage.open(lockerPort, lockerBaudRate);//打开串口

        serialManageBattery = SerialManageSecond.getInstance();
        serialManageBattery.init(new SerialInter() {
            @Override
            public void connectMsg(String path, boolean isSucc) {
                String msg = isSucc ? "成功" : "失败";
                Log.e("串口连接回调", "串口 " + path + " -连接" + msg);
            }

            @Override
            public void readData(String path, byte[] bytes, int size) {
                String receivedData = bytesToHex(bytes, size);
                if (receivedData.startsWith("0104")) {
                    String batteryValue = String.valueOf(hexToDecimal(receivedData.substring(6, 10)));
                    int batteryValueInt = (Integer.parseInt(batteryValue) - 50) * 2;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rightTextView.setText(String.format("%s%%", batteryValueInt));
                            if (batteryValueInt < 20 && !hasNotifiedLowBattery) {
                                Toast.makeText(UserLoginActivity.this, "电量不足，请及时充电", Toast.LENGTH_LONG).show();
                                MediaMp3Player.playMp3(UserLoginActivity.this, R.raw.lowttery);
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
            }
        });
        serialManageBattery.open(clockPort, clockBaudRate);//打开串口
    }

    // 启动定时器
    private void startLockTimer() {
        lockTimer = new Timer();
        lockTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (!isLockClosed) { // 如果 1 分钟内没有关锁
                    runOnUiThread(() -> serialManage.send("8A0106119C"));
                }
                cancelLockTimer(); // 取消定时器
            }
        };
        lockTimer.schedule(lockTimerTask, 60 * 1000); // 1 分钟后执行任务
    }
    // 启动锁侦测定时器
    private void startLockDetectTimer() {
        new Thread(() -> {
            while (isDetectRunning) {
                try {
                    serialManage.send("80010033B2");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // 处理中断异常
                    e.printStackTrace();
                }
            }
        }).start();
    }
    // 停止线程
    private void stopLockDetectThread() {
        isDetectRunning = false;
    }

    // 取消定时器
    private void cancelLockTimer() {
        if (lockTimer != null) {
            lockTimer.cancel();
            lockTimer = null;
        }
        if (lockTimerTask != null) {
            lockTimerTask.cancel();
            lockTimerTask = null;
        }
    }

    // 启动自动退出定时器
    private void startLogoutTimer() {
        if (logoutDetectTimerTask == null) { // 仅在 TimerTask 为空时创建新任务
            logoutDetectTimerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        backToMain.performClick();
                        isTimerRunning = true;
                    });
                }
            };
            logoutDetect.schedule(logoutDetectTimerTask, 3 * 60 * 1000, 60 * 1000); // 每 3 分钟执行一次
        }
    }

    // 取消定时器
    private void cancelLogoutTimer() {
        if (logoutDetectTimerTask != null) {
            logoutDetectTimerTask.cancel();
            logoutDetectTimerTask = null;
        }
    }


    // 持续获电量数据
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
                backgroundHandler.post(() -> serialManageBattery.send("01040000000131CA"));
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    // 与读电子标签相连
    private void connectTag() {
        if (smartTagConfig.startsWith("/dev/ttyS")) {
            if (!isConnected) {
                isConnected = client.openAndroidSerial(smartTagConfig, 2000);
            }
            if (isConnected) {
                MsgBaseInventoryEpc msgBaseInventoryEpc = new MsgBaseInventoryEpc();
                msgBaseInventoryEpc.setAntennaEnable(EnumG.AntennaNo_1 | EnumG.AntennaNo_2 | EnumG.AntennaNo_3 | EnumG.AntennaNo_4);
                msgBaseInventoryEpc.setInventoryMode(EnumG.InventoryMode_Inventory);
                client.sendSynMsg(msgBaseInventoryEpc);
                MsgBaseSetTagLog msgBaseSetTagLog = new MsgBaseSetTagLog();
                msgBaseSetTagLog.setRepeatedTime(100);
                if (0 == msgBaseInventoryEpc.getRtCode()) {
                    System.out.println("Inventory epc successful.");
                } else {
                    System.out.println("Inventory epc error.");
                }
                subscribeHandler(client);
                // 7 秒后结束读取
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
                                    List<Integer> goodsFilterOut = new ArrayList<>();
                                    List<Integer> goodsFilterIn = new ArrayList<>();
                                    List<BorrowReturnRecord> returnRecords = new ArrayList<>();
                                    List<BorrowReturnRecord> borrowRecords = new ArrayList<>();

                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                    // 获取所有捕捉到的rfid，对其进行筛选未录入的数据
                                    for (String rfid : goodsCabinetset.keySet()) {
                                        Good isGoods =  dbHelper.getGoodByRfid(rfid);
                                        if (isGoods == null) {
                                            missingInfo.add(rfid);
                                            missingAnt.add(goodsCabinetset.get(rfid));
                                        }
                                    }
                                    for (Good good : goodsList) {
                                        String rfid = good.getRfid();
                                        if (goodsCabinetset.containsKey(rfid)) {
                                            if (good.getBorrowOrNOt() == 1) {
                                                returnedRfids.add(rfid);
                                                returnRecords.add(new BorrowReturnRecord(good.getTypeName(), rfid, userGlobalId, userName, "归还", good.getBelongingCabinetNumber(), dateFormat.format(new Date())));
                                                good.setBorrowStartTime("");
                                                good.setReturnDueTime("");
                                            }else if(good.getBorrowOrNOt() == 0){
                                                // 如果侦测到而状态还是在箱中，同时有可能是放错箱子了，这个时候也要增加到其中
                                                if(!Objects.equals(goodsCabinetset.get(rfid), good.getBelongingCabinetNumber())) {
                                                    returnedRfids.add(rfid);
                                                    good.setBorrowStartTime("");
                                                    good.setReturnDueTime("");
                                                }
                                            }
                                            try {
                                                // 创建内部data JSON对象并添加Ids数组
                                                JSONObject innerData = new JSONObject();
                                                innerData.put("goodsId", good.getId());
                                                innerData.put("status", 1);
                                                innerData.put("cabinet", goodsCabinetset.get(rfid));
                                                socketClient.sendParameterData(dateFormat.format(new Date()), "add_record", innerData.toString());
                                            } catch (JSONException e) {
                                                throw new RuntimeException(e);
                                            }
                                            goodsFilterIn.add(good.getId());
                                            String actualCabinets = "";
                                            String cabinetNumber = goodsCabinetset.get(rfid);
                                            if (cabinetNumber != null) {
                                                actualCabinets = cabinetNumber;
                                                good.setActualBelongingCabinetNumber(actualCabinets);
                                                dbHelper.updateGood(good);
                                            }
                                        } else {
                                            if (good.getBorrowOrNOt() == 0) {
                                                borrowedRfids.add(rfid);
                                                goodsFilterOut.add(good.getId());
                                                borrowRecords.add(new BorrowReturnRecord(good.getTypeName(), rfid, userGlobalId, userName, "借出", good.getBelongingCabinetNumber(), dateFormat.format(new Date())));
                                                good.setBorrowStartTime(dateFormat.format(new Date()));
                                                good.setReturnDueTime("");
                                                dbHelper.updateGood(good);
                                                try {
                                                    // 创建内部data JSON对象并添加Ids数组
                                                    JSONObject innerData = new JSONObject();
                                                    innerData.put("goodsId", good.getId());
                                                    innerData.put("status", 0);
                                                    innerData.put("cabinet", goodsCabinetset.get(rfid));
                                                    socketClient.sendParameterData(dateFormat.format(new Date()), "add_record", innerData.toString());
                                                } catch (JSONException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }
                                    }
                                    // 批量执行完对数据库进行
                                    dbHelper.addBorrowReturnRecords(returnRecords);
                                    dbHelper.addBorrowReturnRecords(borrowRecords);
                                    int resultOutReturn = dbHelper.updateGoodsStatusByIds(goodsFilterOut, 1);
                                    int resultInReturn = dbHelper.updateGoodsStatusByIds(goodsFilterIn, 0);
                                    goodsList = dbHelper.getAllGoods();
                                    // 创建并显示 Dialog 行为
                                    showDialogEvent();
                                }
                            });
                        }, 7000);
                }).start();
            } else {
                Toast.makeText(UserLoginActivity.this, "标签读写器连接失败，请稍后再试或检查设备", Toast.LENGTH_SHORT).show();
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

                    String antId = String.valueOf(logBaseEpcInfo.getAntId());
                    runOnUiThread(() -> goodsCabinetset.put(rfid, antId + "号柜"));
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

    // 获取登录用户信息
    private void getLoginUser() {
        ImageView userImageView = findViewById(R.id.userImageView);
        TextView nameView = findViewById(R.id.userNameTextView);
        TextView partView = findViewById(R.id.userDepartmentTextView);
        TextView timeView = findViewById(R.id.loginTimeTextView);
        int userId = getIntent().getIntExtra("userId", -1);
        if (userId != -1) {
            User currentUser = dbHelper.getUserById(userId);
            if (currentUser != null) {
                userName = currentUser.getUsername();
                userGlobalId = userId;
                nameView.setText(userName);
                partView.setText(currentUser.getUserPart());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String currentTime = dateFormat.format(new Date());
                // 设置用户图片
                String imagePath = currentUser.getImagePath();
                if (imagePath != null && !imagePath.isEmpty()) {
                    Glide.with(this).load(imagePath).into(userImageView);
                }
                timeView.setText(currentTime);
                if (currentUser.isCk1() == 1) {
                    userCabinetNames.add("1号柜");
                }
                if (currentUser.isCk2() == 1) {
                    userCabinetNames.add("2号柜");
                }
                if (currentUser.isCk3() == 1) {
                    userCabinetNames.add("3号柜");
                }
                if (currentUser.isCk4() == 1) {
                    userCabinetNames.add("4号柜");
                }
            }
        }
    }

    // 获取当前的台账信息
    private void getCurrentBorrowData() {
        // 获取该用户ID所有借出物品
        TextView totalThings = findViewById(R.id.totalThings);
        TextView totalReturnThings = findViewById(R.id.storageThings);

        int totalNum = 0;
        int beyondNum = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        List<BorrowReturnRecord> getCurrentRecord = dbHelper.getUnreturnedRecordsByUserId(userGlobalId);
        long currentTime = System.currentTimeMillis();

        // 获取是否有超期物品
        for (int i = 0; i < getCurrentRecord.size(); i++) {
            String borrowStartTime = getCurrentRecord.get(i).getOperationDate();
            String rfid = getCurrentRecord.get(i).getRfid();
            int borrowDuration = dbHelper.getGoodByRfid(rfid).getBorrowDuration();
            // 检查是否需要添加通知
            if (borrowStartTime != null && !borrowStartTime.isEmpty() && borrowDuration >= 0) {
                try {
                    Date startTime = dateFormat.parse(borrowStartTime);
                    long dueTime = startTime.getTime() + borrowDuration * 24 * 60 * 60 * 1000L;

                    if (dueTime < currentTime) {
                        beyondNum++;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            totalNum++;
        }
        totalThings.setText(String.valueOf(totalNum));
        totalReturnThings.setText(String.valueOf(beyondNum));

        if (beyondNum > 0){
            MediaMp3Player.playMp3(this, R.raw.overdue);
            try {
                // 创建内部data JSON对象并添加Ids数组
                JSONObject innerData = new JSONObject();
                innerData.put("content", "有超期物品");
                innerData.put("cabinet", "");
                socketClient.sendParameterData(dateFormat.format(new Date()), "add_alarm", innerData.toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        mRecyclerView = findViewById(R.id.tableRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3); // 初始列数设置为 1
        mRecyclerView.setLayoutManager(layoutManager);
        List<Notify> notifyList = new ArrayList<>();
        for (int i = 0; i < getCurrentRecord.size(); i++) {
            if (getCurrentRecord.get(i).getOperationType().equals("借出")) {
                notifyList.add(new Notify(i + 1, getCurrentRecord.get(i).getCabinetNumber(), getCurrentRecord.get(i).getItemType(), getCurrentRecord.get(i).getOperationDate()));
            }
        }
        mRecyclerView.addItemDecoration(new VerticalDividerItemDecoration(this, 2, Color.BLUE));
        mAdapter = new SimpleAdapter(notifyList);
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
    }

    // 获取箱子内的所有物品
    private void getAllGoodsList(List<String> itemAllGoodsList) {
        // 提取分类需要数据
        HashMap<String, String> categoryMap = new HashMap<>();
        for (Category name : categoryList) {
            categoryMap.put(name.getTypeName(), name.getTypeImage());
        }
        List<TableData> items = new ArrayList<>();
        try {
            countMap = dbHelper.countGoodsByTypeAndBorrowStatus(itemAllGoodsList.get(0));
            for (Map.Entry<String, int[]> entry : countMap.entrySet()) {
                String typeName = entry.getKey();
                int[] counts = entry.getValue();
                String typeImage = categoryMap.get(typeName);
                items.add(new TableData(R.mipmap.test, typeName, "库存：" + (counts[0] - counts[1]), "借出：" + counts[1], "总计：" + counts[0], "", typeImage, itemAllGoodsList.get(0)));
            }
        } catch (Exception e) {

        }
        // 初始化 adapter
        if (adapter == null) { // 避免重复创建 adapter
            adapter = new TableBorrowAdapter(items, this);
            sRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter.setOnBorrowButtonClickListener(this); // 设置监听
            sRecyclerView.setAdapter(adapter);
        }
        tabLayout.removeAllViews();
        // 循环展示柜子
        for (String name : itemAllGoodsList) {
            textView = new TextView(this);
            textView.setId(View.generateViewId());
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView.setPadding(32, 32, 32, 32);
            textView.setText(name);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(16);
            textView.setBackground(ContextCompat.getDrawable(this, R.drawable.tab_background));
            tabLayout.addView(textView);
            if (tabLayout.getChildCount() > 0) {
                View firstChild = tabLayout.getChildAt(0);
                if (firstChild instanceof TextView) {
                    firstChild.setSelected(true);
                }
            }
            textView.setOnClickListener(v -> {
                String caName = ((TextView) v).getText().toString();
                // TextView 设置为未选中状态
                for (int i = 0; i < tabLayout.getChildCount(); i++) {
                    View child = tabLayout.getChildAt(i);
                    if (child instanceof TextView && child != v) {
                        child.setSelected(false);
                    }
                }
                v.setSelected(true);
                countMapDetail = dbHelper.countGoodsByTypeAndBorrowStatus(caName);
                items.clear();
                for (Map.Entry<String, int[]> entry : countMapDetail.entrySet()) {
                    String typeName = entry.getKey();
                    int[] counts = entry.getValue();
                    String typeImage = categoryMap.get(typeName);
                    items.add(new TableData(R.mipmap.test, typeName, "库存：" + (counts[0] - counts[1]), "借出：" + counts[1], "总计：" + counts[0], "", typeImage, caName));
                }
                adapter.setItems(items);
                adapter.notifyDataSetChanged();
            });
        }

    }

    // 点击退出按钮后退出登录
    private void backToMainAction() {
        backToMain.setOnClickListener(v -> {
            if (isReadCardNumberEnabled) {
                isReadCardNumberEnabled = false; // 禁用按钮
                backToMain.setEnabled(false); // 置灰按钮

                logoutAction();

                // 7 秒后重新启用按钮
                greyHandler.postDelayed(() -> {
                    isReadCardNumberEnabled = true;
                    backToMain.setEnabled(true); // 恢复按钮
                }, 7000);
            }
        });
    }
    // 点击退出按钮执行的内容
    private void logoutAction() {
        if (countMap == null) {
            // 清除 SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("user_data_login", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(UserLoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // 关闭链接后再重联
            if (canOpenLock && canAllClose) {
                try {
                    borrowedRfids.clear();
                    returnedRfids.clear();
                    goodsCabinetset.clear();
                    missingInfo.clear();
                    missingAnt.clear();
                } catch (Exception e) {

                }
                // 禁用页面交互
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                // 显示倒计时提示
                countdownTextView.setVisibility(View.VISIBLE);
                // 启动倒计时
                countDownTimer = new CountDownTimer(7000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        countdownTextView.setText("盘点中，请稍后……");
                    }

                    @Override
                    public void onFinish() {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        // 隐藏倒计时提示
                        countdownTextView.setVisibility(View.GONE);
                    }
                }.start();
                connectTag();
            } else {
                Toast.makeText(UserLoginActivity.this, "柜门未关闭，请先关闭，再进行退出", Toast.LENGTH_SHORT).show();
                MediaMp3Player.playMp3(this, R.raw.close);
            }
        }
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
            System.err.println("Invalid hexadecimal string: " + hexString);
            return -1; // 或抛出异常，具体取决于您的需求
        }
    }

    @Override
    public void onBorrowButtonClick(String cabinetName) {
        if (canOpenLock) {
            switch (cabinetName) {
                case "1号柜":
                    serialManage.send("8A0101119B");
                    break;
                case "2号柜":
                    serialManage.send("8A01021198");
                    break;
                case "3号柜":
                    serialManage.send("8A01031199");
                    break;
                case "4号柜":
                    serialManage.send("8A0104119E");
                    break;
            }
        } else {
            Toast.makeText(UserLoginActivity.this, "请先关锁，才能开启其他柜子", Toast.LENGTH_SHORT).show();
            MediaMp3Player.playMp3(this, R.raw.unclose);
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                // 创建内部data JSON对象并添加Ids数组
                JSONObject innerData = new JSONObject();
                innerData.put("content", "请先关锁，才能开启其他柜子");
                innerData.put("cabinet", cabinetName);
                socketClient.sendParameterData(dateFormat.format(new Date()), "add_alarm", innerData.toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 弹出页面显示
    private void showDialogEvent() {
        Dialog dialog = new Dialog(UserLoginActivity.this);
        dialog.setContentView(R.layout.dialog_layout); // 替换 dialog_layout 为您的弹窗布局文件

        // 获取弹窗的 Window
        Window window = dialog.getWindow();
        if (window != null) {
            // 获取屏幕尺寸
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;

            // 设置弹窗的宽度和高度为屏幕的 60%
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (screenWidth * 0.8);
            params.height = (int) (screenHeight * 0.8);
            window.setAttributes(params);
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            dialog.setCanceledOnTouchOutside(false); // 点击外部时关闭弹窗
        }

        // 获取弹窗中的视图
        ImageView userAvatar = dialog.findViewById(R.id.userAvatar);
        TextView userNameTextView = dialog.findViewById(R.id.userName);
        TextView userDepartmentTextView = dialog.findViewById(R.id.userDepartment);
        TextView borrowedItemsTextView = dialog.findViewById(R.id.borrowedItems);
        TextView returnedItemsTextView = dialog.findViewById(R.id.returnedItems);
        TextView misplacedItemsTextView = dialog.findViewById(R.id.misplacedItems);
        Button closeButton = dialog.findViewById(R.id.dialogButton);
        Button dialogButton = dialog.findViewById(R.id.backButtonPrevious);

        int userId = getIntent().getIntExtra("userId", -1);
        if (userId != -1) {
            User currentUser = dbHelper.getUserById(userId);
            userNameTextView.setText(currentUser.getUsername());
            String imagePath = currentUser.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                Glide.with(this).load(imagePath).into(userAvatar);
            }
            userDepartmentTextView.setText(currentUser.getUserPart());
        }
        /*
         * 这里是处理弹出页面中的显示逻辑，包括：
         *
         * 借出物品列表
         * 归还列表
         * 放错信息列表
         *
         * */
        // 遍历本次借出的物品
        StringBuilder borrowedItemsInfo = new StringBuilder();
        if (borrowedRfids != null && !borrowedRfids.isEmpty()) {
            for (int i = 0; i < borrowedRfids.size(); i++) {
                Good good = dbHelper.getGoodByRfid(borrowedRfids.get(i));
                borrowedItemsInfo.append("编号：").append(good.getId()).append(" ").append(good.getTypeName()).append(" ").append(good.getBelongingCabinetNumber()).append(" ").append(good.getBorrowStartTime()).append(" ").append("借出时限").append(good.getBorrowDuration()).append("天").append("\n");
            }
            borrowedItemsTextView.setText(borrowedItemsInfo.toString());
        } else {
            borrowedItemsTextView.setText("无");
        }

        // 遍历本次归还的物品以及对比放错箱柜的信息
        StringBuilder returnItemsInfo = new StringBuilder();
        StringBuilder misplacedItemsInfo = new StringBuilder();
        // 获取当前时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date());
        if (returnedRfids != null && !returnedRfids.isEmpty()) {
            for (int i = 0; i < returnedRfids.size(); i++) {
                Good good = dbHelper.getGoodByRfid(returnedRfids.get(i));
                // 放错信息比较
                if (!good.getBelongingCabinetNumber().equals(good.getActualBelongingCabinetNumber()) && good.getCabinetNumberChecked() == 1) {
                    misplacedItemsInfo.append("编号：").append(good.getId()).append(" ").append(good.getTypeName()).append(" ").append("应属于：").append(good.getBelongingCabinetNumber()).append(" ").append("现位于：").append(good.getActualBelongingCabinetNumber()).append("\n");
                    switch (good.getActualBelongingCabinetNumber()) {
                        case "1号柜":
                            serialManage.send("8A0101119B");
                            break;
                        case "2号柜":
                            serialManage.send("8A01021198");
                            break;
                        case "3号柜":
                            serialManage.send("8A01031199");
                            break;
                        case "4号柜":
                            serialManage.send("8A0104119E");
                            break;
                    }
                    serialManage.send("8A0105119F");
                    isCanOut = false;
                    // 更新放错的东西状态为借出
                    good.setBorrowOrNOt(1);
                    good.setBorrowStartTime(dateFormat.format(new Date()));
                    good.setReturnDueTime("");
                    dbHelper.updateGood(good);
                    dbHelper.addBorrowReturnRecord(good.getTypeName(), returnedRfids.get(i), userGlobalId, userName, "借出", good.getBelongingCabinetNumber(), dateFormat.format(new Date()));
                } else {
                    returnItemsInfo.append("编号：").append(good.getId()).append(" ").append(good.getTypeName()).append(" ").append(good.getBelongingCabinetNumber()).append(" ").append(formattedDate).append("\n");
                }
            }
            // 判断是否有未识别物品
            if (!missingInfo.isEmpty() && !missingAnt.isEmpty()) {
                for (int j = 0; j < missingInfo.size(); j++) {
                    misplacedItemsInfo.append("未识别物品").append(" ").append("rfid：").append(missingInfo.get(j)).append(" ").append("现位于：").append(missingAnt.get(j)).append("\n");
                    switch (missingAnt.get(j)) {
                        case "1号柜":
                            serialManage.send("8A0101119B");
                            break;
                        case "2号柜":
                            serialManage.send("8A01021198");
                            break;
                        case "3号柜":
                            serialManage.send("8A01031199");
                            break;
                        case "4号柜":
                            serialManage.send("8A0104119E");
                            break;
                    }
                }
                serialManage.send("8A0105119F");
                isCanOut = false;
            }

            returnedItemsTextView.setText(returnItemsInfo.toString());

            if (misplacedItemsInfo.length() > 0) {
                misplacedItemsTextView.setText(misplacedItemsInfo.toString());
            } else {
                misplacedItemsTextView.setText("无");
            }
        } else {
            returnedItemsTextView.setText("无");
            misplacedItemsTextView.setText("无");
        }

        if (!dialog.isShowing()) {
            dialog.show();
        }

        closeButton.setVisibility((isCanOut && canAllClose) ? View.VISIBLE : View.GONE);

        // 设置标题、内容和按钮的点击事件
        closeButton.setOnClickListener(v -> {
            // 侦测锁状态
            if (isCanOut && canAllClose) {
                dialog.dismiss();
                // 清除 SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("user_data_login", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(UserLoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                isCanOut = true;
                dialog.dismiss();
            }
        });
        // 设置返回点击按钮
        dialogButton.setOnClickListener(v->{
            isCanOut = true;
            dialog.dismiss();
            // 页面刷新
            recreate();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 展示用户信息
        getLoginUser();
        if (firstUpdate) {
            getAllGoodsList(userCabinetNames); // 这里需要传入 itemAllGoodsList
            firstUpdate = false;
        }
        // 获取当前的台账信息
        getCurrentBorrowData();
        // 判断管理员是否有能力返回主菜单
        backMenu();
        // 点击物品统计跳转到物品统计页面
        goToTotal();
        // startLogoutTimer();
        startLockDetectTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        firstUpdate = false; // 在 onPause 中重置标志位
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelLockTimer();
        cancelLogoutTimer();
        stopLockDetectThread();
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (serialManage != null) {
            serialManage.close();
        }
        if (serialManageBattery != null) {
            serialManageBattery.close();
        }
        if (client != null) {
            client.close();
            client.onTagEpcLog = null;
            client.onTagEpcOver = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (logoutDetect != null) {
            logoutDetect.cancel();
            logoutDetect = null;
        }
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
        }
        backgroundHandler = null;
        backToMain.setOnClickListener(null);
    }
}
