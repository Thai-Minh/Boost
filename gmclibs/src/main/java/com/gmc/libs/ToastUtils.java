package com.gmc.libs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtils extends Toast {

    public static final int SUCCESS = 1;
    public static final int WARNING = 2;
    public static final int ERROR = 3;
    public static final int INFO = 4;
    public static final int DEFAULT = 5;
    public static final int CONFUSING = 6;

    private static Toast toast;

    public ToastUtils(Context context) {
        super(context);
    }

    public static void dismiss() {
        if (toast != null) toast.cancel();
    }

    public static Toast makeText(Context context, String message, int duration, int type, boolean androidIcon) {
        if (toast != null) {
            toast.cancel();
        }
        toast = new Toast(context);
        toast.setDuration(duration);
        View layout = LayoutInflater.from(context).inflate(R.layout.gmclibs_layout_custom_toast, null, false);
        TextView l1 = layout.findViewById(R.id.toast_text);
        LinearLayout linearLayout = layout.findViewById(R.id.toast_type);
        ImageView img = layout.findViewById(R.id.toast_icon);
        ImageView img1 = layout.findViewById(R.id.imageView4);
        l1.setText(message);
        if (androidIcon == true)
            img1.setVisibility(View.VISIBLE);
        else if (androidIcon == false)
            img1.setVisibility(View.GONE);
        switch (type) {
            case SUCCESS:
                linearLayout.setBackgroundResource(R.drawable.gmclibs_shape_success);
                img.setImageResource(R.drawable.gmclibs_ic_check_black_24dp);
                break;
            case WARNING:
                linearLayout.setBackgroundResource(R.drawable.gmclibs_shape_warning);
                img.setImageResource(R.drawable.gmclibs_ic_pan_tool_black_24dp);
                break;
            case ERROR:
                linearLayout.setBackgroundResource(R.drawable.gmclibs_shape_error);
                img.setImageResource(R.drawable.gmclibs_ic_clear_black_24dp);
                break;
            case INFO:
                linearLayout.setBackgroundResource(R.drawable.gmclibs_shape_info);
                img.setImageResource(R.drawable.gmclibs_ic_info_outline_black_24dp);
                break;
            case CONFUSING:
                linearLayout.setBackgroundResource(R.drawable.gmclibs_shape_confusing);
                img.setImageResource(R.drawable.gmclibs_ic_refresh_black_24dp);
                break;
            case DEFAULT:
            default:
                linearLayout.setBackgroundResource(R.drawable.gmclibs_shape_default);
                img.setVisibility(View.GONE);
                break;
        }
        toast.setView(layout);
        return toast;
    }

    public static Toast makeText(Context context, String message, int duration, int type, int imageResource, boolean androidIcon) {
        Toast toast = new Toast(context);
        View layout = LayoutInflater.from(context).inflate(R.layout.gmclibs_layout_custom_toast, null, false);
        TextView l1 = layout.findViewById(R.id.toast_text);
        LinearLayout linearLayout = layout.findViewById(R.id.toast_type);
        ImageView img = layout.findViewById(R.id.toast_icon);
        ImageView img1 = layout.findViewById(R.id.imageView4);
        l1.setText(message);
        img.setImageResource(imageResource);
        if (androidIcon == true)
            img1.setVisibility(View.VISIBLE);
        else if (androidIcon == false)
            img1.setVisibility(View.GONE);
        switch (type) {
            case SUCCESS:
                linearLayout.setBackgroundResource(R.drawable.gmclibs_shape_success);
                break;
            case WARNING:
                linearLayout.setBackgroundResource(R.drawable.gmclibs_shape_warning);
                break;
            case ERROR:
                linearLayout.setBackgroundResource(R.drawable.gmclibs_shape_error);
                break;
            case INFO:
                linearLayout.setBackgroundResource(R.drawable.gmclibs_shape_info);
                break;
            case DEFAULT:
                linearLayout.setBackgroundResource(R.drawable.gmclibs_shape_default);
                img.setVisibility(View.GONE);
                break;
            case CONFUSING:
                linearLayout.setBackgroundResource(R.drawable.gmclibs_shape_confusing);
                break;
            default:
                linearLayout.setBackgroundResource(R.drawable.gmclibs_shape_default);
                img.setVisibility(View.GONE);
                break;
        }
        toast.setView(layout);
        return toast;
    }

    // Success
    public static void showSuccess(Context context, String message, int duration, boolean androidIcon) {
        makeText(context, message, duration, SUCCESS, androidIcon).show();
    }

    public static void showSuccess(Context context, String message, int duration, int imageResource, boolean androidIcon) {
        makeText(context, message, duration, SUCCESS, imageResource, androidIcon).show();
    }

    // Warning
    public static void showWarning(Context context, String message, int duration, boolean androidIcon) {
        makeText(context, message, duration, WARNING, androidIcon).show();
    }

    public static void showWarning(Context context, String message, int duration, int imageResource, boolean androidIcon) {
        makeText(context, message, duration, WARNING, imageResource, androidIcon).show();
    }

    // Error
    public static void showError(Context context, String message, int duration, boolean androidIcon) {
        makeText(context, message, duration, ERROR, androidIcon).show();
    }

    public static void showError(Context context, String message, int duration, int imageResource, boolean androidIcon) {
        makeText(context, message, duration, ERROR, imageResource, androidIcon).show();
    }

    // Info
    public static void showInfo(Context context, String message, int duration, boolean androidIcon) {
        makeText(context, message, duration, INFO, androidIcon).show();
    }

    public static void showInfo(Context context, String message, int duration, int imageResource, boolean androidIcon) {
        makeText(context, message, duration, INFO, imageResource, androidIcon).show();
    }

    // Confusing
    public static void showConfusing(Context context, String message, int duration, boolean androidIcon) {
        makeText(context, message, duration, CONFUSING, androidIcon).show();
    }

    public static void showConfusing(Context context, String message, int duration, int imageResource, boolean androidIcon) {
        makeText(context, message, duration, CONFUSING, imageResource, androidIcon).show();
    }

    // Default
    public static void showDefault(Context context, String message, int duration, boolean androidIcon) {
        makeText(context, message, duration, DEFAULT, androidIcon).show();
    }

    public static void showDefault(Context context, String message, int duration, int imageResource, boolean androidIcon) {
        makeText(context, message, duration, DEFAULT, imageResource, androidIcon).show();
    }
}
