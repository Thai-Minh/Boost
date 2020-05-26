package com.cleaning.boost.ibooster.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.utils.Constants;
import com.cleaning.boost.ibooster.utils.Utils;
import com.gmc.libs.PackageUtils;

import butterknife.BindView;

public class PermissionManagerActivity extends BaseActivity {

    private final PermissionManagerActivity mActivity = this;

    @BindView(R.id.linearLayoutPermissionList)
    LinearLayout linearLayoutPermissionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindContentView(R.layout.activity_permission_manager);

        initView();
    }

    private void initView() {
        initToolbar(getResources().getString(R.string.permission_manager));

    }

    @Override
    protected void onResume() {
        super.onResume();
        // *** //
        f();
        // *** //
        checkPermission();
    }

    private void checkPermission() {
        linearLayoutPermissionList.removeAllViews();
        View.OnClickListener[] clicks = {null, null, null};

        if(!PackageUtils.hasUsageStatsPermission(mActivity)) {
            clicks[0] = view -> Utils.showUsageAccessSettingsPopup(mActivity);

        }
        if (!PackageUtils.hasStoragePermission(mActivity)) {
            clicks[1] = view -> PackageUtils.requestStoragePermission(mActivity, Constants.RC_READ_WRITE_STORAGE);
        }

        String[] permissionTitles = getResources().getStringArray(R.array.permission_titles);
        String[] permissionDescs = getResources().getStringArray(R.array.permission_descs);

        linearLayoutPermissionList.addView(genPermissionItem(permissionTitles[0], permissionDescs[0], clicks[0]));
        linearLayoutPermissionList.addView(genPermissionItem(permissionTitles[1], permissionDescs[1], clicks[1]));
    }

    private View genPermissionItem(String title, String description, View.OnClickListener clickListener) {
        View view = getLayoutInflater().inflate(R.layout.item_permission, null, false);
        TextView textViewTitle = view.findViewById(R.id.textViewPermissionTitle);
        TextView textViewDesc = view.findViewById(R.id.textViewPermissionDescription);
        textViewTitle.setText(title);
        textViewDesc.setText(description);

        Button btnClick = view.findViewById(R.id.btnPermissionAllow);
        if (clickListener != null) {
            btnClick.setOnClickListener(clickListener);
            btnClick.setVisibility(View.VISIBLE);
        } else {
            btnClick.setVisibility(View.GONE);
        }

        return view;
    }

}
