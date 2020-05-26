package com.cleaning.boost.ibooster.task;

import android.Manifest;
import android.content.Context;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;

import androidx.core.content.ContextCompat;

import com.cleaning.boost.ibooster.utils.Constants;
import com.gmc.libs.LogUtils;
import com.gmc.libs.PackageUtils;
import com.gmc.libs.SharedPrefUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TaskCleanCache extends AsyncTask<Void, String, Boolean> {

    //private String TAG = "TaskCleanCache";
    private final Context context;
    private Method mFreeStorageAndNotifyMethod;
    private final OnActionListener mOnActionListener;
    public static boolean mIsCleaning;
    private static final boolean cancelTask = false;
    private long totalCleaned = 0;
    private long availableStorageBefore;
    private long availableStorageAfter;
    //private long freeStorageBefore;
    //private long freeStorageAfter;

    public interface OnActionListener {

        void onCleanStarted(Context context);

        void onCleanProgressUpdated(Context context, String packageCleaned,
                /*each file size cleaned*/long cleanedSize, String percent);

        void onCleanCompleted(Context context, /*Total cleaned*/long cleaned);

    }

    public TaskCleanCache(Context context, OnActionListener onActionListener) {
        this.context = context;
        this.mOnActionListener = onActionListener;
        try {
            mFreeStorageAndNotifyMethod = context.getPackageManager().getClass().getMethod(
                    "freeStorageAndNotify", long.class, IPackageDataObserver.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPreExecute() {
        if (mOnActionListener != null) {
            mOnActionListener.onCleanStarted(this.context);
        }
        // mIsCleaning = true;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        if (cancelTask || isCancelled()) {
            return true;
        }

        long[] externalStorage = PackageUtils.getStorageSize(PackageUtils.EXTERNAL);
        long[] internalStorage = PackageUtils.getStorageSize(PackageUtils.INTERNAL);
        availableStorageBefore = externalStorage[1] + internalStorage[1];
        //freeStorageBefore = externalStorage[2] + internalStorage[2];
        List<PackageInfo> packages = new ArrayList<>();
        final List<PackageInfo> pkgs = context.getPackageManager().getInstalledPackages(0);

        for (PackageInfo ai : pkgs) {
            if (context.getPackageName().equals(ai.packageName) // skip own app
                    || PackageUtils.isSystemPackage(ai)) { // system app
                continue;
            }
            packages.add(ai);
        }

        int totalApp = packages.size();
        int progress, idx = 0;

        for (PackageInfo ai : packages) {
            idx++;
            progress = idx * 100 / totalApp;
            publishProgress(ai.packageName, "0", String.valueOf(progress));
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                LogUtils.e(e.toString());
            }
        }

        PackageUtils.freeStorage(context);

        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getAbsolutePath());
            if (canCleanInternalCache()) {

                final long totalBytes, blockSize;

                //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSizeLong();
                totalBytes = stat.getBlockCountLong() * blockSize;
                //} else {
                //    blockSize = stat.getBlockSize();
                //    totalBytes = stat.getBlockCount() * blockSize;
                //}

                mFreeStorageAndNotifyMethod.invoke(context.getPackageManager(), totalBytes,
                        new IPackageDataObserver.Stub() {
                            @Override
                            public void onRemoveCompleted(String packageName, boolean succeeded) {
                                if (succeeded) totalCleaned += totalBytes;
                            }
                        }
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (isExternalStorageWritable()) {
                final File externalDataDirectory = new File(Environment
                        .getExternalStorageDirectory().getAbsolutePath() + "/Android/data");

                final String externalCachePath = externalDataDirectory.getAbsolutePath() + "/%s/cache";

                if (externalDataDirectory.isDirectory()) {
                    final File[] files = externalDataDirectory.listFiles();
                    if (files != null) {
                        for (File file : files) {

                            if (cancelTask || isCancelled()) {
                                return true;
                            }

                            long sizeDeleted = deleteDirectory(new File(String.format(externalCachePath,
                                    file.getName())), false);
                            totalCleaned += sizeDeleted;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            new CountDownLatch(0).await(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }

        long[] externalStorage2 = PackageUtils.getStorageSize(PackageUtils.EXTERNAL);
        long[] internalStorage2 = PackageUtils.getStorageSize(PackageUtils.INTERNAL);
        availableStorageAfter = externalStorage2[1] + internalStorage2[1];
        //freeStorageAfter = externalStorage2[2] + internalStorage2[2];

        return true;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (mOnActionListener != null) {
            mOnActionListener.onCleanProgressUpdated(context, values[0], Long.valueOf(values[1]), values[2]);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mIsCleaning = false;
        SharedPrefUtils.save(context, Constants.SHARED_PREF_CLEANED_TIME, System.currentTimeMillis());
        if (mOnActionListener != null) {
            totalCleaned = availableStorageAfter - availableStorageBefore;
            mOnActionListener.onCleanCompleted(context, totalCleaned);
        }

    }

    @Override
    protected void onCancelled() {
        //Timber.e("Task scan cache cancelled");
        mIsCleaning = false;
        super.onCancelled();
    }

    private long deleteDirectory(File file, boolean directoryOnly) {
        if (!canCleanExternalCache()) {
            return 0;
        }

        if (file == null || !file.exists() || (directoryOnly && !file.isDirectory())) {
            return 0;
        }

        long cleaned = 0;

        try {
            if (file.isDirectory()) {
                final File[] children = file.listFiles();

                if (children != null) {
                    for (File child : children) {
                        if (isCancelled() || cancelTask) return cleaned;
                        cleaned += deleteDirectory(child, false);
                    }
                }
            } else {
                cleaned = file.length();
            }

            if (file.delete()) {
                return cleaned;
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }

        return 0;

    }

    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public boolean canCleanInternalCache() {
        return hasPermission(Manifest.permission.CLEAR_APP_CACHE);
    }

    public boolean canCleanExternalCache() {
        return hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
}
