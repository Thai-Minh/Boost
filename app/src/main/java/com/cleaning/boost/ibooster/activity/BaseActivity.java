package com.cleaning.boost.ibooster.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import com.cleaning.boost.ibooster.BuildConfig;
import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.utils.Constants;
import com.cleaning.boost.ibooster.utils.Utils;
import com.gmc.libs.ActivityUtils;
import com.gmc.libs.CommonUtils;
import com.gmc.libs.LogUtils;
import com.gmc.libs.ResourceUtils;
import com.gmc.libs.SharedPrefUtils;
import com.gmc.libs.StringUtils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BaseActivity extends AppCompatActivity {

    private String sTitle;
    private LinearLayout.LayoutParams layoutParams;
    private LinearLayout.LayoutParams params300x250;

    @Nullable
    @BindView(R.id.layoutAd)
    LinearLayout layoutAd;
    @Nullable
    @BindView(R.id.layoutAdRec)
    LinearLayout layoutAdRec;
    @Nullable
    @BindView(R.id.layoutAdRec2)
    LinearLayout layoutAdRec2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected void bindContentView(int layoutResID) {
        Utils.setLanguage(this, null);

        setContentView(layoutResID);
        ButterKnife.bind(this);

        layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params300x250 = new LinearLayout.LayoutParams(
                ResourceUtils.dpToPx(this, 300), ResourceUtils.dpToPx(this, 250));

    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (Constants.ACTIVITY_FEEDBACK.equals(getIntent().getStringExtra(Constants.EXTRA_ACTIVITY))
                || Constants.ACTIVITY_POLICY.equals(getIntent().getStringExtra(Constants.EXTRA_ACTIVITY))
                || Constants.ACTIVITY_PERMISSION.equals(getIntent().getStringExtra(Constants.EXTRA_ACTIVITY))
        ) return false;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!getResources().getString(R.string.app_name).equalsIgnoreCase(sTitle)
                && !StringUtils.isNullOrEmpty(sTitle)) {
            if (item.getItemId() == android.R.id.home) {
                finish();
            }
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                if (!StringUtils.isNullOrEmpty(sTitle) &&
                        !getResources().getString(R.string.app_name).equalsIgnoreCase(sTitle)) {
                    finish();
                }
                break;
            case R.id.action_rate_star:
                ActivityUtils.rateAppOnStore(this);
                break;
            case R.id.action_policy:
                Intent intentPolicy = new Intent(this, PrivacyPolicyActivity.class);
                intentPolicy.putExtra(Constants.EXTRA_ACTIVITY, Constants.ACTIVITY_POLICY);
                startActivity(intentPolicy, false);
                break;
            case R.id.action_about:
                showAbout();
                break;
            case R.id.action_language:
                showLanguagePopup();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLanguagePopup() {
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

    protected void f() {
        Utils.a(this);
        Utils.e(this);
        Utils.f(this);
    }

    private void showAbout() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_about, null, false);

        AlertDialog dialog = Utils.createPopup(this, dialogView, null,
                null, null, null, 0, false, false);

        TextView textViewVersion = dialogView.findViewById(R.id.textViewVersion);
        textViewVersion.setText(getString(R.string.version, BuildConfig.VERSION_NAME));

        Button btnClose = dialogView.findViewById(R.id.btnClose);

        btnClose.setOnClickListener(view -> {
            assert dialog != null;
            dialog.dismiss();
        });
        if (dialog != null) {
            dialog.show();
        }
    }

    protected void initToolbar(String title) {
        sTitle = StringUtils.isNullOrEmpty(title) ? getResources().getString(R.string.app_name) : title;

        try {
            ActionBar actionbar = getSupportActionBar();
            ActionBar.LayoutParams layoutparams = new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
            TextView textview = new TextView(this);
            textview.setLayoutParams(layoutparams);
            textview.setText(sTitle);
            textview.setTextColor(Color.WHITE);
            textview.setGravity(Gravity.CENTER_VERTICAL);
            textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            if (actionbar != null) {
                actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                actionbar.setCustomView(textview);

                boolean hiddenIcon = StringUtils.isNullOrEmpty(title)
                        || getResources().getString(R.string.app_name).equalsIgnoreCase(title);
                actionbar.setDisplayHomeAsUpEnabled(!hiddenIcon);
                actionbar.setElevation(0f);
            }
        } catch (Exception e) {
            LogUtils.e("BaseActivity ", e.toString());
        }
    }

    public void startActivity(Class cls, boolean isNewTask) {
        if (CommonUtils.isFastDoubleClick()) return;
        ActivityUtils.startActivity(this, cls, isNewTask);
    }

    public void startActivity(Intent intent, boolean isNewTask) {
        if (CommonUtils.isFastDoubleClick()) return;
        ActivityUtils.startActivity(this, intent, isNewTask);
    }

    /*
     * LOAD AD
     * */


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
