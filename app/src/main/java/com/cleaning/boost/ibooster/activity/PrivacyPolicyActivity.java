package com.cleaning.boost.ibooster.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.cleaning.boost.ibooster.R;
import com.gmc.libs.ResourceUtils;
import com.gmc.libs.StringUtils;

import butterknife.BindView;

public class PrivacyPolicyActivity extends BaseActivity {


    @BindView(R.id.textViewPolicyContent)
    TextView textViewPolicyContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindContentView(R.layout.activity_privacy_policy);

        initView();

    }

    private void initView() {
        initToolbar(getResources().getString(R.string.privacy_policy));
        loadContent();
        //loadSmallBannerAd();
    }

    private void loadContent() {
        String content = ResourceUtils.getContentAssetFile(this, "privacy.policy");
        textViewPolicyContent.setText(StringUtils.fromHtml(content));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // *** //
        f();
        // *** //
    }
}
