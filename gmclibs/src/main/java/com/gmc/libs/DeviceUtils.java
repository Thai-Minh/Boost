package com.gmc.libs;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresPermission;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class DeviceUtils {

    private static final String TAG = "DeviceUtils";

    @RequiresPermission("android.permission.ACCESS_WIFI_STATE")
    public static String getWLanMacAddress(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            return wifiManager.getConnectionInfo().getMacAddress();
        } catch (Exception e) {
        }
        return null;
    }

    @RequiresPermission("android.permission.BLUETOOTH")
    public static String getBluetoothAddress() {
        try {
            return BluetoothAdapter.getDefaultAdapter().getAddress();
        } catch (Exception e) {
        }
        return null;
    }

    public static String getAndroidID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @RequiresPermission("android.permission.READ_PHONE_STATE")
    public static String getIMEI(Context context) {
        try {
            TelephonyManager telephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyMgr.getDeviceId();
        } catch (Exception e) {
        }
        return null;
    }

    @RequiresPermission(allOf = {"android.permission.ACCESS_WIFI_STATE",
            "android.permission.BLUETOOTH", "android.permission.READ_PHONE_STATE"})
    public static String getDeviceID(Context context) {

        String deviceID = SharedPrefUtils.getString(context, Constants.Key.DEVICE_ID, "");
        try {
            if (!StringUtils.isNullOrEmpty(deviceID)) return deviceID;

            deviceID = getAndroidID(context);
            if (!StringUtils.isNullOrEmpty(deviceID)) return deviceID;

            deviceID = getWLanMacAddress(context);
            if (!StringUtils.isNullOrEmpty(deviceID)) return deviceID;

            deviceID = getBluetoothAddress();
            if (!StringUtils.isNullOrEmpty(deviceID)) return deviceID;

            deviceID = getIMEI(context);
            if (!StringUtils.isNullOrEmpty(deviceID)) return deviceID;

            deviceID = String.valueOf(new AtomicInteger(0).incrementAndGet());
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
            deviceID = UUID.randomUUID().toString();
        } finally {
            if (!StringUtils.isNullOrEmpty(deviceID)) {
                SharedPrefUtils.save(context, Constants.Key.DEVICE_ID, deviceID);
            }
        }

        return deviceID;
    }

    public static String getCPUTemperature() {

        String[] tempPath = {
                "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
                "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/class/i2c-adapter/i2c-4/4-004c/temperature",
                "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/temperature",
                "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
                "/sys/devices/platform/tegra_tmon/temp1_input",
                "/sys/kernel/debug/tegra_thermal/temp_tj",
                "/sys/devices/platform/s5p-tmu/temperature",
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp",
                "/sys/class/hwmon/hwmon0/device/temp1_input",
                "/sys/devices/virtual/thermal/thermal_zone1/temp",
                "/sys/devices/platform/s5p-tmu/curr_temp"
        };

        String line = null;

        for(String path : tempPath) {
            RandomAccessFile reader = null;
            try {
                reader = new RandomAccessFile(path, "r");
                line = reader.readLine();
                if (line != null) break;
            } catch (Exception e) {
            } finally {
                try {
                    reader.close();
                } catch (Exception e) {}
            }
        }

        return line;
    }

    @SuppressLint("NewApi")
    private static String readFile(String file, char endChar) {
        // Permit disk reads here, as /proc/meminfo isn't really "on
        // disk" and should be fast.  TODO: make BlockGuard ignore
        // /proc/ and /sys/ files perhaps?
        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        FileInputStream is = null;
        byte[] mBuffer = new byte[4096];
        try {
            is = new FileInputStream(file);
            int len = is.read(mBuffer);
            is.close();

            if (len > 0) {
                int i;
                for (i = 0; i < len; i++) {
                    if (mBuffer[i] == endChar) {
                        break;
                    }
                }
                return new String(mBuffer, 0, i);
            }
        } catch (java.io.FileNotFoundException e) {
        } catch (java.io.IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (java.io.IOException e) {
                }
            }
            StrictMode.setThreadPolicy(savedPolicy);
        }
        return null;
    }

    public static String getCountryCode(Context context) {
        return NetworkUtils.getCountryCode(context);
    }
}
