package com.gmc.libs.device;

import com.gmc.libs.model.RunningAppsServices;

import java.util.List;

public interface OnTaskScanRunningAppsListener {
    void onScanStarted();

    void onScanProgressUpdated(int current, int max, RunningAppsServices runningApp);

    void onScanCompleted(List<RunningAppsServices> runningAppsList);
}
