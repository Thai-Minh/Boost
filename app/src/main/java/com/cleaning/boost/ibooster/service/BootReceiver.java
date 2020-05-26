package com.cleaning.boost.ibooster.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.cleaning.boost.ibooster.task.OptimizeUtils;
import com.gmc.libs.LogUtils;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        OptimizeUtils.initOptimize(context);

        Intent service = new Intent(context, NotificationPinRamService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
        LogUtils.e("Start Service: ", "in Boot Receiver");
    }
}
