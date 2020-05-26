package com.cleaning.boost.ibooster.task;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Process;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import com.cleaning.boost.ibooster.model.AppsListItem;
import com.gmc.libs.PackageUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskScanCache extends AsyncTask<Void, Object, List<AppsListItem>> {

    private final Context context;
    private final OnActionListener mOnActionListener;
    private Method mGetPackageSizeInfoMethod;
    private int mAppCount = 0;
    private long mCacheSize = 0;
    private int totalApp = 0;
    private boolean isCreateAppItem = false;
    public static boolean mIsScanning = false;
    private final List<AppsListItem> apps = new ArrayList<>();

    public interface OnActionListener {
        void onScanStarted(Context context);

        void onScanProgressUpdated(Context context, AppsListItem appItem, int current, int max, long cacheSize);

        void onScanCompleted(Context context, List<AppsListItem> apps);

    }

    public TaskScanCache(Context context, OnActionListener onActionListener) {
        this.context = context;
        this.mOnActionListener = onActionListener;
        try {
            mGetPackageSizeInfoMethod = context.getPackageManager().getClass().getMethod(
                    "getPackageSizeInfo", String.class, IPackageStatsObserver.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void setCreateAppItem(Boolean isCreateAppItem) {
        this.isCreateAppItem = isCreateAppItem;
    }

    @Override
    protected void onPreExecute() {
        if (mOnActionListener != null) {
            mOnActionListener.onScanStarted(context);
        }
    }

    @Override
    protected List<AppsListItem> doInBackground(Void... params) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !PackageUtils.hasUsageStatsPermission(context)) {
            //Timber.e("Not Grant Usage Access Permission");
            return null;
        }

        mCacheSize = 0;
        PackageManager pm = context.getPackageManager();

        List<PackageInfo> packages = new ArrayList<>();
        final List<PackageInfo> pkgs = pm.getInstalledPackages(0);

        for (PackageInfo ai : pkgs) {
            if (context.getPackageName().equals(ai.packageName) // skip own app
                    || PackageUtils.isSystemPackage(ai)) { // system app
                continue;
            }
            packages.add(ai);
        }

        totalApp = packages.size();

        // publishProgress(null, 0, totalApp);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
            final StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            if (storageManager == null || storageStatsManager == null) {
                // publishProgress(null, 0, totalApp);
                return apps;
            }
            final List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();
            final UserHandle user = Process.myUserHandle();
            try {

                for (PackageInfo ai : packages) {
                    if (isCancelled()) {
                        return apps;
                    }

                    long cacheSize = 0;
                    for (StorageVolume storageVolume : storageVolumes) {
                        final String uuidStr = storageVolume.getUuid();
                        final UUID uuid = uuidStr == null ? StorageManager.UUID_DEFAULT : UUID.fromString(uuidStr);
                        try {
                            final StorageStats storageStats = storageStatsManager.queryStatsForPackage(uuid, ai.packageName, user);
                            cacheSize += storageStats.getCacheBytes();
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                    synchronized (apps) {
                        mCacheSize += cacheSize;
                        AppsListItem appItem = null;
                        if (isCreateAppItem) {
                            appItem = new AppsListItem(ai.packageName, pm.getApplicationLabel(ai.applicationInfo).toString(),
                                    pm.getApplicationIcon(ai.packageName), cacheSize);
                            apps.add(appItem);
                        }
                        publishProgress(appItem, ++mAppCount, totalApp);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                for (PackageInfo ai : packages) {

                    if (isCancelled()) {
                        return apps;
                    }

                    mGetPackageSizeInfoMethod.invoke(context.getPackageManager(), ai.packageName,
                            new IPackageStatsObserver.Stub() {

                                @Override
                                public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) {

                                    synchronized (apps) {
                                        mCacheSize += addPackage(pStats, succeeded);

                                    }

                                }
                            }
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return apps;
    }

    @Override
    protected void onProgressUpdate(Object... values) {

        if (mOnActionListener != null) {
            mOnActionListener.onScanProgressUpdated(context,
                    (AppsListItem)values[0], (Integer)values[1], (Integer)values[2], mCacheSize);
        }

    }

    @Override
    protected void onPostExecute(List<AppsListItem> result) {
        if (mOnActionListener != null) {
            mOnActionListener.onScanCompleted(context, result);
        }

    }

    @Override
    protected void onCancelled(List<AppsListItem> appsListItems) {
        //Timber.e("Task scan cache cancelled");
        super.onCancelled(appsListItems);
    }

    private long addPackage(PackageStats pStats, boolean succeeded) {
        if (!succeeded ) {
            return 0;
        }
        long cacheSize = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            cacheSize += pStats.cacheSize;
        } else {
            cacheSize += pStats.externalCacheSize;
        }

        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo info = packageManager.getApplicationInfo(pStats.packageName,
                    PackageManager.GET_META_DATA);

            synchronized (apps) {
                AppsListItem appItem = null;
                if (isCreateAppItem) {
                    appItem = new AppsListItem(pStats.packageName,
                            packageManager.getApplicationLabel(info).toString(),
                            packageManager.getApplicationIcon(pStats.packageName),
                            cacheSize);

                    apps.add(appItem);

                }
                publishProgress(appItem, ++mAppCount, totalApp);

            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return cacheSize;
    }

}
