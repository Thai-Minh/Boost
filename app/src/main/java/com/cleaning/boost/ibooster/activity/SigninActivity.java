package com.cleaning.boost.ibooster.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cleaning.boost.ibooster.BuildConfig;
import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.utils.Constants;
import com.cleaning.boost.ibooster.utils.Utils;
import com.gmc.libs.ActivityUtils;
import com.gmc.libs.LogUtils;
import com.gmc.libs.ResourceUtils;
import com.gmc.libs.SharedPrefUtils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SigninActivity extends AppCompatActivity {

    private final SigninActivity activity = this;

    @BindView(R.id.textViewVersionSplash)
    TextView textViewVersionSplash;
    @BindView(R.id.btnStartUsingSplash)
    Button btnStartUsingSplash;
    @BindView(R.id.txtLanguageSplash)
    TextView txtLanguageSplash;
    @BindView(R.id.textViewPolicySplash)
    TextView textViewPolicySplash;

    private String currLangCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        checkFirstTime();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        ButterKnife.bind(this);

        textViewVersionSplash.setText(getString(R.string.version, BuildConfig.VERSION_NAME));

        ClickableSpan policyClick = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                startActivity(new Intent(activity, PrivacyPolicyActivity.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(ResourceUtils.getColor(activity, R.color.white_overlay));
            }
        };

        textViewPolicySplash.setMovementMethod(LinkMovementMethod.getInstance());
        textViewPolicySplash.setHighlightColor(Color.TRANSPARENT);
        String textViewContent = textViewPolicySplash.getText().toString();
        SpannableString clickSpannable = new SpannableString(textViewContent);

        String policyTextClick = getString(R.string.privacy);

        int policyIdx = textViewContent.toLowerCase().indexOf(policyTextClick.toLowerCase());

        clickSpannable.setSpan(policyClick, policyIdx, policyIdx + policyTextClick.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewPolicySplash.setText(clickSpannable);

        for (String s : Constants.LANGUAGES) {
            String[] arrLangs = s.split(":");
            if (arrLangs[0].equalsIgnoreCase(currLangCode)) {
                txtLanguageSplash.setText(arrLangs[1]);
                break;
            }
        }
    }

    @OnClick(R.id.btnStartUsingSplash)
    public void onStartUsingClick() {
        SharedPrefUtils.save(this, Constants.SHARED_PREF_FIRST_RUN_TIME, System.currentTimeMillis());
        ActivityUtils.startActivity(this, MainActivity.class, false);
        finish();
    }

    private void checkFirstTime() {
        long firstRunTime = SharedPrefUtils.getLong(this, Constants.SHARED_PREF_FIRST_RUN_TIME, -1);
        if (firstRunTime != -1) {
            // this is not first run time
            ActivityUtils.startActivity(this, MainActivity.class, false);
            finish();
        }

        currLangCode = SharedPrefUtils.getString(this, Constants.SHARED_PREF_LANGUAGE, "en");
        Utils.setLanguage(this, currLangCode);
    }

    @OnClick(R.id.txtLanguageSplash)
    public void onTxtLanguageClick() {

        Configuration config = getResources().getConfiguration();

        String langCode = SharedPrefUtils.getString(this, Constants.SHARED_PREF_LANGUAGE, "-1");
        if ("-1".equals(langCode)) {
            Locale sysLocale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sysLocale = config.getLocales().get(0);
            } else {
                sysLocale = config.locale;
            }
            langCode = sysLocale.getLanguage().toLowerCase();
            LogUtils.e("sysLocale = " + sysLocale.getLanguage());
        }

        try {
            String[] langCodes = new String[Constants.LANGUAGES.length];
            String[] langNames = new String[Constants.LANGUAGES.length];

            int idx = 0, checkedItem = 0;
            for (String s : Constants.LANGUAGES) {
                String[] arrLangs = s.split(":");
                langCodes[idx] = arrLangs[0];
                langNames[idx] = arrLangs[1];
                if (arrLangs[0].equalsIgnoreCase(langCode)) {
                    checkedItem = idx;
                }
                idx++;
            }

            AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
            mBuilder.setTitle(getString(R.string.choose_language));
            mBuilder.setSingleChoiceItems(langNames, checkedItem, (dialogInterface, i) -> {
                changeLang(langCodes[i]);
                dialogInterface.dismiss();
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void changeLang(String langCode) {
        SharedPrefUtils.save(this, Constants.SHARED_PREF_LANGUAGE, langCode);
        if (Utils.setLanguage(this, langCode)) {
            recreate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // *** //
        Utils.a(this);
        Utils.e(this);
        Utils.f(this);
        // *** //
    }
}
