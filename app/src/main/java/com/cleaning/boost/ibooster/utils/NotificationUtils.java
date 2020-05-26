package com.cleaning.boost.ibooster.utils;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import com.gmc.libs.LogUtils;
import com.gmc.libs.SharedPrefUtils;
import com.gmc.libs.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

public class NotificationUtils {

    private static ArrayList notificationAppsWhiteList;

    public static ArrayList<String> getAppsWhiteList(Context context) {
        if (notificationAppsWhiteList == null) {
            String sWhiteList = SharedPrefUtils.getString(context,
                    Constants.SHARED_PREF_NOTIFICATION_WHITE_LIST, null);
            if (!StringUtils.isNullOrEmpty(sWhiteList)) {
                notificationAppsWhiteList = new ArrayList(Arrays.asList(sWhiteList.split(",")));
            } else {
                notificationAppsWhiteList = new ArrayList<>();
            }
        }
        return notificationAppsWhiteList;
    }

    public static String getPackageName(Notification notification) {
        try {
            ApplicationInfo ai = notification.extras.getParcelable("android.appInfo");
            if (ai != null) {
                return ai.packageName;
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }
        return null;
    }

    public static Drawable getIcon(Context context, Notification notification) {
        Icon ic;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                ic = notification.getSmallIcon();
                if (ic != null) return ic.loadDrawable(context);
            } catch (Exception e) {LogUtils.e(e.toString());
            }
        } else { // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // 19

            ApplicationInfo ai = notification.extras.getParcelable("android.appInfo");
            if (ai != null) {
                PackageManager pm = context.getPackageManager();
                try {
                    return pm.getApplicationIcon(ai.packageName);
                } catch (Exception e) {LogUtils.e(e.toString());
                }
            }

            try {
                int iconResId = notification.extras.getInt(Notification.EXTRA_SMALL_ICON);
                return ContextCompat.getDrawable(context, iconResId);
            } catch (Exception e) {LogUtils.e(e.toString());
            }

            try {
                return ContextCompat.getDrawable(context, notification.icon);
            } catch (Exception e) {LogUtils.e(e.toString());
            }

        }

        return null;
    }

    public static String getTitle(Notification notification) {
        try {
            CharSequence chars = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
            if (!TextUtils.isEmpty(chars))
                return chars.toString();
        } catch (Exception e) {LogUtils.e(e.toString());
        }
        try {
            CharSequence chars = notification.extras.getCharSequence(Notification.EXTRA_TITLE_BIG);
            if (!TextUtils.isEmpty(chars))
                return chars.toString();
        } catch (Exception e) {LogUtils.e(e.toString());
        }
        return null;
    }

    public static String getText(Context context, Notification notification) {
        try {
            CharSequence chars = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
            if (!TextUtils.isEmpty(chars)) return chars.toString();

            CharSequence[] lines = notification.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
            if (lines != null && lines.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (CharSequence msg : lines)
                    if (!TextUtils.isEmpty(msg)) {
                        sb.append(msg.toString());
                        sb.append('\n');
                    }
                return sb.toString().trim();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                chars = notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
                if (!TextUtils.isEmpty(chars)) return chars.toString();
            }

        } catch (Exception e) {LogUtils.e(e.toString());
        }

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)) {
            Parcelable[] b = (Parcelable[]) notification.extras.get(Notification.EXTRA_MESSAGES);
            if (b != null) {
                StringBuilder content = new StringBuilder();
                for (Parcelable tmp : b) {
                    Bundle msgBundle = (Bundle) tmp;
                    content.append(msgBundle.getString("text")).append("\n");
                }
                if (!TextUtils.isEmpty(content)) return content.toString();
            }
        }

        // Neu text is null, read thong tin trong RemoteViews
        try {
            RemoteViews remoteViews = getContentView(context, notification);
            if (remoteViews == null) {
                remoteViews = getBigContentView(context, notification);
            }
            return getContent(remoteViews);
        } catch (Exception e) {LogUtils.e(e.toString());
        }

        return null;
    }

    public static String getInfo(Notification notification) {
        try {
            CharSequence chars = notification.extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
            if (!TextUtils.isEmpty(chars)) return chars.toString();
        } catch (Exception e) {LogUtils.e(e.toString());
        }
        return null;
    }

    private static String getContent(RemoteViews remoteViews) {
        if (remoteViews == null) return null;

        try {

            StringBuilder stringBuilder = new StringBuilder();

            Class secretClass = remoteViews.getClass();

            Field[] outerFields = secretClass.getDeclaredFields();
            for (Field outerField : outerFields) {
                if (!outerField.getName().equals("mActions")) continue;

                outerField.setAccessible(true);

                ArrayList actions = (ArrayList) outerField.get(remoteViews);
                if (actions != null) {
                    for (Object action : actions) {

                        Field[] innerFields = action.getClass().getDeclaredFields();

                        Object value = null;
                        Object method = null;

                        for (Field field : innerFields) {
                            field.setAccessible(true);

                            if (field.getName().equals("methodName")) {
                                method = field.get(action);
                            } else if (field.getName().equals("value")) {
                                value = field.get(action);
                            }
                        }

                        if (method != null && value != null) {
                            if ("setText".equals(method.toString())) {
                                stringBuilder.append(value.toString()).append("\n");
                            }
                        }
                    }
                }

                return stringBuilder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static RemoteViews getBigContentView(Context context, Notification notification) {
        if (notification.bigContentView != null)
            return notification.bigContentView;
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Notification.Builder.recoverBuilder(context, notification).createBigContentView();
        else
            return null;
    }

    private static RemoteViews getContentView(Context context, Notification notification) {
        if (notification.contentView != null)
            return notification.contentView;
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Notification.Builder.recoverBuilder(context, notification).createContentView();
        else
            return null;
    }
}
