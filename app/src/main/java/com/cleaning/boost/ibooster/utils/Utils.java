package com.cleaning.boost.ibooster.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;

import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.adapter.UsageDialogViewPagerAdapter;
import com.gmc.libs.LogUtils;
import com.gmc.libs.PackageUtils;
import com.gmc.libs.ResourceUtils;
import com.gmc.libs.SharedPrefUtils;
import com.gmc.libs.StringUtils;

import java.util.Locale;

public class Utils {

    // Check whether this app has android write settings permission.
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean hasWriteSettingsPermission(Context context) {
        boolean ret = true;
        try {
            ret = Settings.System.canWrite(context);
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }
        return ret;
    }

    // Start can modify system settings panel to let user change the write settings permission.
    public static void changeWriteSettingsPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static AlertDialog createPopup(Context context, View viewContent, String title, String content,
                                          String[] btnString, DialogInterface.OnClickListener[] onClickListener,
                                          int iconResId, boolean cancelable, boolean isFullscreen) {

        try {
            if (btnString != null && onClickListener != null) {
                if (btnString.length != onClickListener.length) {
                    LogUtils.e("Dialog invalid [stringResId or onClickListener]");
                    return null;
                } else if (btnString.length > 3) {
                    LogUtils.e("Dialog invalid [over number button]");
                    return null;
                }
            }
            AlertDialog dialog = !isFullscreen ? new AlertDialog.Builder(context).create() :
                    new AlertDialog.Builder(context, R.style.full_screen_dialog).create();

            dialog.setCancelable(cancelable);
            dialog.setCanceledOnTouchOutside(cancelable);

            if (!StringUtils.isNullOrEmpty(title)) {
                int pad = ResourceUtils.dpToPx(context, 16);
                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setPadding(pad, pad, pad, pad);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setGravity(Gravity.CENTER_VERTICAL);

                if (iconResId > 0) {
                    ImageView icon = new ImageView(context);
                    icon.setImageResource(iconResId);
                    linearLayout.addView(icon);
                }

                TextView customTitle = new TextView(context);
                customTitle.setText(title);
                customTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                customTitle.setPadding(pad, 0, pad, 0);
                customTitle.setTextColor(ResourceUtils.getColor(context, R.color.colorPrimaryText));
                linearLayout.addView(customTitle);

                dialog.setCustomTitle(linearLayout);
                //dialog.setTitle(title);
            }

            if (viewContent != null) {
                dialog.setView(viewContent);
            } else if (!StringUtils.isNullOrEmpty(content)) {
                dialog.setMessage(content);
            }

            if (btnString != null && btnString.length > 0 && onClickListener != null) {
                int[] btnWhich = {DialogInterface.BUTTON_NEGATIVE, DialogInterface.BUTTON_NEUTRAL,
                        DialogInterface.BUTTON_POSITIVE};
                for (int idx = 0; idx < btnWhich.length; idx++) {
                    if (onClickListener[idx] != null) {
                        dialog.setButton(btnWhich[idx], btnString[idx], onClickListener[idx]);
                    }
                }

                dialog.setOnShowListener(dialogInterface -> {
                    Button btnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                    if (btnNegative != null) {
                        btnNegative.setTextColor(ResourceUtils.getColor(context, R.color.gmc_color_grey_500));
                    }
                    Button btnNeutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                    if (btnNeutral != null) {
                        btnNeutral.setTextColor(ResourceUtils.getColor(context, R.color.colorPrimary));
                    }
                    Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    if (btnPositive != null) {
                        btnPositive.setTextColor(ResourceUtils.getColor(context, R.color.gmc_color_green_A700));
                    }
                });

            }
            return dialog;
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }
        return null;
    }

    public static void showUsageAccessSettingsPopup(Activity activity) {

        // viewPagerUsageDialog
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_usage_access, null, false);

