package com.cleaning.boost.ibooster.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.activity.CPUCoolerActivity;
import com.cleaning.boost.ibooster.activity.JunkActivity;
import com.cleaning.boost.ibooster.activity.PhoneBoostActivity;

public class NotificationGerenator {

    private static final String CHANNEL = "Channel";
    private static RemoteViews expandedViews;

    //@SuppressLint("WrongConstant")
    public static Notification notifyDetail(Context context, String percentPin, String percentRam) {

        try {
            NotificationCompat.Builder notificationBuilder;
            Notification notification;
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel mChannel = notificationManager.getNotificationChannel(CHANNEL);
                if (mChannel == null) {
                    mChannel = new NotificationChannel(CHANNEL, CHANNEL, importance);
                    mChannel.enableLights(false);
                    mChannel.enableVibration(false);
                    mChannel.setShowBadge(false);
                    notificationManager.createNotificationChannel(mChannel);
                }

                notificationBuilder = new NotificationCompat.Builder(context, CHANNEL);
                notificationBuilder.setChannelId(CHANNEL);

            } else {
                notificationBuilder = new NotificationCompat.Builder(context);
            }

            setListeners(context, percentPin, percentRam);

            notificationBuilder
                    .setSmallIcon(R.drawable.ic_notifications_none_24dp)
                    .setContentTitle("")
                    .setContentText("")
                    .setAutoCancel(true)
                    .setContent(expandedViews)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    // show on lockscreen
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            notificationBuilder.setOngoing(true);
            notificationBuilder.setOnlyAlertOnce(true);
            notification = notificationBuilder.build();

            notificationManager.notify(Constants.NOTIFICATION_ID, notification);
            return notification;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void setListeners(Context context, String percentPin, String percentRam) {
        expandedViews = new RemoteViews(context.getPackageName(), R.layout.notification_app);

        expandedViews.setTextViewText(R.id.txtNotifytPercentPin, percentPin);
        expandedViews.setTextViewText(R.id.txtNotifyPercentRam, percentRam);

        /*if (tempC.equals("0")) {
            expandedViews.setViewVisibility(R.id.txtNumberOfNotify, View.INVISIBLE);
        } else {
            expandedViews.setViewVisibility(R.id.txtNumberOfNotify, View.VISIBLE);
            expandedViews.setTextViewText(R.id.txtNumberOfNotify, tempC);
        }*/

        initListener(context, Constants.NOTIFY_JUNK, JunkActivity.class, R.id.imgNotifyJunk);
        initListener(context, Constants.NOTIFY_BOOST, PhoneBoostActivity.class, R.id.imgNotifyBoost);
        initListener(context, Constants.NOTIFY_COOLER, CPUCoolerActivity.class, R.id.imgNotifyCooler);
    }

    private static void initListener(Context context, String action, Class<?> className, int viewId){
        Intent intentName = new Intent(context, className);
        intentName.setAction(action);
        PendingIntent pdiName = PendingIntent.getActivity(context, 0, intentName, PendingIntent.FLAG_UPDATE_CURRENT);
        expandedViews.setOnClickPendingIntent(viewId, pdiName);
    }
}
