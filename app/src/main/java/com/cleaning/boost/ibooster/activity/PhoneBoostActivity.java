package com.cleaning.boost.ibooster.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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
import com.gmc.libs.Formatter;
import com.gmc.libs.LogUtils;
import com.gmc.libs.PackageUtils;
import com.gmc.libs.ResourceUtils;
import com.gmc.libs.SharedPrefUtils;
import com.gmc.libs.ToastUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class PhoneBoostActivity extends BaseActivity {

    private final PhoneBoostActivity mActivity = this;

    @BindView(R.id.frameLayout)
    FrameLayout frameLayout;
    @BindView(R.id.textViewRamPercent)
    TextView textViewRamPercent;
    @BindView(R.id.textView0)
    TextView textView0;
    @BindView(R.id.textViewTotal)
    TextView textViewTotal;
    @BindView(R.id.textViewAvailable)
    TextView textViewAvailable;
    @BindView(R.id.textView03)
    TextView textView03;
    @BindView(R.id.constraintLayout)
    ConstraintLayout constraintLayout;
    @BindView(R.id.imageViewRunnungApps)
    ImageView imageViewRunnungApps;
    @BindView(R.id.textViewRunnungHeader)
    TextView textViewRunnungHeader;
    @BindView(R.id.textViewAppCounter)
    TextView textViewAppCounter;
    @BindView(R.id.constraintLayoutRunningApps)
    ConstraintLayout constraintLayoutRunningApps;
    @BindView(R.id.recyclerViewRunningAppList)
    RecyclerView recyclerViewRunningAppList;
    @BindView(R.id.btnBoostNow)
    Button btnBoostNow;
    @BindView(R.id.bottomArea)
    LinearLayout bottomArea;
    @BindView(R.id.constraintLayoutNoneOptimize)
    ConstraintLayout constraintLayoutNoneOptimize;
    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.imageView1)
    ImageView imageView1;
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.btnDone)
    Button btnDone;
    @BindView(R.id.constraintLayoutOptimize)
    ConstraintLayout constraintLayoutOptimize;

    private boolean hasUsageAccessPermission = false;
    private boolean isScanCompleted = false;
    private boolean isScanning = false;
    private AppListAdapter runningAppListAdapter;
    private TaskScanApps taskScanApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindContentView(R.layout.activity_phone_boost);

        initView();
    }

    private void initView() {
        initToolbar(getResources().getString(R.string.phone_boost));

        textViewRamPercent.setText(R.string._0);
        textViewTotal.setText(R.string.empty);
        textViewAvailable.setText(R.string.empty);
        textViewAppCounter.setText(R.string._0);

        runningAppListAdapter = new AppListAdapter(mActivity, new ArrayList<>());
        recyclerViewRunningAppList.setLayoutManager(new LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false));
        recyclerViewRunningAppList.setAdapter(runningAppListAdapter);
    }

    private void bindData() {

        // Neu thoi diem clean cache gan nhat trong vong khoang 15 phut thi show "... optimized!"
        long cleanedTime = SharedPrefUtils.getLong(mActivity, Constants.SHARED_PREF_PHONE_BOOST_TIME, 0);
        if (System.currentTimeMillis() - cleanedTime < Constants.TIME_SEPARATOR_BOOSTED) {
            constraintLayoutOptimize.setVisibility(View.VISIBLE);
            // Neu vua moi clean (cach khoang <15'), show rectange banner 2
            /* */
            //loadRecBannerAd2();
            return;
            /**/
        }

        constraintLayoutOptimize.setVisibility(View.GONE);

        getRamInfo();

        getRunningProcess();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // *** //
        f();
        // *** //
        try {
            int fromNotification = getIntent().getIntExtra(Constants.NOTIFICATION_INTENT, 0);
            if (fromNotification == Constants.NOTIFICATION_BOOST_ID) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationManager.cancel(Constants.NOTIFICATION_BOOST_ID);
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }

        bindData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        stopScanning();
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

    @OnClick(R.id.btnBoostNow)
    void onBoostNowClick() {
        if (CommonUtils.isFastDoubleClick()) return;
        if (hasUsageAccessPermission) {
            OptimizeUtils.OptimizeListener actionListener = new OptimizeUtils.OptimizeListener() {

                @Override
                public void onStart() {
                    //loadIntersAd();
                }

                @Override
                public void onCompleted(AlertDialog dialog, Object[] object) {

                    try {
                        if (object != null && object.length == 2) {
                            long freed = (Long) object[0] - (Long) object[1];
                            if (freed > 0) {
                                String sFreed = Formatter.formatFileSizeSI(mActivity, freed) + " RAM has been freed";
                                ToastUtils.showSuccess(mActivity, sFreed, Toast.LENGTH_LONG, false);
                            }
                        }
                    } catch (Exception e) {
                        LogUtils.e(e.toString());
                    }

                    new Handler().postDelayed(() -> {
                        if (dialog != null) dialog.dismiss();
                        //showIntersAd();
                        constraintLayoutOptimize.setVisibility(View.VISIBLE);
                        //loadRecBannerAd2();
                    }, 1000);
                }
            };
            new OptimizeUtils(actionListener).boostNow(mActivity);

        } else {// GRANT PERMISSION
            // PackageUtils.showUsageAccessSettings(mActivity);
            Utils.showUsageAccessSettingsPopup(mActivity);
        }
    }

    @OnClick(R.id.btnDone)
    void onDoneClick() {
        finish();
    }

    private boolean isRunning = false;

    private void getRamInfo() {
        if (isRunning) return;
        isRunning = true;
        Thread timer = new Thread() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                long[] rams = PackageUtils.getRAMSize(mActivity);
                                long used = rams[0] - rams[1];
                                float ramPercent = used * 100 / rams[0];
                                int process = Math.round(ramPercent);
                                if (process > 100) process = 100;
                                textViewRamPercent.setText(String.valueOf(process));

                                String total = Formatter.formatFileSizeSI(mActivity, rams[0]);
                                String available = Formatter.formatFileSizeSI(mActivity, rams[1]);
                                textViewTotal.setText(getString(R.string.total_, total));
                                textViewAvailable.setText(getString(R.string.free_, available));
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        timer.start();
    }

    private void getRunningProcess() {

        if (isScanCompleted || isScanning) return;

        // Kiem tra xem da grant usage access permission chua?
        hasUsageAccessPermission = PackageUtils.hasUsageStatsPermission(mActivity);
        if (hasUsageAccessPermission) {
            // Neu da co quyen usage access
            textViewRunnungHeader.setTextColor(ResourceUtils.getColor(mActivity, R.color.black_text));
            recyclerViewRunningAppList.setVisibility(View.VISIBLE);
            btnBoostNow.setText(R.string.btn_boost_now);
            btnBoostNow.setEnabled(true);

            //loadSmallBannerAd();

            loadAppList();

        } else {
            // Neu chua co quyen usage access
            textViewRunnungHeader.setText(R.string.msg_grant_usage_access_permission);
            textViewRunnungHeader.setTextColor(ResourceUtils.getColor(mActivity, R.color.gmc_color_red_A700));
            textViewAppCounter.setText(R.string._0);
            recyclerViewRunningAppList.setVisibility(View.GONE);
            btnBoostNow.setText(R.string.btn_grant);

            // show rec ad 1
            //loadRecBannerAd();

        }
    }

    private void loadAppList() {

        LogUtils.e("loadAppList");

        TaskScanApps.OnActionListener onActionListener = new TaskScanApps.OnActionListener() {
            boolean updated;

            @Override
            public void onScanStarted() {
                isScanning = true;
                isScanCompleted = false;
                btnBoostNow.setEnabled(false);
                btnBoostNow.setText(R.string.fetching_running_apps);
                runningAppListAdapter.resetData();
                textViewRunnungHeader.setText(R.string.fetching_running_apps);
                textViewAppCounter.setText(R.string._0);
            }

            @Override
            public void onScanProgressUpdated(AppsListItem appItem, int current, int max) {
                if (updated) return;
                if (appItem != null) {
                    runningAppListAdapter.addItem(appItem, true);
                    textViewAppCounter.setText(String.valueOf(current));
                }
            }

            @Override
            public void onScanCompleted() {
                updated = true;
                isScanning = false;
                isScanCompleted = true;
                btnBoostNow.setEnabled(true);
                btnBoostNow.setText(R.string.btn_boost_now);
                textViewRunnungHeader.setText(R.string.running_apps);
            }
        };
        taskScanApps = new TaskScanApps(mActivity, onActionListener);
        taskScanApps.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }
}
