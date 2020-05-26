package com.cleaning.boost.ibooster.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.cleaning.boost.ibooster.BuildConfig;
import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.fragment.HomeCoolerFragment;
import com.cleaning.boost.ibooster.fragment.HomeJunkFragment;
import com.cleaning.boost.ibooster.fragment.HomePhoneBoostFragment;
import com.cleaning.boost.ibooster.model.MemoryViewModel;
import com.cleaning.boost.ibooster.utils.Constants;
import com.cleaning.boost.ibooster.utils.Utils;
import com.gmc.libs.ActivityUtils;
import com.gmc.libs.CommonUtils;
import com.gmc.libs.LogUtils;
import com.gmc.libs.PackageUtils;
import com.gmc.libs.ResourceUtils;
import com.gmc.libs.SharedPrefUtils;
import com.gmc.libs.ToastUtils;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private final Activity activity = this;

    @BindView(R.id.txtAppName)
    TextView txtAppName;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.textViewPermissionSticker)
    TextView textViewPermissionSticker;
    @BindView(R.id.btnPermissionSetting)
    Button btnPermissionSetting;
    @BindView(R.id.linearLayoutPermissionSetting)
    LinearLayout linearLayoutPermissionSetting;
    @BindView(R.id.frame_PhoneBoost)
    FrameLayout framePhoneBoost;
    @BindView(R.id.btnPhoneBoost)
    ConstraintLayout btnPhoneBoost;
    @BindView(R.id.frame_JunkFiles)
    FrameLayout frameJunkFiles;
    @BindView(R.id.btnJunkFiles)
    ConstraintLayout btnJunkFiles;
    @BindView(R.id.frame_CPUCooler)
    FrameLayout frameCPUCooler;
    @BindView(R.id.btnCPUCooler)
    ConstraintLayout btnCPUCooler;
    @BindView(R.id.layoutAd)
    LinearLayout layoutAd;
    @BindView(R.id.bottomArea)
    LinearLayout bottomArea;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    private boolean settingsCanWrite = true;
    private MemoryViewModel memoryViewModel;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Utils.setLanguage(this, null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // tao ModelView de truyen thong tin RAM cho HomeCoolerFragment va HomeMemoryFragment
        ViewModelProvider.Factory factory = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication());
        memoryViewModel = new ViewModelProvider(getViewModelStore(), factory).get(MemoryViewModel.class);

        initView();

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                drawerLayout.closeDrawers();
                fragmentManager = getSupportFragmentManager();
                switch (menuItem.getItemId()) {
                    case R.id.nav_rate_star:
                        ActivityUtils.rateAppOnStore(getApplicationContext());
                        return true;
                    case R.id.nav_policy:
                        Intent intentPolicy = new Intent(getApplicationContext(), PrivacyPolicyActivity.class);
                        intentPolicy.putExtra(Constants.EXTRA_ACTIVITY, Constants.ACTIVITY_POLICY);
                        startActivity(intentPolicy, false);
                        return true;
                    case R.id.nav_about:
                        showAbout();
                        return true;
                    case R.id.nav_language:
                        showLanguagePopup();
                        return true;
                }
                return true;
            }
        });

        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        drawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Respond when the drawer is opened
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion state changes
                    }
                }
        );
    }

    private void initView() {

        initFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_rate_star:
                ActivityUtils.rateAppOnStore(this);
                return true;
            case R.id.action_policy:
                Intent intentPolicy = new Intent(this, PrivacyPolicyActivity.class);
                intentPolicy.putExtra(Constants.EXTRA_ACTIVITY, Constants.ACTIVITY_POLICY);
                startActivity(intentPolicy, false);
                return true;
            case R.id.action_about:
                showAbout();
                return true;
            case R.id.action_language:
                showLanguagePopup();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

    @OnClick(R.id.btnJunkFiles)
    public void onJunkFilesClick() {
        startActivity(JunkActivity.class, false);
    }

    @OnClick(R.id.btnPhoneBoost)
    public void onPhoneBoostClick() {
        startActivity(PhoneBoostActivity.class, false);
    }

    @OnClick(R.id.btnCPUCooler)
    public void onCPUCoolerClick() {
        startActivity(CPUCoolerActivity.class, false);
    }

    @OnClick(R.id.btnPermissionSetting)
    public void onPermissionSettingClick() {
        Intent intentPermission = new Intent(this, PermissionManagerActivity.class);
        intentPermission.putExtra(Constants.EXTRA_ACTIVITY, Constants.ACTIVITY_PERMISSION);
        startActivity(intentPermission, false);
    }

    private void initFragment() {
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        HomeJunkFragment junkFragment = new HomeJunkFragment();
        HomeCoolerFragment coolerFragment = new HomeCoolerFragment();
        HomePhoneBoostFragment phoneBoostFragment = new HomePhoneBoostFragment();

        fragmentTransaction.add(R.id.frame_JunkFiles, junkFragment);
        fragmentTransaction.add(R.id.frame_CPUCooler, coolerFragment);
        fragmentTransaction.add(R.id.frame_PhoneBoost, phoneBoostFragment);

        /*changeLocaleFrag(junkFragment);
        changeLocaleFrag(coolerFragment);
        changeLocaleFrag(phoneBoostFragment);*/

        fragmentTransaction.commit();
    }

    private void changeLocaleFrag(Fragment fragment){
        fragmentTransaction.detach(fragment);
        fragmentTransaction.attach(fragment);
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

    @Override
    protected void onResume() {
        super.onResume();

        f();

        // Check whether has the write settings permission or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            settingsCanWrite = Utils.hasWriteSettingsPermission(getApplicationContext());
        }

        // check total permission need to grant to show sticker SETTINGS
        int needGrant = 0;
        if (!PackageUtils.hasUsageStatsPermission(this)) {
            needGrant++;
        }
        if (!PackageUtils.hasStoragePermission(this)) {
            needGrant++;
        }

        // get Ram Info
        getRamInfo();

        try {
            // needGrant = 1;
            if (needGrant == 0) {
                linearLayoutPermissionSetting.setVisibility(View.GONE);

                ConstraintLayout.LayoutParams newLayoutParams = (ConstraintLayout.LayoutParams) btnPhoneBoost.getLayoutParams();
                newLayoutParams.topMargin = ResourceUtils.dpToPx(getApplicationContext(), 40);
                btnPhoneBoost.setLayoutParams(newLayoutParams);
            } else {
                Utils.setLanguage(this, null);

                linearLayoutPermissionSetting.setVisibility(View.VISIBLE);
                textViewPermissionSticker.setText(getString(R.string.msg_permission_need_allow, needGrant));

                ConstraintLayout.LayoutParams newLayoutParams = (ConstraintLayout.LayoutParams) btnPhoneBoost.getLayoutParams();
                newLayoutParams.topMargin = ResourceUtils.dpToPx(getApplicationContext(), 6);
                btnPhoneBoost.setLayoutParams(newLayoutParams);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isRunning = false;

    private void getRamInfo() {
        isRunning = true;
        Thread timer = new Thread() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        runOnUiThread(() -> {
                            long[] rams = PackageUtils.getRAMSize(activity);
                            // Truyen thong tin vao ModelView cho HomeCoolerFragment va HomeMemoryFragment
                            memoryViewModel.setName(rams[0] + "-" + rams[1]);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        timer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
    }

    private long backPressedTime = 0;

    @Override
    public void onBackPressed() {
        long currMills = System.currentTimeMillis();
        if (currMills - backPressedTime > 2000) {
            backPressedTime = currMills;
            ToastUtils.showInfo(this, getResources().getString(R.string.msg_press_again_to_exit),
                    Toast.LENGTH_SHORT, false);
        } else {
            ToastUtils.dismiss();
            super.onBackPressed();
        }
    }

    private void f() {
        Utils.a(this);
        Utils.e(this);
        Utils.f(this);
    }

    public void startActivity(Class cls, boolean isNewTask) {
        if (CommonUtils.isFastDoubleClick()) return;
        ActivityUtils.startActivity(this, cls, isNewTask);
    }

    public void startActivity(Intent intent, boolean isNewTask) {
        if (CommonUtils.isFastDoubleClick()) return;
        ActivityUtils.startActivity(this, intent, isNewTask);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.RC_READ_WRITE_STORAGE) {
            if (ActivityUtils.hasPermission(activity, Constants.READ_WRITE_STORAGE_PERMISSIONS)) {
                try {
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.frame_JunkFiles + ":1");
                    if (fragment != null) {
                        if (fragment instanceof HomeJunkFragment) {
                            ((HomeJunkFragment) fragment).startCleanJunk();
                        }
                    }
                } catch (Exception e) {
                    LogUtils.e(e.toString());
                }
            }
        }
    }
}
