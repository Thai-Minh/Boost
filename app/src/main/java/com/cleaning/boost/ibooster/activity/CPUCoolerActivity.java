package com.cleaning.boost.ibooster.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.adapter.AppListAdapter;
import com.cleaning.boost.ibooster.model.AppsListItem;
import com.cleaning.boost.ibooster.task.OptimizeUtils;
import com.cleaning.boost.ibooster.task.TaskScanApps;
import com.cleaning.boost.ibooster.utils.Constants;
import com.cleaning.boost.ibooster.utils.Utils;
import com.gmc.libs.CommonUtils;
import com.gmc.libs.LogUtils;
import com.gmc.libs.PackageUtils;
import com.gmc.libs.ResourceUtils;
import com.gmc.libs.SharedPrefUtils;
import com.gmc.libs.ToastUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class CPUCoolerActivity extends BaseActivity {


    //comment CPU Activity
    private final CPUCoolerActivity mActivity = this;

    @BindView(R.id.textViewTemperatureC)
    TextView textViewTemperatureC;
    @BindView(R.id.textViewTemperatureF)
    TextView textViewTemperatureF;
    @BindView(R.id.textViewTemperatureStatus)
    TextView textViewTemperatureStatus;
    @BindView(R.id.textViewTempTitle2)
    TextView textViewTempTitle2;
    @BindView(R.id.recyclerViewRunningApps)
    RecyclerView recyclerViewRunningApps;
    @BindView(R.id.btnCoolDown)
    Button btnCoolDown;
    @BindView(R.id.imageViewCooling)
    ImageView imageViewCooling;
    @BindView(R.id.textViewCoolingMsg)
    TextView textViewCoolingMsg;
    @BindView(R.id.btnDone)
    Button btnDone;
    @BindView(R.id.constraintLayoutOptimize)
    ConstraintLayout constraintLayoutOptimize;

    private boolean hasUsageAccessPermission = false;
    private boolean isScanCompleted = false;
    private boolean isScanning = false;
    private AppListAdapter runningAppListAdapter;
    private TaskScanApps taskScanApps;
    private IntentFilter intentfilter;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindContentView(R.layout.activity_cpu_cooler);

        initView();

        initData();
    }

    private void initView() {
        initToolbar(getResources().getString(R.string.cpu_cooler));
        animation = AnimationUtils.loadAnimation(mActivity, R.anim.gmclibs_rotate);
    }

    private void initData() {
        // round to 2 digits:
        numberFormat.setMaximumFractionDigits(1);
        intentfilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        runningAppListAdapter = new AppListAdapter(mActivity, new ArrayList<>());
        recyclerViewRunningApps.setLayoutManager(new LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false));
        recyclerViewRunningApps.setAdapter(runningAppListAdapter);
    }

    private void bindData() {
        // Neu thoi diem cooldown gan nhat trong vong khoang 20 phut thi show "... optimized!"
        long cleanedTime = SharedPrefUtils.getLong(mActivity, Constants.SHARED_PREF_PHONE_COOL_DOWN_TIME, 0);
        long sub = System.currentTimeMillis() - cleanedTime;
        if (sub < Constants.TIME_SEPARATOR_COOLED) {
            constraintLayoutOptimize.setVisibility(View.VISIBLE);
            // Neu vua moi clean (cach khoang <1') show cooling
            if (sub < Constants.TIME_SEPARATOR_COOLING) {
                textViewCoolingMsg.setText(R.string.msg_your_phone_is_cooling);
            } else {
                textViewCoolingMsg.setText(R.string.msg_phone_optimized);
            }

            return;

        }

        constraintLayoutOptimize.setVisibility(View.GONE);

        getRunningProcess();

    }

    @OnClick(R.id.btnCoolDown)
    void onCoolDownClick() {
        if (CommonUtils.isFastDoubleClick()) return;
        if (hasUsageAccessPermission) {
            OptimizeUtils.OptimizeListener actionListener = new OptimizeUtils.OptimizeListener() {

                @Override
                public void onStart() {
                    //loadIntersAd();
                    ToastUtils.showInfo(mActivity, getString(R.string.cooling_down), Toast.LENGTH_LONG, false);
                }

                @Override
                public void onCompleted(AlertDialog dialog, Object[] object) {
                    new Handler().postDelayed(() -> {
                        if (dialog != null) dialog.dismiss();
                        //showIntersAd();
                        constraintLayoutOptimize.setVisibility(View.VISIBLE);
                        imageViewCooling.startAnimation(animation);
                        //loadRecBannerAd2();
                    }, 1000);
                }
            };
            new OptimizeUtils(actionListener).coolDown(mActivity);

        } else {// GRANT PERMISSION
            // PackageUtils.showUsageAccessSettings(mActivity);
            Utils.showUsageAccessSettingsPopup(mActivity);
        }
    }

    @OnClick(R.id.btnDone)
    void onDoneClick() {
        finish();
    }

    protected void onResume() {
        super.onResume();

        // *** //
        f();
        // *** //

        try {
            int fromNotification = getIntent().getIntExtra(Constants.NOTIFICATION_INTENT, 0);
            if (fromNotification == Constants.NOTIFICATION_CPU_COOLER_ID) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                assert notificationManager != null;
                notificationManager.cancel(Constants.NOTIFICATION_CPU_COOLER_ID);
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }

        registerReceiver(broadcastreceiver, intentfilter);

        bindData();
    }

    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastreceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScanning();
    }

    private void getRunningProcess() {

        if (isScanCompleted || isScanning) return;

        // Kiem tra xem da grant usage access permission chua?
        hasUsageAccessPermission = PackageUtils.hasUsageStatsPermission(mActivity);
        if (hasUsageAccessPermission) {
            // Neu da co quyen usage access
            textViewTempTitle2.setTextColor(ResourceUtils.getColor(mActivity, R.color.black_text));
            recyclerViewRunningApps.setVisibility(View.VISIBLE);
            btnCoolDown.setText(R.string.btn_cool_down);
            btnCoolDown.setEnabled(true);

            //loadSmallBannerAd();

            loadAppList();

        } else {
            // Neu chua co quyen usage access
            textViewTempTitle2.setText(R.string.msg_grant_usage_access_permission);
            textViewTempTitle2.setTextColor(ResourceUtils.getColor(mActivity, R.color.gmc_color_red_A700));
            recyclerViewRunningApps.setVisibility(View.GONE);
            btnCoolDown.setText(R.string.btn_grant);

            // show rec ad 1
            //loadRecBannerAd();

        }
    }

    private void loadAppList() {

        TaskScanApps.OnActionListener onActionListener = new TaskScanApps.OnActionListener() {
            boolean updated;

            @Override
            public void onScanStarted() {
                isScanning = true;
                isScanCompleted = false;
                btnCoolDown.setEnabled(false);
                btnCoolDown.setText(R.string.fetching_running_apps);
                runningAppListAdapter.resetData();
                textViewTempTitle2.setText(getString(R.string.msg_optimize_running_apps, "0"));
            }

            @Override
            public void onScanProgressUpdated(AppsListItem appItem, int current, int max) {
                if (updated) return;
                if (appItem != null) {
                    runningAppListAdapter.addItem(appItem, true);
                    textViewTempTitle2.setText(getString(R.string.msg_optimize_running_apps,
                            String.valueOf(current)));
                }
            }

            @Override
            public void onScanCompleted() {
                updated = true;
                isScanning = false;
                isScanCompleted = true;
                btnCoolDown.setEnabled(true);
                btnCoolDown.setText(R.string.btn_cool_down);
            }
        };
        taskScanApps = new TaskScanApps(mActivity, onActionListener);
        taskScanApps.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void stopScanning() {
        isScanning = false;
        try {
            if (taskScanApps != null) {
                taskScanApps.cancel(true);
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }
    }

    private final HandleMessage handle = new HandleMessage();

    private class HandleMessage extends Handler {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == Constants.CPU_TEMPERATURE) {
                float c = msg.getData().getFloat("TEMP");
                float f = c * 9 / 5 + 32;

                textViewTemperatureC.setText(numberFormat.format(c));
                textViewTemperatureF.setText(numberFormat.format(f));
                String helthStatus;
                if (c < Constants.HEALTHY_CPU_TEMPERATURE_LOW) {
                    helthStatus = Constants.HEALTHY_CPU_TEMP_STATUS[0];
                } else if (c >= Constants.HEALTHY_CPU_TEMPERATURE_HIGH
                        && c < Constants.HEALTHY_CPU_TEMPERATURE_OVERHEATING) {
                    helthStatus = Constants.HEALTHY_CPU_TEMP_STATUS[2];
                } else if (c >= Constants.HEALTHY_CPU_TEMPERATURE_OVERHEATING) {
                    helthStatus = Constants.HEALTHY_CPU_TEMP_STATUS[3];
                } else {
                    helthStatus = Constants.HEALTHY_CPU_TEMP_STATUS[1];
                }
                textViewTemperatureStatus.setText(getString(R.string.cpu_temperature_is, helthStatus));
            }
        }
    }

    private final BroadcastReceiver broadcastreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float batteryTemp = (float) (intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10;
            Bundle data = new Bundle();
            data.putFloat("TEMP", batteryTemp);
            Message msg = new Message();
            msg.what = Constants.CPU_TEMPERATURE;
            msg.setData(data);
            handle.sendMessage(msg);

        }
    };
}
