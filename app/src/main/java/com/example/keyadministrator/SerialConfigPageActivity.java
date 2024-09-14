package com.example.keyadministrator;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

public class SerialConfigPageActivity extends AppCompatActivity {
    private Spinner spinnerSerialPort, spinnerBaudRate, spinnerSerialPortTag, spinnerBaudRateTag, spinner_serial_port_meter, spinner_baud_rate_meter;
    private String serialPortSmart;
    private int baudRateSmart;
    private String serialPortTag;
    private int baudRateTag;
    private String serialPortClock;
    private int baudRateClock;
    private SerialManage serialManage;
    private SerialManageSecond serialManageSecond;
    private EditText editTextIpAddress, editTextPortNumber;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_config);

        spinnerSerialPort = findViewById(R.id.spinner_serial_port);
        spinnerBaudRate = findViewById(R.id.spinner_baud_rate);
        spinnerSerialPortTag = findViewById(R.id.spinner_serial_port_tag);
        spinnerBaudRateTag = findViewById(R.id.spinner_baud_rate_tag);
        spinner_serial_port_meter = findViewById(R.id.spinner_serial_port_meter);
        spinner_baud_rate_meter = findViewById(R.id.spinner_baud_rate_meter);
        editTextIpAddress = findViewById(R.id.editTextIpAddress);
        editTextPortNumber = findViewById(R.id.editTextPortNumber);

        dbHelper = new DatabaseHelper(this);

        // 点击返回按钮返回上一层页面
        returnButton();
        // 初始化配置选项
        initialDefaultConfig();
        // 智能锁检测按钮事件
        detectButton();
        // 点击智能锁保存按钮事件
        saveButtonSmart();
        // 智能标签检测按钮事件
        detectButtonTag();
        // 点击智能标签保存按钮事件
        saveButtonTag();
        // 智能电表检测事件
        detectButtonClock();
        // 智能电表保存事件
        saveButtonClock();
        // 点击检测ip按钮事件
        detectButtonIP();
        // ip和端口保存事件
        saveButtonIP();
    }

    // 点击返回按钮返回上一层页面
    private void returnButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
            try{
                serialManage.close();
            }catch(Exception ignored) {
            }
        });
    }

    // 初始化配置选项
    private void initialDefaultConfig() {
        // 获取
        List<Config> configList = dbHelper.getAllConfigs();

        // 设置智能锁串口选项
        ArrayAdapter<String> serialPortAdapter = new ArrayAdapter<>(this, R.layout.spinner_text_item, getSerialPortOptions());
        serialPortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSerialPort.setAdapter(serialPortAdapter);

        // 设置默认值（如果数据库中有智能锁配置）
        if (!configList.isEmpty()) {
            Config smartLockConfig = configList.get(0);
            int position = serialPortAdapter.getPosition(smartLockConfig.serialPort);
            if (position >= 0) {
                spinnerSerialPort.setSelection(position);
            }
        }


        // 设置智能锁波特率选项
        ArrayAdapter<String> baudRateAdapter = new ArrayAdapter<>(this, R.layout.spinner_text_item, getBaudRateOptions());
        baudRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBaudRate.setAdapter(baudRateAdapter);

        // 设置默认值（如果数据库中有智能锁配置）
        if (!configList.isEmpty()) {
            Config smartLockConfig = configList.get(0);
            int position = baudRateAdapter.getPosition(String.valueOf(smartLockConfig.baudRate));
            if (position >= 0) {
                spinnerBaudRate.setSelection(position);
            }
        }

        // 设置智能标签串口选项
        ArrayAdapter<String> serialPortAdapterTag = new ArrayAdapter<>(this, R.layout.spinner_text_item, getSerialPortOptions());
        serialPortAdapterTag.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSerialPortTag.setAdapter(serialPortAdapterTag);
        if (configList.size() > 1) {
            Config smartTagConfig = configList.get(1);
            int position = serialPortAdapterTag.getPosition(smartTagConfig.serialPort);
            if (position >= 0) {
                spinnerSerialPortTag.setSelection(position);
            }
        }

        // 设置智能标签波特率选项
        ArrayAdapter<String> baudRateAdapterTag = new ArrayAdapter<>(this, R.layout.spinner_text_item, getBaudRateOptions());
        baudRateAdapterTag.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBaudRateTag.setAdapter(baudRateAdapterTag);
        if (configList.size() > 1) {
            Config smartTagConfig = configList.get(1);
            int position = baudRateAdapterTag.getPosition(String.valueOf(smartTagConfig.baudRate));
            if (position >= 0) {
                spinnerBaudRateTag.setSelection(position);
            }
        }

        // 设置智能电表串口选项
        ArrayAdapter<String> serialPortAdapterClock = new ArrayAdapter<>(this, R.layout.spinner_text_item, getSerialPortOptions());
        serialPortAdapterClock.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_serial_port_meter.setAdapter(serialPortAdapterClock);

        // 设置默认值（如果数据库中有智能电表配置）
        if (!configList.isEmpty()) {
            Config smartClockConfig = configList.get(2);
            int position = serialPortAdapterClock.getPosition(smartClockConfig.serialPort);
            if (position >= 0) {
                spinner_serial_port_meter.setSelection(position);
            }
        }

        // 设置智能电表波特率选项
        ArrayAdapter<String> baudRateAdapterClock = new ArrayAdapter<>(this, R.layout.spinner_text_item, getBaudRateOptions());
        baudRateAdapterClock.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_baud_rate_meter.setAdapter(baudRateAdapterClock);

        // 设置默认值（如果数据库中有智能电表配置）
        if (!configList.isEmpty()) {
            Config smartClockConfig = configList.get(2);
            int position = baudRateAdapterClock.getPosition(String.valueOf(smartClockConfig.baudRate));
            if (position >= 0) {
                spinner_baud_rate_meter.setSelection(position);
            }
        }

        // 设置ip地址和端口号
        if (!configList.isEmpty()) {
            Config smartIpConfig = configList.get(3);
            String ipAddress = smartIpConfig.getSerialPort();
            int portNumber = smartIpConfig.getBaudRate();
            editTextIpAddress.setText(ipAddress);
            editTextPortNumber.setText(String.valueOf(portNumber));
        }
    }

    // 获取串口选项列表（您需要根据实际情况实现此方法）
    private String[] getSerialPortOptions() {
        return new String[]{"/dev/ttyS0", "/dev/ttyS1", "/dev/ttyS2", "/dev/ttyS3", "/dev/ttyS4", "/dev/ttyUSB0", "/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3", "/dev/ttyUSB4"};
    }

    // 获取波特率选项列表
    private String[] getBaudRateOptions() {
        return new String[]{"300", "600", "1200", "2400", "4800", "9600", "14400", "19200", "28800", "38400", "57600", "115200", "230400", "460800", "921600"};
    }

    // 智能锁检测按钮事件
    private void detectButton() {
        Button detectButton = findViewById(R.id.detectButtonSmart);
        detectButton.setOnClickListener(v -> {
            serialPortSmart = spinnerSerialPort.getSelectedItem().toString();
            baudRateSmart = Integer.parseInt(spinnerBaudRate.getSelectedItem().toString());
            try {
                serialManage = SerialManage.getInstance();
                serialManage.init(new SerialInter() {
                    @Override
                    public void connectMsg(String path, boolean isSucc) {

                    }

                    @Override
                    public void readData(String path, byte[] bytes, int size) {

                    }
                });
                boolean isConnected = serialManage.getOpenStatue(serialPortSmart, baudRateSmart);
                if (isConnected) {
                    Toast.makeText(this, "串口连接成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "串口连接失败", Toast.LENGTH_SHORT).show();
                }
                serialManage.close();
            }catch(Exception e){
                Toast.makeText(this, "没有该接口", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // 点击智能锁保存按钮事件
    private void saveButtonSmart() {
        Button saveButton = findViewById(R.id.saveButtonSmart);
        saveButton.setOnClickListener(v -> {
            //获取当前选择的串口和波特率
            String newSerialPort = spinnerSerialPort.getSelectedItem().toString();
            int newBaudRate = Integer.parseInt(spinnerBaudRate.getSelectedItem().toString());

            // 查询数据库获取智能锁配置的 ID
            List<Config> configList = dbHelper.getAllConfigs();
            if (!configList.isEmpty()) {
                Config smartLockConfig = configList.get(0);
                int smartLockId = smartLockConfig.id;

                // 更新数据库中的智能锁配置
                int rowsAffected= dbHelper.updateConfig(smartLockId, "智能锁", newSerialPort, newBaudRate);
                if (rowsAffected > 0) {
                    Toast.makeText(this, "智能锁配置保存成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "智能锁配置保存失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "未找到智能锁配置", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // 智能标签检测按钮事件
    private void detectButtonTag() {
        Button detectButton = findViewById(R.id.detectButtonTag);
        detectButton.setOnClickListener(v -> {
            serialPortTag = spinnerSerialPortTag.getSelectedItem().toString();
            baudRateTag = Integer.parseInt(spinnerBaudRateTag.getSelectedItem().toString());
            try {
                serialManageSecond = SerialManageSecond.getInstance();
                serialManageSecond.init(new SerialInter() {
                    @Override
                    public void connectMsg(String path, boolean isSucc) {

                    }

                    @Override
                    public void readData(String path, byte[] bytes, int size) {

                    }
                });
                boolean isConnected = serialManageSecond.getOpenStatue(serialPortTag, baudRateTag);
                if (isConnected) {
                    Toast.makeText(this, "串口连接成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "串口连接失败", Toast.LENGTH_SHORT).show();
                }
                serialManageSecond.close();
            }catch (Exception e){
                Toast.makeText(this, "没有该接口", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // 点击智能标签保存按钮事件
    private void saveButtonTag() {
        Button saveButton = findViewById(R.id.saveButtonTag);
        saveButton.setOnClickListener(v->{
            //获取当前选择的串口和波特率
            String newSerialPort = spinnerSerialPortTag.getSelectedItem().toString();
            int newBaudRate = Integer.parseInt(spinnerBaudRateTag.getSelectedItem().toString());

            // 查询数据库获取智能标签配置的 ID
            List<Config> configList = dbHelper.getAllConfigs();
            if (configList.size() > 1) {
                Config smartTagConfig = configList.get(1);
                int smartTagId = smartTagConfig.id;

                // 更新数据库中的智能标签配置
                int rowsAffected = dbHelper.updateConfig(smartTagId, "智能标签", newSerialPort, newBaudRate);
                if (rowsAffected > 0) {
                    Toast.makeText(this, "智能标签配置保存成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "智能标签配置保存失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "未找到智能标签配置", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // 智能电表检测事件
    private void detectButtonClock() {
        Button detectButton = findViewById(R.id.detectButtonMeter);
        detectButton.setOnClickListener(v -> {
            serialPortClock = spinner_serial_port_meter.getSelectedItem().toString();
            baudRateClock = Integer.parseInt(spinner_baud_rate_meter.getSelectedItem().toString());
            try {
                serialManage = SerialManage.getInstance();
                serialManage.init(new SerialInter() {
                    @Override
                    public void connectMsg(String path, boolean isSucc) {

                    }

                    @Override
                    public void readData(String path, byte[] bytes, int size) {

                    }
                });
                boolean isConnected = serialManage.getOpenStatue(serialPortClock, baudRateClock);
                if (isConnected) {
                    Toast.makeText(this, "串口连接成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "串口连接失败", Toast.LENGTH_SHORT).show();
                }
                serialManage.close();
            }catch(Exception e){
                Toast.makeText(this, "没有该接口", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // 智能电表保存事件
    private void saveButtonClock() {
        Button saveButton = findViewById(R.id.saveButtonMeter);
        saveButton.setOnClickListener(v->{
            //获取当前选择的串口和波特率
            String newSerialPort = spinner_serial_port_meter.getSelectedItem().toString();
            int newBaudRate = Integer.parseInt(spinner_baud_rate_meter.getSelectedItem().toString());

            // 查询数据库获取智能表配置的 ID
            List<Config> configList = dbHelper.getAllConfigs();
            if (configList.size() > 1) {
                Config smartClockConfig = configList.get(2);
                int smartClockId = smartClockConfig.id;

                // 更新数据库中的智能标签配置
                int rowsAffected = dbHelper.updateConfig(smartClockId, "电量显示", newSerialPort, newBaudRate);
                if (rowsAffected > 0) {
                    Toast.makeText(this, "智能电表配置保存成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "智能电表配置保存失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "未找到智能电表配置", Toast.LENGTH_SHORT).show();
            }
        });

    }
    // 点击检测ip按钮事件
    private void detectButtonIP() {
        Button detectButton = findViewById(R.id.detectButtonIP);
        detectButton.setOnClickListener(v->{
            String ipAddress = editTextIpAddress.getText().toString();
            Integer portNum = null;
            try {
                portNum = Integer.parseInt(editTextPortNumber.getText().toString());
            } catch (NumberFormatException e) {
                // 处理端口号不是有效整数的情况
                Toast.makeText(SerialConfigPageActivity.this, "请输入有效端口号", Toast.LENGTH_SHORT).show();
            }
            if (ipAddress.isEmpty() || portNum == null) {
                Toast.makeText(SerialConfigPageActivity.this, "请输入有效的 IP 地址和端口号", Toast.LENGTH_SHORT).show();
            }
            // 在后台线程中执行网络操作
            Integer finalPortNum = portNum;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(ipAddress, finalPortNum), 5000); // 设置 5 秒超时
                        socket.close();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SerialConfigPageActivity.this, "IP 和端口有效", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SerialConfigPageActivity.this, "IP 或端口无效", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).start();
        });
    }

    // ip和端口保存事件
    private void saveButtonIP() {
        Button saveButton = findViewById(R.id.saveButtonIP);
        saveButton.setOnClickListener(v->{
            //获取当前选择的串口和波特率
            String newSerialPort = editTextIpAddress.getText().toString();
            int newBaudRate = Integer.parseInt(editTextPortNumber.getText().toString());
            // 查询数据库获取智能表配置的 ID
            List<Config> configList = dbHelper.getAllConfigs();
            if (configList.size() > 1) {
                Config smartIpConfig = configList.get(3);
                int smartIpId = smartIpConfig.id;

                // 更新数据库中的智能标签配置
                int rowsAffected = dbHelper.updateConfig(smartIpId, "ip", newSerialPort, newBaudRate);
                if (rowsAffected > 0) {
                    Toast.makeText(this, "ip和端口配置保存成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "ip和端口配置保存失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "未找到ip和端口配置", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 关闭数据库连接
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (serialManage != null) {
            serialManage.close();
        }
        if (serialManageSecond != null) {
            serialManageSecond.close();
        }
    }
}
