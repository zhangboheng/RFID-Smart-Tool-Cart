package com.example.keyadministrator;

import android.app.Application;
import com.example.keyadministrator.util.debug.DebugInfoDumper;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import xcrash.XCrash;

public class ArcFaceApplication extends Application {
    private static ArcFaceApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        initCrashDumper();

        // 设置全局异常处理
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
    }

    private void initCrashDumper() {
        XCrash.InitParameters initParameters = new XCrash.InitParameters();
        File dir = new File(DebugInfoDumper.CRASH_LOG_DIR);
        if (dir.isFile()){
            dir.delete();
        }
        if (!dir.exists()){
            dir.mkdirs();
        }
        initParameters.setLogDir(DebugInfoDumper.CRASH_LOG_DIR);
        XCrash.init(application, initParameters);
    }

    @Override
    public void onTerminate() {
        application = null;
        super.onTerminate();
    }

    public static ArcFaceApplication getApplication() {
        return application;
    }

    private class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            // 保存异常信息到存储或其他处理
            saveCrashInfoToFile(ex);
            // 如果需要让应用关闭，取消下面的注释
            System.exit(2);
        }

        private void saveCrashInfoToFile(Throwable ex) {
            try {
                File file = new File(getExternalFilesDir(null), "crash_log.txt");
                try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
                    ex.printStackTrace(pw);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
