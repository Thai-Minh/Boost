package com.cleaning.boost.ibooster.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.activity.JunkCleanedActivity;
import com.cleaning.boost.ibooster.activity.MainActivity;
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

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class HomeJunkFragment extends Fragment {

    private static HomeJunkFragment homeJunkFragment;
    @BindView(R.id.textViewJunkSize)
    TextView textViewJunkSize;
    @BindView(R.id.textViewJunkSizeSup)
    TextView textViewJunkSizeSup;
    @BindView(R.id.textViewJunkSizeSub)
    TextView textViewJunkSizeSub;
    @BindView(R.id.constraintLayoutJunkSize)
    ConstraintLayout constraintLayoutJunkSize;
    @BindView(R.id.textViewJunkMessage)
    TextView textViewJunkMessage;
    @BindView(R.id.layoutCleanedJustNow)
    LinearLayout layoutCleanedJustNow;
    @BindView(R.id.homeLayoutClean)
    LinearLayout homeLayoutClean;


    private Unbinder unbinder;
    private boolean isScanCompleted = false;
    private boolean isScanning = false;
    private long totalCacheSize = 0;
    private int scanProgress = 0;
    private TaskScanCache taskScanCache;
    private boolean hasUsageAccessPermission = false;

    public static HomeJunkFragment getInstance() {
        if (homeJunkFragment == null) homeJunkFragment = new HomeJunkFragment();
        return homeJunkFragment;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setLanguage(getContext(), null);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_junk, container, false);
        unbinder = ButterKnife.bind(this, view);
        homeLayoutClean.setVisibility(View.VISIBLE);
        layoutCleanedJustNow.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindData();

    }

    private void bindData() {

        try {
            // Neu thoi diem clean cache gan nhat trong vong khoang 60 phut thi show "Just cleaned now!"
            long cleanedTime = SharedPrefUtils.getLong(getContext(), Constants.SHARED_PREF_CLEANED_TIME, 0);
            if (System.currentTimeMillis() - cleanedTime < Constants.TIME_SEPARATOR_CLEANED) {
                layoutCleanedJustNow.setVisibility(View.VISIBLE);
                homeLayoutClean.setVisibility(View.GONE);
                return;
            }
            homeLayoutClean.setVisibility(View.VISIBLE);
            layoutCleanedJustNow.setVisibility(View.GONE);

            if (isScanCompleted || isScanning) return;
            hasUsageAccessPermission = PackageUtils.hasUsageStatsPermission(getContext());
            constraintLayoutJunkSize.setVisibility(View.INVISIBLE);

            if (hasUsageAccessPermission) {
                constraintLayoutJunkSize.setVisibility(View.VISIBLE);
                textViewJunkMessage.setText(R.string.msg_clean_to_speedup);

                startScanning();

            } else {
                textViewJunkMessage.setText(R.string.msg_grant_usage_access_permission);
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
            e.printStackTrace();
        }
    }

    @OnClick(R.id.textViewJunkMessage)
    public void onJunkActionClick() {

        if (CommonUtils.isFastDoubleClick()) return;

        if (hasUsageAccessPermission) { // CLEAN NOW

            if (isScanCompleted) {
                if (!ActivityUtils.hasPermission(getActivity(), Constants.READ_WRITE_STORAGE_PERMISSIONS)) {
                    ActivityUtils.requestPermissions(getActivity(),
                            Constants.READ_WRITE_STORAGE_PERMISSIONS, Constants.RC_READ_WRITE_STORAGE);
                    return;
                }

                startCleanJunk();

            }

        } else {    // GRANT PERMISSION
            Utils.showUsageAccessSettingsPopup(Objects.requireNonNull(getActivity()));
        }
    }

    @Override
    public void onDestroy() {
        stopScanning();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unbind the view to free some memory
        unbinder.unbind();
    }

    private void stopScanning() {
        isScanning = false;
        isScanCompleted = false;
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
            } else {
            }

            switch (msg.what) {
                case Constants.FETCH_PACKAGE_SIZE_COMPLETED:
                case Constants.ALL_PACKAGE_SIZE_COMPLETED:
                    try {
                        String cacheSizeString = Formatter.formatFileSizeSI(getContext(), totalCacheSize);
                        String[] units = cacheSizeString.split(" ");
                        textViewJunkSize.setText(units[0].trim());
                        textViewJunkSizeSup.setText(units[1].trim());
                    } catch (Exception e) {
                        LogUtils.e(e.toString());
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void startScanning() {
        if (!isScanning) {

            TaskScanCache.OnActionListener onActionListener = new TaskScanCache.OnActionListener() {

                @Override
                public void onScanStarted(Context context) {
                    isScanCompleted = false;
                    isScanning = true;
                }

                @Override
                public void onScanProgressUpdated(Context context, AppsListItem appItem,
                                                  int current, int max, long cacheSize) {
                    scanProgress = (current * 100 / max);
                    if (scanProgress < 0) scanProgress = 0;
                    if (scanProgress > 100) scanProgress = 100;
                    totalCacheSize = cacheSize;
                    handle.sendEmptyMessage(Constants.FETCH_PACKAGE_SIZE_COMPLETED);
                }

                @Override
                public void onScanCompleted(Context context, List<AppsListItem> apps) {
                    handle.sendEmptyMessage(Constants.ALL_PACKAGE_SIZE_COMPLETED);
                    isScanCompleted = true;
                    isScanning = false;
                }
            };
            taskScanCache = new TaskScanCache(Objects.requireNonNull(getContext()), onActionListener);
            taskScanCache.setCreateAppItem(false);
            taskScanCache.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void startCleanJunk() {

        MainActivity mainActivity = (MainActivity) getActivity();

        // Clean junk cache
        OptimizeUtils.OptimizeListener listener = new OptimizeUtils.OptimizeListener() {

            @Override
            public void onStart() {

            }

            @Override
            public void onCompleted(AlertDialog dialog, Object[] object) {
                new Handler().postDelayed(() -> {
                    if (dialog != null) dialog.dismiss();
                    Intent intent = new Intent(getActivity(), JunkCleanedActivity.class);
                    intent.putExtra("CLEANED_LONG", (Long) object[0]);
                    if (mainActivity != null) {
                        mainActivity.startActivity(intent, false);
                        //mainActivity.showIntersAd();
                    }
                }, 1000);
            }
        };

        new OptimizeUtils(listener).cleanNow(getActivity());
    }

}
