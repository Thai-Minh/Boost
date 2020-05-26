package com.gmc.libs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

public class NotificationUtils {

    private Context mContext;
    private String channelID, channelName;
    private NotificationManager notificationManager;
    public NotificationCompat.Builder builder;
    public Notification notification;
    public RemoteViews remoteViews;

    public NotificationUtils(Context context) {
        mContext = context.getApplicationContext();
    }

    private NotificationUtils createChannel(String channelID, String channelName) {
        this.channelID = channelID;
        this.channelName = channelName;
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(this.channelID, this.channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        return this;
    }

    private NotificationUtils createChannel(String channelID, String channelName, int channelImportance) {
        this.channelID = channelID;
        this.channelName = channelName;
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(this.channelID, this.channelName, channelImportance);
            notificationManager.createNotificationChannel(mChannel);
        }
        return this;
    }

    public static NotificationUtils create(Context context, String channelID, String channelName) {
        return new NotificationUtils(context).createChannel(channelID, channelName);
    }
    public static NotificationUtils create(Context context, String channelID, String channelName, int channelImportance) {
        return new NotificationUtils(context).createChannel(channelID, channelName, channelImportance);
    }

    public NotificationUtils builder() {
        builder = new NotificationCompat.Builder(mContext, channelID);
        builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
        return this;
    }

    public NotificationUtils builder(int visibility) {
        builder = new NotificationCompat.Builder(mContext, channelID);
        builder.setVisibility(visibility);
        return this;
    }

    public NotificationCompat.Builder getBuilder() {return builder;}

    public void setRemoteView(int layoutId) {
        remoteViews = new RemoteViews(mContext.getPackageName(), layoutId);
        builder.setCustomContentView(remoteViews);
    }
    public void setRemoteView(RemoteViews remoteView) {
        remoteViews = remoteView;
        builder.setCustomContentView(remoteViews);
    }
    public void setOnClickPendingIntentBroadcast(int viewId, Class<?> cls) {
        Intent intent = new Intent(mContext, cls);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(viewId, pendingIntent);
    }

    public void setOnClickPendingIntentActivity(int viewId, Intent intent) {
        PendingIntent pendingIntent =
                PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(viewId, pendingIntent);
    }

    public void setOnClickPendingIntentActivity(int viewId, Class<?> cls) {
        Intent intent = new Intent(mContext, cls);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(viewId, pendingIntent);
    }

    public void createNotification() {
        notification = builder.build();
    }

    public void pushNotification(int id) {
        notificationManager.notify(id, notification);
    }
}
