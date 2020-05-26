package com.gmc.libs.device;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.gmc.libs.PackageUtils;
import com.gmc.libs.model.RunningAppsServices;

import java.util.ArrayList;
import java.util.List;

public class ScanRunningApps extends AsyncTask<Void, Object, List<RunningAppsServices>> {

    private Context context;
    private PackageManager packageManager;
    private int mAppCount = 0;
    private List<RunningAppsServices> runningApps;
    private OnTaskScanRunningAppsListener taskScanListener;
    private long size;

    public ScanRunningApps(Context context) {
        this.runningApps = new ArrayList<>();
        packageManager = context.getPackageManager();
        this.context = context;
    }

    public void setTaskScanListener(OnTaskScanRunningAppsListener taskScanListener) {
        this.taskScanListener = taskScanListener;
    }

    public long getSize() {
        return size;
    }

    @Override
    protected void onPreExecute() {
        if (taskScanListener != null) {
            taskScanListener.onScanStarted();
        }
    }

    @Override
    protected List<RunningAppsServices> doInBackground(final Void... params) {
        try {
            List<RunningAppsServices> runningAppList = PackageUtils.getRunningApps(context);
            runningApps.clear();
            size = 0;
            for (RunningAppsServices appsServices : runningAppList) {
                addPackage(appsServices);
                postPublishProgress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return runningApps;
    }

    private void postPublishProgress() {
        if (mAppCount == runningApps.size()) {
            publishProgress(mAppCount, mAppCount, null);
        }
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        if (taskScanListener != null) {
            if (values.length == 3) {
                taskScanListener.onScanProgressUpdated((int) values[0], (int) values[1], (RunningAppsServices) values[2]);
            }
        }
    }

    @Override
    protected void onPostExecute(List<RunningAppsServices> result) {
        if (taskScanListener != null) {
            taskScanListener.onScanCompleted(result);
        }
    }

    private void addPackage(RunningAppsServices appsServices) {
        try {
            if (exists(appsServices)) return;
            ApplicationInfo info = packageManager.getApplicationInfo(appsServices.packageName, PackageManager.GET_META_DATA);
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) return;    // system service
            appsServices.appName = (String) packageManager.getApplicationLabel(info);
            appsServices.appIcon = packageManager.getApplicationIcon(appsServices.packageName);
            size += appsServices.describeContents;
            runningApps.add(appsServices);

            publishProgress(mAppCount, runningApps.size(), appsServices);

        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    private boolean exists(RunningAppsServices appsServices) {
        try {
            if (runningApps == null || runningApps.isEmpty()) return false;
            for (RunningAppsServices item : runningApps) {
                if (appsServices.packageName.equals(item.packageName)) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
