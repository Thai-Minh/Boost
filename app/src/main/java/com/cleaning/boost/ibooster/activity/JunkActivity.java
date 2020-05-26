package com.cleaning.boost.ibooster.activity;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.adapter.AppListAdapter;
import com.cleaning.boost.ibooster.model.AppsListItem;
import com.cleaning.boost.ibooster.task.OptimizeUtils;
import com.cleaning.boost.ibooster.task.TaskScanCache;
import com.cleaning.boost.ibooster.utils.Constants;
import com.cleaning.boost.ibooster.utils.Utils;
import com.gmc.libs.ActivityUtils;
import com.gmc.libs.CommonUtils;
import com.gmc.libs.Formatter;
import com.gmc.libs.LogUtils;
import com.gmc.libs.PackageUtils;
import com.gmc.libs.ResourceUtils;
import com.gmc.libs.SharedPrefUtils;
import com.gmc.libs.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class JunkActivity extends BaseActivity {

    private final JunkActivity mActivity = this;
    @BindView(R.id.textViewJunkSize)
    TextView textViewJunkSize;
    @BindView(R.id.textViewJunkSizeSup)
    TextView textViewJunkSizeSup;
    @BindView(R.id.textViewJunkSizeSub)
    TextView textViewJunkSizeSub;
    @BindView(R.id.textViewScanningMsg)
    TextView textViewScanningMsg;
    @BindView(R.id.textViewExplain)
    TextView textViewExplain;
    @BindView(R.id.junkHeadArea)
    ConstraintLayout junkHeadArea;
    @BindView(R.id.recyclerViewAppList)
    RecyclerView recyclerViewAppList;
    @BindView(R.id.btnCleanNow)
    Button btnCleanNow;
    @BindView(R.id.bottomArea)
    LinearLayout bottomArea;
    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.btnDone)
    Button btnDone;
    @BindView(R.id.layoutCleanedJustNow)
    ConstraintLayout layoutCleanedJustNow;

    private boolean isScanCompleted = false;
    private boolean isScanning = false;
    private long totalCacheSize = 0;
    private int scanProgress = 0;
    private TaskScanCache taskScanCache;
    private boolean hasUsageAccessPermission = false;
    private AppListAdapter cacheListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindContentView(R.layout.activity_junk);

        initView();

        // bindData();
    }

    private void initView() {
        initToolbar(getResources().getString(R.string.junk_files));
        textViewScanningMsg.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.btnCleanNow)
    public void onCleanNowClick() {

        if (CommonUtils.isFastDoubleClick()) return;

        if (hasUsageAccessPermission) { // CLEAN NOW
            if (isScanCompleted) {

                // check read/write storage
                if (!ActivityUtils.hasPermission(mActivity, Constants.READ_WRITE_STORAGE_PERMISSIONS)) {
                    ActivityUtils.requestPermissions(mActivity,
                            Constants.READ_WRITE_STORAGE_PERMISSIONS, Constants.RC_READ_WRITE_STORAGE);
                    return;
                }

                startCleanJunk();

            } else {
                ToastUtils.showWarning(mActivity,
                        getResources().getString(R.string.btn_scanning_), Toast.LENGTH_SHORT, false);
            }

        } else {    // GRANT PERMISSION
            // PackageUtils.showUsageAccessSettings(mActivity);
            Utils.showUsageAccessSettingsPopup(mActivity);
        }
    }

    @OnClick(R.id.btnDone)
    public void onDoneClick() {
        finish();
    }

    private void initializeAdapter(List<AppsListItem> items) {
        // packageSize = 0;
        cacheListAdapter = new AppListAdapter(mActivity, items);
        recyclerViewAppList.setLayoutManager(new LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false));
        recyclerViewAppList.setAdapter(cacheListAdapter);
    }

    private void bindData() {

        if (isScanCompleted || isScanning) return;

        // Neu thoi diem clean cache gan nhat trong vong khoang 30 phut thi show "Just cleaned now!"
        long cleanedTime = SharedPrefUtils.getLong(mActivity, Constants.SHARED_PREF_CLEANED_TIME, 0);
        if (System.currentTimeMillis() - cleanedTime < Constants.TIME_SEPARATOR_CLEANED) {
            layoutCleanedJustNow.setVisibility(View.VISIBLE);
            // Neu vua moi clean (cach khoang <30'), show rectange banner 2
            /**/
            //loadRecBannerAd2();
            isScanCompleted = true;
            return;
            /**/
        }
        layoutCleanedJustNow.setVisibility(View.GONE);

        hasUsageAccessPermission = PackageUtils.hasUsageStatsPermission(mActivity);
        if (hasUsageAccessPermission) {
            recyclerViewAppList.setVisibility(View.VISIBLE);
            scanJunk();
            //loadSmallBannerAd();
        } else {
            textViewExplain.setText(R.string.msg_grant_usage_access_permission);
            textViewExplain.setTextColor(ResourceUtils.getColor(mActivity, R.color.gmc_color_red_A400));
            btnCleanNow.setText(R.string.btn_grant);
            btnCleanNow.setEnabled(true);
            recyclerViewAppList.setVisibility(View.GONE);
            // Neu chua duoc grant permission, show rectange banner 1
            //loadRecBannerAd();
        }
    }

    private void scanJunk() {

        textViewExplain.setText(R.string.msg_junk_files_is);
        textViewExplain.setTextColor(ResourceUtils.getColor(mActivity, R.color.gmc_color_grey_900));

        startScanning();
    }

    private void startCleanJunk() {
        // Sau khi clean completed, show message Just cleaned now! & Rec banner 2
        OptimizeUtils.OptimizeListener listener = new OptimizeUtils.OptimizeListener() {

            @Override
            public void onStart() {
                //loadIntersAd();
            }

            @Override
            public void onCompleted(AlertDialog dialog, Object[] object) {
                new Handler().postDelayed(() -> {
                    if (dialog != null) dialog.dismiss();
                    //showIntersAd();
                    layoutCleanedJustNow.setVisibility(View.VISIBLE);
                    //loadRecBannerAd2();
                }, 1000);
            }
        };

        new OptimizeUtils(listener).cleanNow(mActivity);
    }

    @Override
    public void onStop() {
        //stopScanning();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        stopScanning();
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();
        // *** //
        f();
        // *** //
        try {
            int fromNotification = getIntent().getIntExtra(Constants.NOTIFICATION_INTENT, 0);
            if (fromNotification == Constants.NOTIFICATION_JUNK_FILES_ID) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(Constants.NOTIFICATION_JUNK_FILES_ID);
                }
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }

        bindData();
    }

    private void stopScanning() {
        LogUtils.e("JunkActivity", "stopScanning");
        isScanning = false;
        try {
            if (taskScanCache != null) {
                taskScanCache.cancel(true);
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler handle = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {

            if (!isScanCompleted) {
                String s = getResources().getString(R.string.scanning) + " " + scanProgress + "%";
                btnCleanNow.setText(s);
            } else {
                btnCleanNow.setText(R.string.btn_clean_now);
            }

            switch (msg.what) {
                case Constants.FETCH_PACKAGE_SIZE_COMPLETED:
                case Constants.ALL_PACKAGE_SIZE_COMPLETED:
                    String cacheSizeString = Formatter.formatFileSizeSI(mActivity, totalCacheSize);
                    String[] units = cacheSizeString.split(" ");
                    textViewJunkSize.setText(units[0].trim());
                    textViewJunkSizeSup.setText(units[1].trim());
                    break;
                default:
                    break;
            }
        }
    };

    private void startScanning() {
        if (!isScanning) {

            TaskScanCache.OnActionListener onActionListener = new TaskScanCache.OnActionListener() {
                boolean updated = false;

                @Override
                public void onScanStarted(Context context) {
                    updateStartUI();
                }

                @Override
                public void onScanProgressUpdated(Context context, AppsListItem appItem,
                                                  int current, int max, long cacheSize) {

                    if (updated) return;

                    if (appItem != null) {

                        cacheListAdapter.addItem(appItem, true);
                        cacheListAdapter.sort(AppListAdapter.CACHE_ITEM_SIZE_COMPARATOR);

                        updateScanProgressing(current, max, appItem.getPackageName());

                        totalCacheSize = cacheListAdapter.getCacheSize();
                        handle.sendEmptyMessage(Constants.FETCH_PACKAGE_SIZE_COMPLETED);
                    }

                    if (current >= max) {
                        updated = true;
                        totalCacheSize = cacheListAdapter.getCacheSize();
                        updateFinishUI();
                    } else {
                        isScanCompleted = false;
                        isScanning = true;
                    }

                }

                @Override
                public void onScanCompleted(Context context, List<AppsListItem> apps) {
                    totalCacheSize = cacheListAdapter.getCacheSize();
                    updateFinishUI();
                }
            };
            taskScanCache = new TaskScanCache(mActivity, onActionListener);
            taskScanCache.setCreateAppItem(true);
            taskScanCache.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void updateStartUI() {
        initializeAdapter(new ArrayList<>());

        textViewScanningMsg.setVisibility(View.VISIBLE);
        textViewScanningMsg.setGravity(Gravity.LEFT);
        textViewScanningMsg.setText(R.string.scanning);
        isScanCompleted = false;
        isScanning = true;
        btnCleanNow.setEnabled(false);
    }

    private void updateFinishUI() {
        // fetch all package size completed
        isScanCompleted = true;
        isScanning = false;
        btnCleanNow.setEnabled(true);
        textViewScanningMsg.setVisibility(View.VISIBLE);
        textViewScanningMsg.setGravity(Gravity.CENTER);
        textViewScanningMsg.setText(R.string.scan_completed);
        handle.sendEmptyMessage(Constants.ALL_PACKAGE_SIZE_COMPLETED);
    }

    private void updateScanProgressing(int current, int max, String pkgName) {
        scanProgress = (current * 100 / max);
        if (scanProgress < 0) scanProgress = 0;
        if (scanProgress > 100) scanProgress = 100;
        String sMsg = getResources().getString(R.string.scanning) + ": " + pkgName;
        textViewScanningMsg.setText(sMsg);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.RC_READ_WRITE_STORAGE) {
            if (ActivityUtils.hasPermission(mActivity, Constants.READ_WRITE_STORAGE_PERMISSIONS)) {
                startCleanJunk();
            }
        }
    }
}
