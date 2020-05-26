package com.cleaning.boost.ibooster.task;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.inf.ActionListener;
import com.cleaning.boost.ibooster.service.CheckJunkCacheReceiver;
import com.cleaning.boost.ibooster.service.CheckRamReceiver;
import com.cleaning.boost.ibooster.utils.Constants;
import com.cleaning.boost.ibooster.utils.Utils;
import com.gmc.libs.Formatter;
import com.gmc.libs.LogUtils;
import com.gmc.libs.PackageUtils;
import com.gmc.libs.ResourceUtils;
import com.gmc.libs.SharedPrefUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class OptimizeUtils {

    public interface OptimizeListener {
        void onStart();

        void onCompleted(AlertDialog dialog, Object... object);
    }

    private final OptimizeListener mListener;

    public OptimizeUtils(OptimizeListener listener) {
        mListener = listener;
    }

    private static void fullView(Activity activity, View view) {
        Rect displayRectangle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        view.setMinimumWidth((int) (displayRectangle.width() * 1f));
        view.setMinimumHeight((int) (displayRectangle.height() * 1f));
    }

    public void cleanNow(Activity activity) {
        try {

            GifDrawable gifFromAssets = new GifDrawable(activity.getAssets(), "anim_junk_clean.gif");
            View cleaningView = activity.getLayoutInflater().inflate(R.layout.layout_cleaning, null, false);
            LinearLayout linearLayoutClean = cleaningView.findViewById(R.id.linearLayoutClean);
            linearLayoutClean.setBackground(gifFromAssets);

            fullView(activity, cleaningView);

            final AlertDialog dialog = Utils.createPopup(activity, cleaningView, null,
                    null, null, null, 0, false, true);

            if (dialog != null) {
                Window window = dialog.getWindow();
                if (window != null)
                    window.setBackgroundDrawable(new ColorDrawable(ResourceUtils.getColor(activity, R.color.bg_gif_junk)));
                dialog.show();
            }

            TextView textViewScanning = cleaningView.findViewById(R.id.textViewCleaningMsg);
            TaskCleanCache.OnActionListener listener = new TaskCleanCache.OnActionListener() {
                @Override
                public void onCleanStarted(Context context) {
                    textViewScanning.setText(R.string.btn_scanning_);
                    mListener.onStart();
                }

                @Override
                public void onCleanProgressUpdated(Context context, String pkg, long cleanedSize, String percent) {
                    try {
                        String msg = context.getResources().getString(R.string.scanning_percent,
                                percent + "%", pkg);
                        textViewScanning.setText(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCleanCompleted(Context context, long cleaned) {
                    String cleanedSize = cleaned > 0 ? (Formatter.formatFileSizeSI(context, cleaned) + " ") : "";
                    cleanedSize += context.getResources().getString(R.string.clean_completed);
                    textViewScanning.setText(cleanedSize);
                    new Handler().postDelayed(() -> mListener.onCompleted(dialog, cleaned), 1000);
                }
            };
            new TaskCleanCache(activity, listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void boostNow(Activity activity) {
        try {

            GifDrawable gifFromAssets = new GifDrawable(activity.getAssets(), "anim_phone_boost.gif");
            View boostingView = activity.getLayoutInflater().inflate(R.layout.layout_boost_popup, null, false);
            LinearLayout linearLayoutClean = boostingView.findViewById(R.id.linearLayoutBoost);
            linearLayoutClean.setBackground(gifFromAssets);

            fullView(activity, boostingView);

            final AlertDialog dialog = Utils.createPopup(activity, boostingView, null,
                    null, null, null, 0, false, true);
            if (dialog != null) {
                Window window = dialog.getWindow();
                if (window != null)
                    window.setBackgroundDrawable(new ColorDrawable(ResourceUtils.getColor(activity, R.color.bg_gif_boost)));
                dialog.show();
            }

            final TextView textViewFreeingUp = boostingView.findViewById(R.id.textViewFreeingUp);

            ActionListener onActionListener = new ActionListener() {
                @Override
                public void onStarted(Object[] objects) {
                    mListener.onStart();
                    textViewFreeingUp.setText(R.string.freeing_up);
                }

                @Override
                public void onCompleted(Context context, Object[] objects) {
                    SharedPrefUtils.save(context, Constants.SHARED_PREF_PHONE_BOOST_TIME, System.currentTimeMillis());
                    textViewFreeingUp.setText(R.string.boost_completed);
                    new Handler().postDelayed(() -> mListener.onCompleted(dialog, objects), 1000);
                }
            };

            new TaskBoost(activity, onActionListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void coolDown(Activity activity) {
        try {

            GifDrawable gifFromAssets = new GifDrawable(activity.getAssets(), "anim_cpu_cooler.gif");
            View coolingView = activity.getLayoutInflater().inflate(R.layout.layout_cooling_popup, null, false);
            LinearLayout linearLayoutClean = coolingView.findViewById(R.id.linearLayoutCooling);
            linearLayoutClean.setBackground(gifFromAssets);

            fullView(activity, coolingView);

            final AlertDialog dialog = Utils.createPopup(activity, coolingView, null,
                    null, null, null, 0, false, true);

            if (dialog != null) {
                Window window = dialog.getWindow();
                if (window != null)
                    window.setBackgroundDrawable(new ColorDrawable(ResourceUtils.getColor(activity, R.color.bg_gif_cooldown)));
                dialog.show();
            }


            ActionListener onActionListener = new ActionListener() {
                @Override
                public void onStarted(Object[] objects) {
                    mListener.onStart();
                }

                @Override
                public void onCompleted(Context context, Object[] objects) {
                    SharedPrefUtils.save(context, Constants.SHARED_PREF_PHONE_COOL_DOWN_TIME, System.currentTimeMillis());
                    new Handler().postDelayed(() -> mListener.onCompleted(dialog, objects), 1000);
                }
            };

            new TaskBoost(activity, onActionListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopAllBackgroundProcess(Context context) {
        PackageManager pm = context.getPackageManager();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        try {

            final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (int i = 0; i < packages.size(); i++) {
                ApplicationInfo p = packages.get(i);

                if (context.getPackageName().equals(p.packageName)  // skip own app
                        || PackageUtils.isSystemPackage(p)) { // system app
                    continue;
                }

                // force stop all background process
                stopPackage(p.packageName);

                // kill all background process
                if (am != null) am.killBackgroundProcesses(p.packageName);
            }

        } catch (Exception e) {
            LogUtils.e(e.toString());
        }

        // remove all recent tasks
        clearRecentTasks(context, am);
    }

    private static void stopPackage(String pkgName) {
        BufferedReader bufferedReader = null;
        try {
            Process process = Runtime.getRuntime().exec("am kill all " + pkgName);
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
            } catch (Exception e) {
                LogUtils.e(e.toString());
            }
        }
        try {
            Process process = Runtime.getRuntime().exec("am force-stop " + pkgName);
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (Exception e) {
            LogUtils.e(e.toString());
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
            } catch (Exception e) {
                LogUtils.e(e.toString());
            }
        }
    }

    private static void clearRecentTasks(Context context, ActivityManager am) {
        if (am == null) {
            am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        try {
            Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
            Method mRemoveTask = activityManagerClass
                    .getMethod("removeTask", int.class, int.class);
            mRemoveTask.setAccessible(true);

            assert am != null;
            List<ActivityManager.RecentTaskInfo> recents =
                    am.getRecentTasks(1000, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
            // Start from 1, since we don't want to kill ourselves!
            for (int i = 1; i < recents.size(); i++) {
                try {
                    mRemoveTask.invoke(am, recents.get(i).persistentId, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }
    }

    public static void initOptimize(Context context) {
        try {
            LogUtils.e("OptimizeUtils initOptimize");
            AlarmManager processTimer = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            // CHECK JUNK CACHE FILES **************************************************************
            Intent intent = new Intent(context, CheckJunkCacheReceiver.class);
            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            try {
                if (processTimer != null) processTimer.cancel(pendingIntent);
            } catch (Exception e) {
                LogUtils.e(e.toString());
            }

            // Repeat alarm every repeatTime milliseconds
            processTimer.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(), Constants.TIME_REPEAT_SCAN_CACHE, pendingIntent);

            // CHECK RAM LEFT **********************************************************************
            Intent intentRam = new Intent(context, CheckRamReceiver.class);
            PendingIntent pendingIntentRam =
                    PendingIntent.getBroadcast(context, 0, intentRam, PendingIntent.FLAG_UPDATE_CURRENT);

            try {
                processTimer.cancel(pendingIntentRam);
            } catch (Exception e) {
                LogUtils.e(e.toString());
            }

            // Repeat alarm every repeatTime milliseconds
            processTimer.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(), Constants.TIME_REPEAT_CHECK_RAM, pendingIntentRam);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
