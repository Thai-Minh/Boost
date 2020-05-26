package com.cleaning.boost.ibooster.activity;

import android.os.Bundle;
import android.widget.Button;

import com.cleaning.boost.ibooster.R;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.btnPolicy)
    Button btnPolicy;
    @BindView(R.id.btnTerms)
    Button btnTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindContentView(R.layout.activity_settings);

        initView();
    }

    private void initView() {
        initToolbar(getResources().getString(R.string.mnu_settings));
    }

    @OnClick(R.id.btnPolicy)
    public void onPolicyClick() {
        startActivity(PrivacyPolicyActivity.class, false);
    }

    @OnClick(R.id.btnTerms)
    public void onTermsClick() {
        startActivity(TermOfServiceActivity.class, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // *** //
        f();
        // *** //
    }
}
