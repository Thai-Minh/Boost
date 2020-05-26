package com.cleaning.boost.ibooster.service;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.activity.JunkActivity;
import com.cleaning.boost.ibooster.model.AppsListItem;
import com.cleaning.boost.ibooster.task.TaskScanCache;
import com.cleaning.boost.ibooster.utils.Constants;
import com.gmc.libs.Formatter;
import com.gmc.libs.NotificationUtils;
import com.gmc.libs.SharedPrefUtils;

import java.util.List;
import java.util.Random;

public class CheckJunkCacheReceiver extends BroadcastReceiver {

    private Context context;
    private long totalCacheSize;
    private long currTimeMillisecond;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context.getApplicationContext();

        currTimeMillisecond = System.currentTimeMillis();
        //Timber.e("OptimizeReceiver currTimeMillisecond=%d", currTimeMillisecond);

        // Kiem tra junk cache de push notification cho user. Moi lan kiem tra phai cach nhau 4h
        checkJunkCache();
    }

    /***********************************************************************************************
     * CHECK JUNK CACHE
     ***********************************************************************************************/
    private void checkJunkCache() {
        /*
         * Kiểm tra xem lần cuối cùng scan cache có cách thời điểm hiện tại SEPERATOR_SCAN_CACHE_4H
         * hay không. subLastTimeScanCache là khoảng cách từ lần scan trước tới hiện tại.
         * Nếu là lần đầu chạy thì lastTimeScanCache = 0, sẽ bỏ qua không scan cache.
         * */
        long lastTimeScanCache = SharedPrefUtils.getLong(context, Constants.SHARED_PREF_LAST_TIME_SCAN_CACHE, 0);
        //Timber.e("OptimizeReceiver lastTimeScanCache=%d", lastTimeScanCache);

        long subLastTimeScanCache = currTimeMillisecond - lastTimeScanCache;
        //Timber.e("OptimizeReceiver subLastTimeScanCache=%d", subLastTimeScanCache);

        if (lastTimeScanCache == 0)
            SharedPrefUtils.save(context, Constants.SHARED_PREF_LAST_TIME_SCAN_CACHE, currTimeMillisecond);

        if (lastTimeScanCache > 0 && subLastTimeScanCache >= Constants.TIME_SEPARATOR_CLEANED_MAX) {
            scanCache();
        }
    }

    private void scanCache() {
        try {
            if (!TaskScanCache.mIsScanning) {
                //Timber.e("OptimizeReceiver scanCache");
                TaskScanCache.OnActionListener onActionListener = new TaskScanCache.OnActionListener() {

                    @Override
                    public void onScanStarted(Context context) {
                        TaskScanCache.mIsScanning = true;
                    }

                    @Override
                    public void onScanProgressUpdated(Context context, AppsListItem appItem, int current, int max, long cacheSize) {
                        totalCacheSize = cacheSize;
                    }

                    @Override
                    public void onScanCompleted(Context context, List<AppsListItem> apps) {
                        SharedPrefUtils.save(context, Constants.SHARED_PREF_LAST_TIME_SCAN_CACHE, currTimeMillisecond);

                        if (totalCacheSize <= 0) return;
                        String[] junkMessage = {
                                "Found %s junk files, should clean your storage.",
                                "%s. Your phone has too many junk files. Clean to speed up"
                        };

                        RemoteViews mRemoteView = new RemoteViews(context.getPackageName(), R.layout.notification_junk);
                        String sInfo = String.format(junkMessage[new Random().nextInt(2)],
                                Formatter.formatFileSizeSI(context, totalCacheSize));
                        mRemoteView.setTextViewText(R.id.textViewJunkFoundSize, sInfo);

                        NotificationUtils notificationUtils = NotificationUtils.create(
                                context, Constants.NOTIFICATION_CHANNEL_ID, Constants.NOTIFICATION_CHANNEL_NAME,
                                NotificationManagerCompat.IMPORTANCE_DEFAULT).builder(NotificationCompat.VISIBILITY_PUBLIC);

                        notificationUtils.setRemoteView(mRemoteView);
                        notificationUtils.getBuilder()
                                /*.setSmallIcon(R.drawable.ic_status_bar)*/
                                .setPriority(Notification.PRIORITY_DEFAULT)
                                .setAutoCancel(true);

                        Intent junkIntent = new Intent(context, JunkActivity.class);
                        junkIntent.putExtra(Constants.NOTIFICATION_INTENT, Constants.NOTIFICATION_JUNK_FILES_ID);

                        notificationUtils.setOnClickPendingIntentActivity(R.id.btnCleanOnNotification, junkIntent);
                        notificationUtils.createNotification();
                        notificationUtils.pushNotification(Constants.NOTIFICATION_JUNK_FILES_ID);
                    }
                };
                TaskScanCache taskScanCache = new TaskScanCache(context, onActionListener);
                taskScanCache.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
