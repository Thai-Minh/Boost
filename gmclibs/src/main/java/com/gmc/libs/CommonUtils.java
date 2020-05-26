package com.gmc.libs;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class CommonUtils {

    private static long lastClickTime;

    private static long TIME = 500;
    private static long exitTime = 0;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < TIME) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static void sendEmail(Context context, String[] recipients, String subject, String body) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , recipients);
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT   , body);
        try {
            context.startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            ToastUtils.showWarning(context,
                    "There are no email clients installed.", Toast.LENGTH_SHORT, false);
        }
    }
}
