package com.example.keyadministrator.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.gg.reader.api.utils.StringUtils;

public class ToastUtils {

    // 构造方法私有化 不允许new对象
    private ToastUtils() {
    }

    private static Toast looperToast = null;
    private static Toast toast = null;
    private static Handler handler = new Handler();

    public static void init(Context context) {
        toast = Toast.makeText(context, null, Toast.LENGTH_SHORT);
    }

    public static void showText(String text) {
        if (!StringUtils.isNullOfEmpty(text)) {
            toast.setText(text);
        } else {
            toast.setText("通信超时");
        }
        toast.show();
    }


    public static void handlerText(String text) {
        if (!StringUtils.isNullOfEmpty(text)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    toast.setText(text);
                    toast.show();
                }
            });
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    toast.setText("TimeOut");
                    toast.show();
                }
            });
        }

    }


    public static void showLooperText(Context context, String text) {
        try {
            if (looperToast == null) {
                looperToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
            }
            looperToast.setText(text);
            looperToast.show();
        } catch (Exception e) {
            Looper.prepare();
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            Looper.loop();
        }

    }

    public static void showText(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }
        if (!StringUtils.isNullOfEmpty(text)) {
            toast.setText(text);
        } else {
            toast.setText("未知错误");
        }
        toast.show();
    }
}
