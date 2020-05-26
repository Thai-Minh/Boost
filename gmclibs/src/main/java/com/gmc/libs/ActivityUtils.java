package com.gmc.libs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class ActivityUtils {

    public static final int ANIM_TYPE_DEFAULT = 0;
    public static final int ANIM_TYPE_SLIDE = 1;
    public static final int ANIM_TYPE_FADE = 2;
    public static final String ANIM_TYPE = "ANIM_TYPE";

    public static void rateAppOnStore(Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + context.getApplicationInfo().packageName)));
    }

    public static void moreApps(Context context, String pubName) {
        context.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://search?q=pub:" + pubName)));
    }

    public static void share(Context context, String title, String subject, String message) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        context.startActivity(Intent.createChooser(shareIntent, title));
    }

    public static void showDefaultAlert(Context context, String title, String message, String closeText) {
        final androidx.appcompat.app.AlertDialog dialog
                = new androidx.appcompat.app.AlertDialog.Builder(context).create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, closeText, (dialog1, which) -> dialog1.dismiss());
        dialog.show();
    }

    public static void openSMSComposer(Context context, String number, String message) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.fromParts("sms", number, null));
        intent.putExtra("sms_body", message);
        context.startActivity(intent);
    }

    public static void openEmailComposer(Context context, String title, String[] mailtos, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SEND,
                Uri.fromParts("mailto", TextUtils.join(",", mailtos), null));
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_EMAIL, mailtos);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, ResourceUtils.fromHtml(body));
        intent.putExtra(Intent.EXTRA_HTML_TEXT, body);
        context.startActivity(Intent.createChooser(intent, title));
    }

    public static void startActivity(Context context, Intent intent, boolean isNewTask) {
        if (isNewTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        context.startActivity(intent);
    }

    public static void startActivity(Context context, Class cls, boolean isNewTask) {
        Intent intent = new Intent(context, cls);
        startActivity(context, intent, isNewTask);
    }

    public static void startActivity(Activity context, Intent intent, int animType, boolean isNewTask) {
        intent.putExtra(ANIM_TYPE, animType);
        startActivity(context, intent, isNewTask);
        switch (animType) {
            case ANIM_TYPE_SLIDE:
                context.overridePendingTransition(R.anim.gmclibs_slide_in_from_right, R.anim.gmclibs_slide_out_to_left);
                break;
            case ANIM_TYPE_FADE:
                context.overridePendingTransition(R.anim.gmclibs_fade_in, R.anim.gmclibs_fade_out);
                break;
            default:
                break;
        }
    }

    public static void startActivity(Activity context, Class cls, int animType, boolean isNewTask) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(ANIM_TYPE, animType);
        startActivity(context, intent, isNewTask);
        switch (animType) {
            case ANIM_TYPE_SLIDE:
                context.overridePendingTransition(R.anim.gmclibs_slide_in_from_right, R.anim.gmclibs_slide_out_to_left);
                break;
            case ANIM_TYPE_FADE:
                context.overridePendingTransition(R.anim.gmclibs_fade_in, R.anim.gmclibs_fade_out);
                break;
            default:
                break;
        }
    }

    public static void finishActivity(Activity context) {
        int animType = context.getIntent().getIntExtra(ANIM_TYPE, ANIM_TYPE_DEFAULT);
        switch (animType) {
            case ANIM_TYPE_SLIDE:
                context.overridePendingTransition(R.anim.gmclibs_slide_in_from_left, R.anim.gmclibs_slide_out_to_right);
                break;
            case ANIM_TYPE_FADE:
                context.overridePendingTransition(R.anim.gmclibs_fade_in, R.anim.gmclibs_fade_out);
                break;
            default:
                break;
        }
    }

    public static void addFragment(FragmentActivity activity, Fragment fragment, int containerFrameId) {
        activity.getSupportFragmentManager().beginTransaction()
                .add(containerFrameId, fragment)
                .addToBackStack(null)
                .commit();
    }

    public static void replaceFragment(FragmentActivity activity, Fragment fragment, int containerFrameId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerFrameId, fragment)
                .addToBackStack(null)
                .commit();
    }

    public static boolean hasPermission(Context context, String[] permissions) {

        for(String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        try {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
