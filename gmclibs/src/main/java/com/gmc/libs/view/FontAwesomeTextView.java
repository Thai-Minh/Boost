package com.gmc.libs.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.gmc.libs.FontManager;
import com.gmc.libs.LogUtils;
import com.gmc.libs.R;

public class FontAwesomeTextView extends AppCompatTextView {

    private boolean isBrandingIcon, isSolidIcon;

    public FontAwesomeTextView(Context context) {
        super(context);
    }

    public FontAwesomeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontAwesomeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FontAwesome,
                0, 0);
        isSolidIcon = a.getBoolean(R.styleable.FontAwesome_solid_icon, false);
        isBrandingIcon = a.getBoolean(R.styleable.FontAwesome_brand_icon, false);
        init();
    }

    private void init() {
        LogUtils.e("isBrandingIcon=" + isBrandingIcon + " | isSolidIcon=" + isSolidIcon);
        if (isBrandingIcon)
            setTypeface(FontManager.get(getContext(), FontManager.FA_FONT_BRANDS));
        else if (isSolidIcon)
            setTypeface(FontManager.get(getContext(), FontManager.FA_FONT_SOLID));
        else
            setTypeface(FontManager.get(getContext(), FontManager.FA_FONT_REGULAR));
    }
}
