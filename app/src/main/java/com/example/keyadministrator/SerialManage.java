package com.example.keyadministrator;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 串口管理类
 */
public class SerialManage {

    private static SerialManage instance;
    private ScheduledExecutorService scheduledExecutor;//线程池 同一管理保证只有一个
    private SerialHandle serialHandle;//串口连接 发送 读取处理对象
    private Queue<String> queueMsg = new ConcurrentLinkedQueue<String>();//线程安全到队列
    private ScheduledFuture sendStrTask;//循环发送任务
    private boolean isConnect = false;//串口是否连接

    private SerialManage() {
        scheduledExecutor = Executors.newScheduledThreadPool(8);//初始化8个线程
    }

    public static SerialManage getInstance() {
        if (instance == null) {
            synchronized (SerialManage.class) {
                if (instance == null) {
                    instance = new SerialManage();
                }
            }
        }
        return instance;
    }

    /**
     * 获取线程池
     *
     * @return
     */
    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    /**
     * 串口初始化
     *
     * @param serialInter
     */
    public void init(SerialInter serialInter) {
        if (serialHandle == null) {
            serialHandle = new SerialHandle();
            startSendTask();
        }
        serialHandle.addSerialInter(serialInter);

    }

    /**
     * 打开串口
     */
    public void open(String devicePath, int baudRate) {
        isConnect = serialHandle.open(devicePath, baudRate, true);//设置地址，波特率，开启读取串口数据
    }
    /**
     * 打开串口返回true or false
     */
    public boolean getOpenStatue(String devicePath, int baudRate) {
        isConnect = serialHandle.open(devicePath, baudRate, false);//设置地址，波特率，开启读取串口数据
        return isConnect;
    }

    /**
     * 发送指令
     *
     * @param msg
     */
    public void send(String msg) {
        /*
         此处没有直接使用 serialHandle.send(msg); 方法去发送指令
         因为 某些硬件在极短时间内只能响应一个指令,232通讯一次发送多个指令会有物理干扰，
         让硬件接收到指令不准确；所以 此处将指令添加到队列中，排队执行，确保每个指令一定执行.
         若不相信可以试试用serialHandle.send(msg)方法循环发送10个不同的指令，看看10个指令
         的执行结果。
         */
        queueMsg.offer(msg);//向队列添加指令
    }

    /**
     * 关闭串口
     */
    public void close() {
        serialHandle.close();//关闭串口
    }

    //启动发送发送任务
    private void startSendTask() {
        cancelSendTask(); // 先检查是否已经启动了任务？如果有，则取消
        // 在前一个任务完成执行后，每隔 100 毫秒检查一次队列中是否有新的指令需要执行
        sendStrTask = scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (!isConnect) return; // 串口未连接，退出
                if (serialHandle == null) return; // 串口未初始化，退出
                String msg = queueMsg.poll(); // 取出指令
                if (msg == null || "".equals(msg)) return; // 无效指令，退出
                serialHandle.send(msg); // 发送指令
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    //取消发送任务
    private void cancelSendTask() {
        if (sendStrTask == null) return;
        sendStrTask.cancel(true);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendStrTask = null;
    }
}