        // add bottom dot pager
        TextView[] dots = new TextView[2];
        LinearLayout layoutDots = dialogView.findViewById(R.id.layoutDots);
        layoutDots.removeAllViews();
        int padding = ResourceUtils.dpToPx(activity, 2);
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(activity);
            dots[i].setText(StringUtils.fromHtml("&#8226;"));
            dots[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);
            dots[i].setTextColor(ResourceUtils.getColor(activity, R.color.dot_inactive));
            dots[i].setPadding(padding, padding, padding, padding);
            layoutDots.addView(dots[i]);
        }
        dots[0].setTextColor(ResourceUtils.getColor(activity, R.color.yellow_overlay1));

        // set pager adapter
        ViewPager viewPager = dialogView.findViewById(R.id.viewPagerUsageDialog);
        viewPager.setAdapter(new UsageDialogViewPagerAdapter(activity));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                dots[0].setTextColor(ResourceUtils.getColor(activity, R.color.dot_inactive));
                dots[1].setTextColor(ResourceUtils.getColor(activity, R.color.dot_inactive));
                dots[position].setTextColor(ResourceUtils.getColor(activity, R.color.yellow_overlay1));
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });

        // create usage dialog
        AlertDialog dialog = createPopup(activity, dialogView, null,
                null, null, null, 0, false, false);

        if (dialog == null) return;

        Button btnDeny = dialogView.findViewById(R.id.buttonDeny);
        Button btnGrant = dialogView.findViewById(R.id.buttonGrant);

        btnDeny.setOnClickListener(view -> dialog.dismiss());
        btnGrant.setOnClickListener(view -> {
            PackageUtils.showUsageAccessSettings(activity);
            dialog.dismiss();
        });
        dialog.show();
    }

    public static void showNotificationAccessPopup(Context context) {

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_notification_settings, null, false);

        AlertDialog dialog = Utils.createPopup(context, dialogView, null,
                null, null, null, 0, false, false);
        if (dialog == null) return;
        Button btnDeny = dialogView.findViewById(R.id.buttonDeny);
        Button btnGrant = dialogView.findViewById(R.id.buttonGrant);

        btnDeny.setOnClickListener(view -> dialog.dismiss());
        btnGrant.setOnClickListener(view -> {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
            context.startActivity(intent);
            dialog.dismiss();
        });
        dialog.show();

    }

    public static void a(Activity activity) {
        if (!activity.getPackageName().equals(new String(Constants.pkgs))) {
            activity.finish();
            System.exit(1);
        }
    }

    public static void e(Activity activity) {
        if (!activity.getPackageName().equals(new String(Constants.pkgs))) {
            activity.finish();
            System.exit(1);
        }
    }

    public static void f(Activity activity) {
        if (!activity.getPackageName().equals(new String(Constants.pkgs))) {
            activity.finish();
            System.exit(1);
        }
    }

    public static boolean setLanguage(Context context, String langCode) {

        if (StringUtils.isNullOrEmpty(langCode)) {
            langCode = SharedPrefUtils.getString(context, Constants.SHARED_PREF_LANGUAGE, "en");
        }

        try {
            Locale sysLocale;
            Resources rs = context.getResources();
            Configuration config = rs.getConfiguration();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sysLocale = config.getLocales().get(0);
            } else {
                sysLocale = config.locale;
            }

            LogUtils.e("sysLocale.getLanguage()=" + sysLocale.getLanguage());

            if (!"".equals(langCode) && !sysLocale.getLanguage().equals(langCode)) {
                Locale locale = new Locale(langCode.toLowerCase());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    config.setLocale(locale);
                } else {
                    config.locale = locale;
                }
                rs.updateConfiguration(config, rs.getDisplayMetrics());
                return true;
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }
        return false;
    }

    public static void openApp(Activity activity, String packageName) {
        try {
            if (isAppInstalled(activity, packageName)) {
                Intent intent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
                if (intent == null) return;
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }

}
