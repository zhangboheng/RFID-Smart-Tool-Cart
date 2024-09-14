package com.example.keyadministrator;

import android.serialport.SerialPort;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 串口实处理类
 */
public class SerialHandle implements Runnable {

    private static final String TAG = "串口处理类";
    private String path = "";//串口地址
    private SerialPort mSerialPort;//串口对象
    private InputStream mInputStream;//串口的输入流对象
    private BufferedInputStream mBuffInputStream;//用于监听硬件返回的信息
    private OutputStream mOutputStream;//串口的输出流对象 用于发送指令
    private SerialInter serialInter;//串口回调接口
    private ScheduledFuture readTask;//串口读取任务

    /**
     * 添加串口回调
     *
     * @param serialInter
     */
    public void addSerialInter(SerialInter serialInter) {
        this.serialInter = serialInter;
    }

    /**
     * 打开串口
     *
     * @param devicePath 串口地址(根据平板的说明说填写)
     * @param baudrate   波特率(根据对接的硬件填写 - 硬件说明书上"通讯"中会有标注)
     * @param isRead     是否持续监听串口返回的数据
     * @return 是否打开成功
     */
    public boolean open(String devicePath, int baudrate, boolean isRead) {
        return open(devicePath, baudrate, 8, 1, 0, isRead);
    }

    /**
     * 打开串口
     *
     * @param devicePath 串口地址(根据平板的说明说填写)
     * @param baudrate   波特率(根据对接的硬件填写 - 硬件说明书上"通讯"中会有标注)
     * @param dataBits   数据位(根据对接的硬件填写 - 硬件说明书上"通讯"中会有标注)
     * @param stopBits   停止位(根据对接的硬件填写 - 硬件说明书上"通讯"中会有标注)
     * @param parity     校验位(根据对接的硬件填写 - 硬件说明书上"通讯"中会有标注)
     * @param isRead     是否持续监听串口返回的数据
     * @return 是否打开成功
     */
    public boolean open(String devicePath, int baudrate, int dataBits, int stopBits, int parity, boolean isRead) {
        boolean isSucc = false;
        try {
            if (mSerialPort != null) close();
            File device = new File(devicePath);
            mSerialPort = SerialPort // 串口对象
                    .newBuilder(device, baudrate) // 串口地址地址，波特率
                    .dataBits(dataBits) // 数据位,默认8；可选值为5~8
                    .stopBits(stopBits) // 停止位，默认1；1:1位停止位；2:2位停止位
                    .parity(parity) // 校验位；0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
                    .build(); // 打开串口并返回
            mInputStream = mSerialPort.getInputStream();
            mBuffInputStream = new BufferedInputStream(mInputStream);
            mOutputStream = mSerialPort.getOutputStream();
            isSucc = true;
            path = devicePath;
            if (isRead) readData();//开启识别
        } catch (Throwable tr) {
            close();
            isSucc = false;
        } finally {
            return isSucc;
        }
    }

    // 读取数据
    private void readData() {
        if (readTask != null) {
            readTask.cancel(true);
            try {
                Thread.sleep(160);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 此处睡眠：当取消任务时，线程池已经执行任务，无法取消，所以等待线程池的任务执行完毕
            readTask = null;
        }
        readTask = SerialManage
                .getInstance()
                .getScheduledExecutor() // 获取线程池
                .scheduleWithFixedDelay(this, 0, 150, TimeUnit.MILLISECONDS); // 执行一个循环任务，但使用 scheduleWithFixedDelay
    }

    @Override//每隔 150 毫秒会触发一次run
    public void run() {
        if (Thread.currentThread().isInterrupted()) return;
        try {
            int available = mBuffInputStream.available();
            if (available == 0) return;
            byte[] received = new byte[1024];
            int size = mBuffInputStream.read(received);
            if (size > 0 && serialInter != null) serialInter.readData(path, received, size);
        } catch (IOException e) {
            //Log.e(TAG, "串口读取数据异常:" + e.toString());
        }
    }

    /**
     * 关闭串口
     */
    public void close(){
        try{
            if (mInputStream != null) mInputStream.close();
        }catch (Exception e){
            Log.e(TAG,"串口输入流对象关闭异常：" +e.toString());
        }
        try{
            if (mOutputStream != null) mOutputStream.close();
        }catch (Exception e){
            Log.e(TAG,"串口输出流对象关闭异常：" +e.toString());
        }
        try{
            if (mSerialPort != null) mSerialPort.close();
            mSerialPort = null;
        }catch (Exception e){
            Log.e(TAG,"串口对象关闭异常：" +e.toString());
        }
    }

    /**
     * 向串口发送指令
     */
    public void send(final String msg) {
        byte[] bytes = hexStr2bytes(msg);//字符转成byte数组
        try {
            mOutputStream.write(bytes);//通过输出流写入数据
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 把十六进制表示的字节数组字符串，转换成十六进制字节数组
     *
     * @param
     * @return byte[]
     */
    private byte[] hexStr2bytes(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toUpperCase().toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (hexChar2byte(achar[pos]) << 4 | hexChar2byte(achar[pos + 1]));
        }
        return result;
    }

    /**
     * 把16进制字符[0123456789abcdef]（含大小写）转成字节
     * @param c
     * @return
     */
    private static int hexChar2byte(char c) {
        switch (c) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'a':
            case 'A':
                return 10;
            case 'b':
            case 'B':
                return 11;
            case 'c':
            case 'C':
                return 12;
            case 'd':
            case 'D':
                return 13;
            case 'e':
            case 'E':
                return 14;
            case 'f':
            case 'F':
                return 15;
            default:
                return -1;
        }
    }

}
