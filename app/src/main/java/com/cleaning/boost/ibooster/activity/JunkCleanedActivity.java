package com.cleaning.boost.ibooster.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.cleaning.boost.ibooster.R;
import com.gmc.libs.Formatter;
import com.gmc.libs.LogUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class JunkCleanedActivity extends BaseActivity {

    private final JunkCleanedActivity mActivity = this;

    @BindView(R.id.textViewCleaned)
    TextView textViewCleaned;
    @BindView(R.id.btnDone)
    Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindContentView(R.layout.activity_junk_cleaned);

        initView();

    }

    private void initView() {
        initToolbar(getResources().getString(R.string.junk_files));
        //loadRecBannerAd();
        long cleanedSize = getIntent().getLongExtra("CLEANED_LONG", -1);
        LogUtils.e("Cleaned " + cleanedSize);
        if (cleanedSize > 0) {
            String s = Formatter.formatFileSizeSI(mActivity, cleanedSize);
            textViewCleaned.setText(getResources().getString(R.string.msg_junk_cleaned, s));
        }
    }

    @OnClick(R.id.btnDone)
    public void onDoneClick() {
        finish();
    }
}
