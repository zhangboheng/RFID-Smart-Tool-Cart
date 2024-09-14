package com.example.keyadministrator;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.keyadministrator.data.FaceRepository;
import com.example.keyadministrator.facedb.FaceDatabase;
import com.example.keyadministrator.facedb.dao.FaceDao;
import com.example.keyadministrator.facedb.entity.FaceEntity;
import com.example.keyadministrator.faceserver.FaceServer;
import com.example.keyadministrator.faceserver.RegisterFailedException;
import com.example.keyadministrator.util.FileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SocketClient {
    private static SocketClient instance; // 单例实例
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private OnConnectListener onConnectListener;
    private OnMessageReceivedListener messageListener;
    private DatabaseHelper dbHelper;
    private QueryFaceTask queryFaceTask;
    private Context context; // 添加 Context 成员变量
    private FaceDao faceDao;
    private FaceDatabase faceDatabase;
    private FaceEntity getFaceEntity;
    private FaceRepository faceRepository;
    private MutableLiveData<Boolean> initFinished = new MutableLiveData<>();
    private static final int PAGE_SIZE = 20;

    private SocketClient() { // 私有构造函数
    }
    // 添加 setContext() 方法
    public void setContext(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context); // 初始化 dbHelper
        FaceServer instance = FaceServer.getInstance();
        instance.init(ArcFaceApplication.getApplication().getApplicationContext(), faceCount -> initFinished.postValue(true));
        faceRepository = new FaceRepository(PAGE_SIZE, faceDao, instance);
    }

    // 在需要使用 FaceDatabase 时再进行初始化
    private FaceDao getFaceDao() {
        if (faceDao == null) {
            faceDatabase = FaceDatabase.getInstance(context);
            faceDao =faceDatabase.faceDao();
            queryFaceTask = new QueryFaceTask(faceDao);
        }
        return faceDao;
    }

    public static SocketClient getInstance() { // 获取单例实例
        if (instance == null) {
            instance = new SocketClient();
        }
        return instance;
    }

    public void setOnConnectListener(OnConnectListener listener) {
        this.onConnectListener = listener;
    }
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.messageListener = listener;
    }
    public void connect() {
        new Thread(() -> {
            try {
                List<Config> getConfig = dbHelper.getAllConfigs();
                String ipAddress = getConfig.get(3).getSerialPort();
                int ipPort = getConfig.get(3).getBaudRate();
                socket = new Socket(ipAddress, ipPort);
                socket.setKeepAlive(true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                if (onConnectListener != null) {
                    onConnectListener.onConnected();
                }
                receiveMessage();
            } catch (IOException e) {
                // 处理连接异常
                System.out.println("Connection failed: " + e.getMessage());
            }
        }).start();

    }

    // 传递添加用户 JSON 数据
    public void sendParameterData(String reportTime, String dataType, String dataContent) {
        new Thread(() -> {
            try {
                JSONObject jsonData = new JSONObject();
                jsonData.put("reportTime", reportTime);
                jsonData.put("dataType", dataType);
                jsonData.put("data", dataContent);
                Log.i("", "Post Data" + jsonData.toString());
                if (writer != null) {
                    writer.println(jsonData);
                }
            } catch (JSONException e) {
                // 处理 JSON 异常
                e.printStackTrace();
            }
        }).start();
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    public void receiveMessage() {
        StringBuilder messageBuilder = new StringBuilder();
        char[]buffer = new char[1024];
        int bytesRead;

        try {
            while (true) {
                bytesRead = reader.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                messageBuilder.append(buffer, 0, bytesRead);
                if (isMessageComplete(messageBuilder.toString())) {
                    String completeMessage = messageBuilder.toString();
                    JSONObject jsonObject = new JSONObject(completeMessage);
                    String dataType = jsonObject.getString("DataType");
                    // 添加用户方法
                    if (dataType.equals("add_user")){
                        String dataValue = jsonObject.getString("Data");
                        JSONObject dataJSON = new JSONObject(dataValue);
                        String username = dataJSON.getString("UserName");
                        String part = dataJSON.getString("Department");
                        String phone = dataJSON.getString("Phonenumber");
                        String password = dataJSON.getString("Password");
                        String image = dataJSON.getString("Avatar");
                        int statue = dataJSON.getInt("Status");
                        long faceId = 0;
                        String fullImage = "";
                        FaceDao faceDao = getFaceDao();
                        if (!image.isEmpty()){
                            File file =  Base64ToImage.saveBase64ToFile(image);
                            byte[] bytes = FileUtil.fileToData(file);
                            try {
                                getFaceEntity = faceRepository.registerJpeg(context, bytes, "");
                                faceId = getFaceEntity.getFaceId();
                                fullImage = getFaceEntity.getImagePath();
                                String salt = PasswordUtils.getSalt();
                                String hashedPassword = PasswordUtils.hashPassword(password, salt);
                                dbHelper.addUser(username, hashedPassword, part, phone, String.valueOf(faceId), "", 0, 0, 0,0, 0, 0, fullImage, null, null, statue);
                            }catch(Exception e){
                                Log.i("", "出现了问题");
                            }
                            queryFaceTask.cancel();
                        }

                    }
                    // 编辑用户方法
                    if (dataType.equals("update_user")){
                        String dataValue = jsonObject.getString("Data");
                        JSONObject dataJSON = new JSONObject(dataValue);
                        int userId = dataJSON.getInt("UserId");
                        String username = dataJSON.getString("UserName");
                        String part = dataJSON.getString("Department");
                        String phone = dataJSON.getString("Phonenumber");
                        String password = dataJSON.getString("Password");
                        String image = dataJSON.getString("Avatar");
                        int faceId = dataJSON.getInt("Remark");
                        String faceData = dataJSON.getString("FaceData");
                        String fullImage = "";
                        User user = dbHelper.getUserById(userId);
                        FaceDao faceDao = getFaceDao();

                        if (!image.isEmpty()){
                            String imagePath = user.getImagePath();
                            Base64ToImage.convertBase64ToImage(image, imagePath);
                            fullImage = Base64ToImage.saveBase64ToLocal(image);
                        }
                        if (user.getFaceId() != null && !user.getFaceId().isEmpty()) {
                            Integer faceIdNum = Integer.getInteger(user.getFaceId());
                            if (faceIdNum != null) {
                                queryFaceTask.getFaceById(faceIdNum, faceEntity -> {
                                    if (faceEntity != null) {
                                        getFaceEntity = faceEntity;
                                    }
                                });
                                getFaceEntity.setImagePath(fullImage);
                                queryFaceTask.updateFaceEntity(getFaceEntity);
                                queryFaceTask.cancel();
                            }
                        }
                        user.setUsername(username);
                        user.setUserPart(part);
                        user.setUserPhone(phone);
                        user.setPassword(password);
                        user.setImagePath(fullImage);
                        dbHelper.updateUser(user);

                    }
                    // 删除用户方法
                    if(dataType.equals("delete_user")){
                        String dataValue = jsonObject.getString("Data");
                        int number = Integer.parseInt(dataValue.substring(1, dataValue.length() - 1));
                        dbHelper.deleteUser(number);
                    }
                    // 添加物品方法
                    if (dataType.equals("add_goods")){
                        String dataValue = jsonObject.getString("Data");
                        JSONObject dataJSON = new JSONObject(dataValue);
                        String goodName = dataJSON.getString("GoodsName");
                        String factory = dataJSON.getString("Manufactor");
                        int lifetime = dataJSON.getInt("UsefulLife");
                        String rfid = dataJSON.getString("RfidCode");
                        int cabinetNumber = dataJSON.getInt("Cabinet");
                        int nowCabinetNumber = dataJSON.getInt("NowCabinet");
                        String belongCabinet = "";
                        String nowCabinet = "";
                        if (cabinetNumber == 0){
                            belongCabinet = "1号柜";
                        }else if (cabinetNumber == 1){
                            belongCabinet = "2号柜";
                        }else if (cabinetNumber == 2){
                            belongCabinet = "3号柜";
                        }else if (cabinetNumber == 3){
                            belongCabinet = "4号柜";
                        }
                        if (nowCabinetNumber == 0){
                            nowCabinet = "1号柜";
                        }else if (nowCabinetNumber == 1){
                            nowCabinet = "2号柜";
                        }else if (nowCabinetNumber == 2){
                            nowCabinet = "3号柜";
                        }else if (nowCabinetNumber == 3){
                            nowCabinet = "4号柜";
                        }
                        dbHelper.addGood(rfid, goodName, factory, lifetime, "", "", 1, 1, belongCabinet, nowCabinet);
                    }
                    // 编辑物品方法
                    if (dataType.equals("update_goods")){
                        String dataValue = jsonObject.getString("Data");
                        JSONObject dataJSON = new JSONObject(dataValue);
                        int goodId = dataJSON.getInt("GoodsId");
                        String goodName = dataJSON.getString("GoodsName");
                        String factory = dataJSON.getString("Manufactor");
                        int lifetime = dataJSON.getInt("UsefulLife");
                        String rfid = dataJSON.getString("RfidCode");
                        int status = dataJSON.getInt("Status");
                        int cabinetNumber = dataJSON.getInt("Cabinet");
                        int nowCabinetNumber = dataJSON.getInt("NowCabinet");
                        String belongCabinet = "";
                        String nowCabinet = "";
                        if (cabinetNumber == 0){
                            belongCabinet = "1号柜";
                        }else if (cabinetNumber == 1){
                            belongCabinet = "2号柜";
                        }else if (cabinetNumber == 2){
                            belongCabinet = "3号柜";
                        }else if (cabinetNumber == 3){
                            belongCabinet = "4号柜";
                        }
                        if (nowCabinetNumber == 0){
                            nowCabinet = "1号柜";
                        }else if (nowCabinetNumber == 1){
                            nowCabinet = "2号柜";
                        }else if (nowCabinetNumber == 2){
                            nowCabinet = "3号柜";
                        }else if (nowCabinetNumber == 3){
                            nowCabinet = "4号柜";
                        }
                        Good good = new Good(goodId, rfid, goodName, factory, lifetime, "", "", 1, 1, belongCabinet, nowCabinet, status);
                        dbHelper.updateGood(good);

                    }
                    // 删除物品方法
                    if(dataType.equals("delete_goods")){
                        String dataValue = jsonObject.getString("Data");
                        int number = Integer.parseInt(dataValue.substring(1, dataValue.length() - 1));
                        dbHelper.deleteGood(number);
                    }
                    // 添加类别方法
                    if (dataType.equals("add_goods_category")){
                        String dataValue = jsonObject.getString("Data");
                        JSONObject dataJSON = new JSONObject(dataValue);
                        String name = dataJSON.getString("CategoryName");
                        int index = dataJSON.getInt("OrderNo");
                        String unit = dataJSON.getString("Unit");
                        String image = dataJSON.getString("CategoryImage");
                        String fullImage = Base64ToImage.saveBase64ToLocal(image);
                        dbHelper.addCategory(index, name, unit, fullImage);
                    }
                    // 编辑类别方法
                    if (dataType.equals("update_goods_category")){
                        String dataValue = jsonObject.getString("Data");
                        JSONObject dataJSON = new JSONObject(dataValue);
                        int categoryId = dataJSON.getInt("CategoryId");
                        String name = dataJSON.getString("CategoryName");
                        int index = dataJSON.getInt("OrderNo");
                        String unit = dataJSON.getString("Unit");
                        String image = dataJSON.getString("CategoryImage");
                        Category category = dbHelper.getCategoryById(categoryId);
                        if (category != null) {
                            String imagePath = category.getTypeImage();
                            Base64ToImage.convertBase64ToImage(image, imagePath);
                            category.setTypeName(name);
                            category.setIndex(index);
                            category.setTypeUnit(unit);
                            dbHelper.updateCategory(category);
                        }
                    }
                    // 删除类别方法
                    if(dataType.equals("delete_goods_category")){
                        String dataValue = jsonObject.getString("Data");
                        int number = Integer.parseInt(dataValue.substring(1, dataValue.length() - 1));
                        dbHelper.deleteCategory(number);
                    }
                    // 处理完整的消息
                    messageBuilder.setLength(0); // 清空 StringBuilder，准备接收下一条消息
                }
            }
        } catch (IOException | JSONException e) {
            // 处理异常，例如尝试重新连接
            Log.e("", "读取 Socket 数据时出错: " + e.getMessage());
            if (messageListener != null) {
                messageListener.onConnectionLost(); // 通知连接丢失
            }
        }
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            // 处理断开连接异常
            System.out.println("Disconnection failed: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public interface OnConnectListener {
        void onConnected();
    }
    public interface OnMessageReceivedListener {
        void onMessageReceived(String message);

        void onConnectionLost();
    }
    private boolean isMessageComplete(String message) {
        return message.endsWith("\0"); // Check for newline delimiter
    }
}