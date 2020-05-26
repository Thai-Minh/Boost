package com.gmc.libs.device;

import java.util.List;

public interface OnTaskScanListener {
    void onScanStarted();

    void onScanProgressUpdated(int current, int max, CacheItem cacheItem);

    void onScanCompleted(List<CacheItem> apps);
}
