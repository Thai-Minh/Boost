package com.cleaning.boost.ibooster.task;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.cleaning.boost.ibooster.model.AppsListItem;
import com.gmc.libs.PackageUtils;

import java.util.ArrayList;
import java.util.List;

public class TaskScanApps extends AsyncTask<Void, Object, Void> {

    private final Context context;
    private final OnActionListener mOnActionListener;

    public interface OnActionListener {
        void onScanStarted();
        void onScanProgressUpdated(AppsListItem appItem, int current, int max);
        void onScanCompleted();
    }

    public TaskScanApps(Context context, OnActionListener onActionListener) {
        this.context = context;
        this.mOnActionListener = onActionListener;
    }

    @Override
    protected void onPreExecute() {
        if (mOnActionListener != null) {
            mOnActionListener.onScanStarted();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {

        PackageManager pm = context.getPackageManager();

        List<ApplicationInfo> packages = new ArrayList<>();
        // final List<PackageInfo> pkgs = pm.getInstalledPackages(0);
        final List<ApplicationInfo> pkgs = pm.getInstalledApplications(0);

        for (ApplicationInfo ai : pkgs) {
            if (context.getPackageName().equals(ai.packageName) // skip own app
                    || PackageUtils.isSystemPackage(ai)) { // system app
                continue;
            }
            packages.add(ai);
        }

        int totalApp = packages.size();

        try {

            int current = 0;
            for (ApplicationInfo ai : packages) {
                if (isCancelled()) {
                    return null;
                }

                AppsListItem appItem = new AppsListItem(ai.packageName, pm.getApplicationLabel(ai).toString(),
                        pm.getApplicationIcon(ai.packageName), 0);

                current++;
                publishProgress(appItem, current, totalApp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {

        if (mOnActionListener != null) {
            mOnActionListener.onScanProgressUpdated((AppsListItem)values[0], (Integer)values[1], (Integer)values[2]);
        }

    }

    @Override
    protected void onPostExecute(Void result) {
        if (mOnActionListener != null) {
            mOnActionListener.onScanCompleted();
        }

    }

    @Override
    protected void onCancelled(Void aVoid) {
        super.onCancelled(aVoid);
    }

}
