package com.cleaning.boost.ibooster.model;

import android.graphics.drawable.Drawable;

public class AppsListItem {

    private final long mCacheSize;
    private final String mPackageName, mApplicationName;
    private final Drawable mIcon;

    public AppsListItem(String packageName, String applicationName, Drawable icon, long cacheSize) {
        mCacheSize = cacheSize;
        mPackageName = packageName;
        mApplicationName = applicationName;
        mIcon = icon;
    }

    public Drawable getApplicationIcon() {
        return mIcon;
    }

    public String getApplicationName() {
        return mApplicationName;
    }

    public long getCacheSize() {
        return mCacheSize;
    }

    public String getPackageName() {
        return mPackageName;
    }
}
