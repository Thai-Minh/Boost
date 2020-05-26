package com.gmc.libs;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import java.util.Hashtable;

public class FontManager {

    public static final String FA_WEBFONT = "fontawesome-webfont.ttf";
    public static final String FA_FONT_REGULAR = "fa-regular-400.ttf";
    public static final String FA_FONT_SOLID = "fa-solid-900.ttf";
    public static final String FA_FONT_BRANDS = "fa-brands-400.ttf";
    private static Hashtable<String, Typeface> fontCache = new Hashtable<>();


    /**
     * Use:
     * Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FA_WEBFONT);
     * FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);
     * */

    public static Typeface get(Context context, String font) {
        Typeface typeface = fontCache.get(font);
        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), font);
            } catch (Exception e) {
                LogUtils.e("FontManager", e.toString());
                return null;
            }
            fontCache.put(font, typeface);
        }
        return typeface;
    }

    public static Typeface getTypeface(Context context, String font) {
        return get(context, font);
    }

    public static void markAsIconContainer(View v, Typeface typeface) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                markAsIconContainer(child, typeface);
            }
        } else if (v instanceof AppCompatTextView) {
            ((AppCompatTextView) v).setTypeface(typeface);
        } else if (v instanceof TextView) {
            ((TextView) v).setTypeface(typeface);
        }
    }

}
