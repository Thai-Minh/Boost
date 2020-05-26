package com.cleaning.boost.ibooster.service;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.activity.PhoneBoostActivity;
import com.cleaning.boost.ibooster.utils.Constants;
import com.gmc.libs.NotificationUtils;
import com.gmc.libs.PackageUtils;
import com.gmc.libs.SharedPrefUtils;

import java.util.Random;

public class CheckRamReceiver extends BroadcastReceiver {

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context.getApplicationContext();
        long currTimeMillisecond = System.currentTimeMillis();
        //Timber.e("CheckRamReceiver currTimeMillisecond=%d", currTimeMillisecond);
        long lastTimeCheckRam = SharedPrefUtils.getLong(context, Constants.SHARED_PREF_LAST_TIME_CHECK_RAM, 0);
        //Timber.e("CheckRamReceiver lastTimeCheckRam=%d", lastTimeCheckRam);

        long subLastTimeCheckRam = currTimeMillisecond - lastTimeCheckRam;
        //Timber.e("CheckRamReceiver subLastTimeCheckRam=%d", subLastTimeCheckRam);

        if (lastTimeCheckRam == 0)
            SharedPrefUtils.save(context, Constants.SHARED_PREF_LAST_TIME_CHECK_RAM, currTimeMillisecond);

        if (lastTimeCheckRam > 0 && subLastTimeCheckRam >= Constants.TIME_SEPARATOR_CHECK_RAM_3H) {
            new GetRamInfoTask().execute();
        }

    }

    private class GetRamInfoTask extends AsyncTask<Void, Void, long[]> {
        @Override
        protected long[] doInBackground(Void... voids) {
            return PackageUtils.getRAMSize(context);
        }

        @Override
        protected void onPostExecute(long[] result) {

            try {
                SharedPrefUtils.save(context, Constants.SHARED_PREF_LAST_TIME_CHECK_RAM, System.currentTimeMillis());

                float freeRamPercent = result[1] * 100 / result[0];
                int processLeft = Math.round(freeRamPercent);
                int usedRamPercent = 100 - processLeft;

                String[] ramMessage = {
                        "%s RAM used, tap BOOST to optimize",
                        "Used RAM is %s, optimize now",
                        "Only %s RAM left, free memory to speed up.",
                        "%s RAM left, to optimize your phone, tap BOOST"
                };

                if (processLeft > 0 && processLeft <= 30) {

                    String sInfo = String.format(ramMessage[2], processLeft + "%");
                    int idx = new Random().nextInt(4);
                    if (idx == 0 || idx == 1) {
                        sInfo = String.format(ramMessage[idx], usedRamPercent + "%");
                    } else if (idx == 2 || idx == 3) {
                        sInfo = String.format(ramMessage[idx], processLeft + "%");
                    }

                    RemoteViews mRemoteView = new RemoteViews(context.getPackageName(), R.layout.notification_ram);
                    mRemoteView.setTextViewText(R.id.textViewRamMessage, sInfo);

                    NotificationUtils notificationUtils = NotificationUtils.create(
                            context, Constants.NOTIFICATION_CHANNEL_ID, Constants.NOTIFICATION_CHANNEL_NAME,
                            NotificationManagerCompat.IMPORTANCE_DEFAULT).builder(NotificationCompat.VISIBILITY_PUBLIC);

                    notificationUtils.setRemoteView(mRemoteView);
                    notificationUtils.getBuilder()
                            /*.setSmallIcon(R.drawable.ic_status_bar)*/
                            .setPriority(Notification.PRIORITY_DEFAULT)
                            .setAutoCancel(true);

                    Intent phoneBoostIntent = new Intent(context, PhoneBoostActivity.class);
                    phoneBoostIntent.putExtra(Constants.NOTIFICATION_INTENT, Constants.NOTIFICATION_BOOST_ID);

                    notificationUtils.setOnClickPendingIntentActivity(R.id.btnBoostNotification, phoneBoostIntent);
                    notificationUtils.createNotification();
                    notificationUtils.pushNotification(Constants.NOTIFICATION_BOOST_ID);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
