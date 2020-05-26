package com.cleaning.boost.ibooster.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.cleaning.boost.ibooster.inf.ActionListener;
import com.gmc.libs.LogUtils;
import com.gmc.libs.PackageUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TaskBoost extends AsyncTask<Void, Void, Long[]> {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private final ActionListener actionListener;

    public TaskBoost(Context context, ActionListener listener) {
        this.context = context;
        this.actionListener = listener;
    }

    @Override
    protected void onPreExecute() {
        if (actionListener != null) {
            actionListener.onStarted(null);
        }
    }

    @Override
    protected Long[] doInBackground(Void... voids) {
        // get ram info before freed
        long[] ramsBefore = PackageUtils.getRAMSize(context);

        // free ram
        CountDownLatch countDownLatch = new CountDownLatch(0);
        OptimizeUtils.stopAllBackgroundProcess(context);
        PackageUtils.freeMemory();

        try {
            countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LogUtils.e(e.toString());}

        // get ram info after freed
        long[] ramsAfter = PackageUtils.getRAMSize(context);

        return new Long[]{ramsBefore[1], ramsAfter[1]};
    }

    @Override
    protected void onPostExecute(Long[] result) {
        if (actionListener != null) {
            actionListener.onCompleted(context, result);
        }
    }
}
