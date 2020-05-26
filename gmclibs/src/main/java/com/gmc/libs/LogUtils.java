package com.gmc.libs;

import android.util.Log;

public class LogUtils {

    private static final String TAG = "AppLogs";
    private static boolean SHOW_LOG = false;

    public static String getTag() {return TAG;}
    public static void enableLog() {SHOW_LOG = true;}
    public static void disableLog() {SHOW_LOG = false;}

    public static void e(String tag, String message) {
        if (SHOW_LOG && message != null) {
            if (tag == null) tag = TAG;
            Log.e(tag, message);
        }
    }

    public static void e(String message) {
        e(null, message);
    }

    public static void log(String tag, String message) {
        e(tag, message);
    }

    public static void log(String message) {
        log(null, message);
    }

}
