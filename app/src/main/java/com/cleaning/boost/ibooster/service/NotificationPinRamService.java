package com.cleaning.boost.ibooster.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.cleaning.boost.ibooster.utils.Constants;
import com.cleaning.boost.ibooster.utils.NotificationGerenator;
import com.gmc.libs.LogUtils;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationPinRamService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                initReceiver();
            }
        };
        long delay = 30000L;
        Timer timer = new Timer("Timer");
        timer.schedule(timerTask, 0, delay);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initReceiver();
        return START_STICKY;
    }

    private BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                context.unregisterReceiver(this);
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int percentage = level * 100 / scale;

                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                activityManager.getMemoryInfo(mi);
                double availableMegs = mi.availMem / 0x100000L;

                double percentAvail = mi.availMem / (double)mi.totalMem * 100.0;
                double percentUse = 100.0 - percentAvail;
                DecimalFormat outputFormat = new DecimalFormat("#.#");

                Notification notification = NotificationGerenator.notifyDetail(context,
                        "Pin: " + String.valueOf(percentage) + "%",
                        "Ram: " + String.valueOf(outputFormat.format(percentUse) + "%"));
                startForeground(Constants.NOTIFICATION_ID, notification);

            } catch (Exception e){
                LogUtils.e("NotificationPinRamService: ", e.toString());
            }
        }
    };

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
        unregisterReceiver(batteryLevelReceiver);
    }
}
