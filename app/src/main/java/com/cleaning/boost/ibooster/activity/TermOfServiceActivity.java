package com.cleaning.boost.ibooster.activity;

import android.os.Bundle;

import com.cleaning.boost.ibooster.R;

public class TermOfServiceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindContentView(R.layout.activity_term_of_service);

        initView();

    }

    private void initView() {
        initToolbar(getResources().getString(R.string.terms));
        //loadSmallBannerAd();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // *** //
        f();
        // *** //
    }

}
