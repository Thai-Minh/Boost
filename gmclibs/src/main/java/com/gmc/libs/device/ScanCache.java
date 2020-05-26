package com.gmc.libs.device;

import android.annotation.TargetApi;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Process;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import com.gmc.libs.PackageUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScanCache extends AsyncTask<Void, Object, List<CacheItem>> {

    private PackageManager packageManager;
    private int mAppCount = 0;
    private ArrayList<AppDetail> appList;
    private List<CacheItem> apps;

    private OnTaskScanListener taskScanListener;
    private Context context;

    public ScanCache(Context context) {
        this.context = context;
        this.apps = new ArrayList<>();
        packageManager = context.getPackageManager();
    }

    public void setTaskScanListener(OnTaskScanListener taskScanListener) {
        this.taskScanListener = taskScanListener;
    }

    @Override
    protected void onPreExecute() {
        if (taskScanListener != null) {
            taskScanListener.onScanStarted();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected List<CacheItem> doInBackground(final Void... params) {
        final StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
        final StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        if (storageManager == null || storageStatsManager == null) {
            postPublishProgress();
            return apps;
        }
        final List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();
        final UserHandle user = Process.myUserHandle();

        try {
            appList = PackageUtils.getInstalledApps(context,false);
            apps.clear();
            for (AppDetail app : appList) {
                long cacheSize = 0;
                for (StorageVolume storageVolume : storageVolumes) {
                    final String uuidStr = storageVolume.getUuid();
                    final UUID uuid = uuidStr == null ? StorageManager.UUID_DEFAULT : UUID.fromString(uuidStr);
                    try {
                        final StorageStats storageStats = storageStatsManager.queryStatsForPackage(uuid, app.packageName, user);
                        cacheSize += storageStats.getCacheBytes();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                addPackage(app.packageName, cacheSize);
                mAppCount++;
                postPublishProgress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apps;
    }

    private void postPublishProgress() {
        if (mAppCount == appList.size()) {
            publishProgress(mAppCount, mAppCount, null);
        }
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        if (taskScanListener != null) {
            if (values.length == 3) {
                taskScanListener.onScanProgressUpdated((int) values[0], (int) values[1], (CacheItem) values[2]);
            }
        }
    }

    @Override
    protected void onPostExecute(List<CacheItem> result) {
        if (taskScanListener != null) {
            taskScanListener.onScanCompleted(result);
        }
    }

    private long addPackage(String packageName, long cacheSize) {
        try {
            if (exists(packageName)) return 0;
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

            CacheItem cacheItem = new CacheItem(packageName,
                    packageManager.getApplicationLabel(info).toString(),
                    packageManager.getApplicationIcon(packageName),
                    cacheSize);
            apps.add(cacheItem);

            publishProgress(mAppCount, appList.size(), cacheItem);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return cacheSize;
    }

    private boolean exists(String packageName) {
        try {
            if (apps == null || apps.isEmpty()) return false;
            for (CacheItem item : apps) {
                if (packageName.equals(item.getPackageName())) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
