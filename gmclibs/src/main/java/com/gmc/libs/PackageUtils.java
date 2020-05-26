package com.gmc.libs;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.os.StatFs;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gmc.libs.device.AppDetail;
import com.gmc.libs.model.RunningAppsServices;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PackageUtils {

    public static final int SYSTEM = 1;
    public static final int INTERNAL = 2;
    public static final int EXTERNAL = 3;
    public static final int APP_SYSTEM = 1;
    public static final int APP_NONE_SYSTEM = 2;
    public static final int APP_ALL = 3;

    public static void freeStorage(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            // Get all methods on the PackageManager
            Method[] methods = pm.getClass().getDeclaredMethods();
            for (Method m : methods) {
                if (m.getName().equals("freeStorage")) {
                    // Found the method I want to use
                    try {
                        long desiredFreeStorage = Long.MAX_VALUE;
                        m.invoke(pm, desiredFreeStorage, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isSystemPackage(ResolveInfo ri) {
        return isSystemPackage(ri.activityInfo.applicationInfo);
    }
    public static boolean isSystemPackage(ApplicationInfo ai) {
        return (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
    public static boolean isSystemPackage(PackageInfo pi) {
        return isSystemPackage(pi.applicationInfo);
    }
    public static boolean isSystemPackage(Context context, String packageName) {
        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(packageName, 0);
            return isSystemPackage(app);
        } catch (Exception e) {
            return false;
        }
    }

    private static AppDetail getAppDetail(PackageManager pm, PackageInfo p) {
        AppDetail newInfo = new AppDetail();
        newInfo.appName = p.applicationInfo.loadLabel(pm).toString();
        newInfo.packageName = p.packageName;
        newInfo.versionName = p.versionName;
        newInfo.versionCode = p.versionCode;
        newInfo.icon = p.applicationInfo.loadIcon(pm);
        newInfo.dataDir = p.applicationInfo.dataDir;
        newInfo.sourceDir = p.applicationInfo.sourceDir;
        newInfo.firstInstallTime = p.firstInstallTime;
        return newInfo;
    }

    public static ArrayList<AppDetail> getInstalledApps(Context context, int appType) {
        try {
            ArrayList<AppDetail> res = new ArrayList<AppDetail>();
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packs = pm.getInstalledPackages(0);
            for (int i = 0; i < packs.size(); i++) {
                PackageInfo p = packs.get(i);
                AppDetail newInfo = null;

                if (context.getPackageName().equals(p.packageName)) { // skip own app
                    continue;
                }

                if (appType == APP_SYSTEM && isSystemPackage(p)) {
                    newInfo = getAppDetail(pm, p);
                } else if (appType == APP_NONE_SYSTEM && !isSystemPackage(p)) {
                    newInfo = getAppDetail(pm, p);
                } else if (appType == APP_ALL) {
                    newInfo = getAppDetail(pm, p);
                }

                if (newInfo != null) res.add(newInfo);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<AppDetail> getInstalledApps(Context context, boolean getSysPackages) {
        try {
            ArrayList<AppDetail> res = new ArrayList<AppDetail>();
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packs = pm.getInstalledPackages(0);
            for (int i = 0; i < packs.size(); i++) {
                PackageInfo p = packs.get(i);

                if (context.getPackageName().equals(p.packageName)  // skip own app
                        || (!getSysPackages && isSystemPackage(p))) {
                    continue;
                }
                AppDetail newInfo = getAppDetail(pm, p);
                res.add(newInfo);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Context createContextOfPackageName(Context context, String packageName) {
        try {
            return context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File[] getAllCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            return cacheDir.listFiles();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean deleteDir(File dir) {
        try {
            if (dir != null && dir.isDirectory()) {
                String[] children = dir.list();
                for (String aChildren : children) {
                    boolean success = deleteDir(new File(dir, aChildren));
                    if (!success) {
                        return false;
                    }
                }
                return dir.delete();
            } else if (dir != null && dir.isFile()) {
                return dir.delete();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * whatStorage = SYSTEM(1) | INTERNAL(2) | EXTERNAL(3) = default
     * size = {totalBytes, availableBytes, freeBytes}
     */
    public static long[] getStorageSize(int whatStorage) {
        long[] size = {0, 0, 0};
        StatFs statFS;
        if (whatStorage == EXTERNAL) {
            statFS = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        } else if (whatStorage == INTERNAL) {
            statFS = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        } else if (whatStorage == SYSTEM) {
            statFS = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        } else {
            statFS = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        }

        long availableBytes, totalBytes, freeBytes;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            long blockSize = statFS.getBlockSizeLong();
            totalBytes = statFS.getBlockCountLong() * blockSize;
            availableBytes = statFS.getAvailableBlocksLong() * blockSize;
            freeBytes = statFS.getFreeBlocksLong() * blockSize;
        } else {
            long blockSize = statFS.getBlockSize();
            totalBytes = statFS.getBlockCount() * blockSize;
            availableBytes = statFS.getAvailableBlocks() * blockSize;
            freeBytes = statFS.getFreeBlocks() * blockSize;
        }

        size[0] = totalBytes;
        size[1] = availableBytes;
        size[2] = freeBytes;
        return size;
    }

    /**
     * ram = {totalSize, freeSize}
     */
    public static long[] getRAMSize(Context context) {

        long[] ram = {0, 0};

        try {
            ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);
            ram[0] = memInfo.totalMem;
            ram[1] = memInfo.availMem;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ram;

    }

    /*
     * if android sdk api < 21: actvityManager.getRunningAppProcesses()
     * else (api >= 21) use UsageStatsManager
     *       Must check grant permission
     * */
    public static List<RunningAppsServices> getRunningApps(Context context) {

        List<RunningAppsServices> runningList = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager actvityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppInfos = actvityManager.getRunningAppProcesses();
            if (runningAppInfos != null && !runningAppInfos.isEmpty()) {
                runningList = new ArrayList<>();
                for (ActivityManager.RunningAppProcessInfo ai : runningAppInfos) {
                    if (ai.pkgList[0].equals(BuildConfig.APPLICATION_ID)) continue;

                    RunningAppsServices ras = new RunningAppsServices();
                    ras.pid = ai.pid;
                    ras.uid = ai.uid;
                    ras.packageName = ai.pkgList[0];
                    ras.processName = ai.processName;
                    ras.describeContents = ai.describeContents();
                    ras.pid = ai.pid;
                    runningList.add(ras);
                }
            }
        } else {
            List<UsageStats> usageStatsList = getUsageStatsList(context);
            if (usageStatsList != null && !usageStatsList.isEmpty()) {
                runningList = new ArrayList<>();
                for (UsageStats us : usageStatsList) {
                    if (us.getPackageName().equals(BuildConfig.APPLICATION_ID)) continue;
                    new ActivityManager.RunningAppProcessInfo();
                    RunningAppsServices ras = new RunningAppsServices();
                    ras.packageName = us.getPackageName();
                    ras.processName = us.getPackageName();
                    ras.describeContents = us.describeContents();
                    runningList.add(ras);
                }
            }
        }

        return runningList;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static List<UsageStats> getUsageStatsList(Context context) {
        try {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            Calendar calendar = Calendar.getInstance();
            long endTime = calendar.getTimeInMillis();
            calendar.add(Calendar.YEAR, -1);
            long startTime = calendar.getTimeInMillis();
            List<UsageStats> usageStatsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
            return usageStatsList;
        } catch (Exception e) {
            return null;
        }
    }

    public static void freeMemory() {
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    public static void killProcess(int pid) {
        try {
            Process.sendSignal(pid, Process.SIGNAL_KILL);
        } catch (Exception e) {
        }
    }

    @RequiresPermission("android.permission.KILL_BACKGROUND_PROCESSES")
    public static void killProcessByPackageName(Context context, String pkgName) {
        try {

            if (pkgName == null || pkgName.isEmpty()) {
                return;
            }
            ActivityManager actvityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            int pid = findPIDbyPackageName(context, pkgName);
            Process.killProcess(pid);
            Process.sendSignal(pid, Process.SIGNAL_KILL);
            actvityManager.killBackgroundProcesses(pkgName);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static int findPIDbyPackageName(Context context, String packageName) {
        int result = -1;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.RunningAppProcessInfo> pis = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo pi : pis) {
                if (pi.processName.equalsIgnoreCase(packageName)) {
                    result = pi.pid;
                }
                if (result != -1) break;
            }
        } else {
            result = -1;
        }

        return result;
    }


    public static boolean hasUsageStatsPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        boolean granted = false;
        try {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            if (appOps == null) {
                return false;
            }
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());

            if (mode == AppOpsManager.MODE_DEFAULT) {
                granted = (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
            } else {
                granted = (mode == AppOpsManager.MODE_ALLOWED);
            }
        } catch (Exception e) {

        }
        return granted;
    }

    public static void showUsageAccessSettings(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            if (context instanceof Activity) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            }
            context.startActivity(intent);
        }
    }

    public static void showAccessibilitySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setClassName("com.android.settings",
                "com.android.settings.Settings");
        intent.setAction(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        //intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,"the fragment which you want show");
        //intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, extras);

        context.startActivity(intent);
    }

    // To check if service is enabled
    public static boolean isAccessibilitySettingsOn(Context mContext, String serviceClassCanonicalName) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + serviceClassCanonicalName;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            LogUtils.e( "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            LogUtils.e( "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            LogUtils.e( "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    LogUtils.e( "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        LogUtils.e( "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            LogUtils.e( "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }

    public static Context getPackageContext(Context context, String packageName) {
        Context pkgContext = null;
        try {
            if (context.getPackageName().equals(packageName)) {
                pkgContext = context;
            } else {
                try {
                    pkgContext = context.createPackageContext(packageName,
                            Context.CONTEXT_IGNORE_SECURITY
                                    | Context.CONTEXT_INCLUDE_CODE);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pkgContext;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean isActivityRunning(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        boolean isAppRunning = false;
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(packageName) || info.baseActivity.getPackageName().equals(packageName)) {
                isAppRunning = true;
                break;
            }
        }
        return isAppRunning;
    }

    /* Before Android Q */
    public static boolean appIsRunning(Context ctx, String packageName) {
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        if (runningAppProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                if (runningAppProcessInfo.processName.startsWith(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Intent getAppOpenIntentByPackageName(Context context, String packageName) {
        String mainAct = null;
        PackageManager pkgMag = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        List<ResolveInfo> list = pkgMag.queryIntentActivities(intent, PackageManager.GET_META_DATA);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainAct = info.activityInfo.name;
                break;
            }
        }
        if (TextUtils.isEmpty(mainAct)) {
            return null;
        }
        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;
    }

    public static boolean openPackage(Context context, String packageName) {
        Context pkgContext = getPackageContext(context, packageName);
        Intent intent = getAppOpenIntentByPackageName(context, packageName);
        if (pkgContext != null && intent != null) {
            pkgContext.startActivity(intent);
            return true;
        }
        return false;
    }

    public static String getAppTitle(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo app = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(app).toString();
        } catch (Exception e) {}
        return null;
    }

    public static Drawable getAppIcon(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationIcon(packageName);
        } catch (Exception e) {}
        return null;
    }

    /*
    public static List<PackageInfo> getInstalledPackages(Context context) {
        List<PackageInfo> apps = new ArrayList<>();
        PackageManager pManager = context.getPackageManager();
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++) {
            PackageInfo pak = paklist.get(i);
            if ((pak.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
            // customs applications
            apps.add(pak);
        }
        return apps;
    }

    public static List<ApplicationInfo> getInstalledApps(Context context) {
        List<ApplicationInfo> apps = new ArrayList<>();
        PackageManager pManager = context.getPackageManager();
        List<ApplicationInfo> paklist = pManager.getInstalledApplications(0);
        for (ApplicationInfo pak : paklist) {
            if ((pak.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
            // customs applications
            apps.add(pak);
        }
        return apps;
    }
    */

    public static boolean hasStoragePermission(Context context) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return true;
    }

    public static void requestStoragePermission(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
    }
}
