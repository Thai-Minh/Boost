package com.cleaning.boost.ibooster;

import android.content.Intent;
import android.os.Build;

import com.cleaning.boost.ibooster.service.NotificationPinRamService;
import com.cleaning.boost.ibooster.task.OptimizeUtils;
import com.gmc.libs.DeviceUtils;
import com.gmc.libs.LogUtils;

public class MyApp extends com.orm.SugarApp {

    private static MyApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        LogUtils.disableLog();

        OptimizeUtils.initOptimize(instance);

        LogUtils.e("Country: " + DeviceUtils.getCountryCode(instance));

        Intent service = new Intent(this, NotificationPinRamService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service);
        } else {
            startService(service);
        }
        LogUtils.e("Start Service: ", "in MyApp");
    }

    public static synchronized MyApp getInstance() {
        return instance;
    }

}
