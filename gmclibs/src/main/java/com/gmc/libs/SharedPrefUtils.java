package com.gmc.libs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.RequiresFeature;

import com.google.gson.Gson;

public class SharedPrefUtils {

    private static String TAG = "SharedPrefUtils";
    private static String META_SHARED_PREF = "SHARED_PREF";
    private static SharedPreferences sharedPreferences;
    private static Gson mGson;

    @RequiresFeature(name = "SHARED_PREF", enforcement = "<meta-data android:name=\"SHARED_PREF\" android:value=\"SHARED_PREFERENCES_NAME\" />")
    public static SharedPreferences getInstant(Context context) {
        if (sharedPreferences == null) {
            String sSharedPref = ResourceUtils.getMetaDataString(context, META_SHARED_PREF);
            if (StringUtils.isNullOrEmpty(sSharedPref)) {
                sSharedPref = context.getApplicationInfo().packageName;
            }
            sharedPreferences = context.getSharedPreferences(sSharedPref, Context.MODE_PRIVATE);
            // LogUtils.e(META_SHARED_PREF, sSharedPref);
        }
        return sharedPreferences;
    }

    public static Gson getGSon() {
        if (mGson == null) mGson = new Gson();
        return mGson;
    }

    public static String getString(Context context, String key, String defaultVal) {
        return getInstant(context).getString(key, defaultVal);
    }

    public static int getInt(Context context, String key, int defaultVal) {
        return getInstant(context).getInt(key, defaultVal);
    }

    public static long getLong(Context context, String key, long defaultVal) {
        return getInstant(context).getLong(key, defaultVal);
    }

    public static boolean getBoolean(Context context, String key, boolean defaultVal) {
        return getInstant(context).getBoolean(key, defaultVal);
    }

    public static float getFloat(Context context, String key, float defaultVal) {
        return getInstant(context).getFloat(key, defaultVal);
    }

    public static void save(Context context, String key, Object value) {
        if (value == null) {
            LogUtils.log(TAG, "save object null");
            return;
        }

        try {
            SharedPreferences.Editor editor = getInstant(context).edit();
            if (value instanceof String) {
                editor.putString(key, (String) value);
            } else if (value instanceof Integer) {
                editor.putInt(key, (Integer) value);
            } else if (value instanceof Long) {
                editor.putLong(key, (Long) value);
            } else if (value instanceof Float) {
                editor.putFloat(key, (Float) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            }
            editor.commit();
        } catch (Exception e) {
            LogUtils.log(TAG, e.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Context context, String key, Class<T> anonymousClass) {
        if (anonymousClass == String.class) {
            return (T) getInstant(context).getString(key, "");
        } else if (anonymousClass == Boolean.class) {
            return (T) Boolean.valueOf(getInstant(context).getBoolean(key, false));
        } else if (anonymousClass == Float.class) {
            return (T) Float.valueOf(getInstant(context).getFloat(key, 0));
        } else if (anonymousClass == Integer.class) {
            return (T) Integer.valueOf(getInstant(context).getInt(key, 0));
        } else if (anonymousClass == Long.class) {
            return (T) Long.valueOf(getInstant(context).getLong(key, 0));
        } else {
            return (T) getGSon().fromJson(getInstant(context).getString(key, ""), anonymousClass);
        }
    }

    public static <T> void put(Context context, String key, T data) {
        SharedPreferences.Editor editor = getInstant(context).edit();
        if (data instanceof String) {
            editor.putString(key, (String) data);
        } else if (data instanceof Boolean) {
            editor.putBoolean(key, (Boolean) data);
        } else if (data instanceof Float) {
            editor.putFloat(key, (Float) data);
        } else if (data instanceof Integer) {
            editor.putInt(key, (Integer) data);
        } else if (data instanceof Long) {
            editor.putLong(key, (Long) data);
        } else {
            editor.putString(key, getGSon().toJson(data));
        }
        editor.apply();
    }

    public static void clear(Context context) {
        getInstant(context).edit().clear().apply();
    }

    public static void remove(Context context, String key) {
        getInstant(context).edit().remove(key).apply();
    }

}
