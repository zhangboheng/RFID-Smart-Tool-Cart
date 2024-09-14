package com.example.keyadministrator.util;

import android.app.Application;


public class initApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.init(this);
    }

}
