package com.example.keyadministrator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // 网络已连接
            Toast.makeText(context, "网络已连接", Toast.LENGTH_SHORT).show();
        } else {
            // 网络未连接
            Toast.makeText(context, "网络未连接", Toast.LENGTH_SHORT).show();
        }
    }
}
