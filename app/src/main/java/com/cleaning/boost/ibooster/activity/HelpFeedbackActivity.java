package com.cleaning.boost.ibooster.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.utils.Constants;
import com.gmc.libs.CommonUtils;
import com.gmc.libs.StringUtils;
import com.gmc.libs.ToastUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class HelpFeedbackActivity extends BaseActivity {

    private final HelpFeedbackActivity mContext = this;

    @BindView(R.id.editTextFeedbackDetail)
    EditText editTextFeedbackDetail;
    @BindView(R.id.editTextFeedbackEmail)
    EditText editTextFeedbackEmail;
    @BindView(R.id.rbFeedbackBugs)
    RadioButton rbFeedbackBugs;
    @BindView(R.id.rbFeedbackSuggestion)
    RadioButton rbFeedbackSuggestion;
    @BindView(R.id.rbFeedbackOther)
    RadioButton rbFeedbackOther;
    @BindView(R.id.btnSendFeedback)
    Button btnSendFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindContentView(R.layout.activity_help_feedback);

        initView();
    }

    private void initView() {
        initToolbar(getResources().getString(R.string.title_feedback));
        //loadSmallBannerAd();
    }

    @OnClick(R.id.btnSendFeedback)
    public void onSendFeedbackClick() {
        String feedbackDetail = editTextFeedbackDetail.getText().toString();
        String feedbackEmail = editTextFeedbackEmail.getText().toString();
        String suggestion = "Suggestion";
        if (rbFeedbackBugs.isChecked()) {
            suggestion = "Bugs";
        } else if (rbFeedbackOther.isChecked()) {
            suggestion = "Other";
        }

        if (StringUtils.isNullOrEmpty(feedbackDetail)) {
            ToastUtils.showWarning(mContext, "Please input your describer", Toast.LENGTH_LONG, false);
            editTextFeedbackDetail.requestFocus();
            return;
        }

        if (StringUtils.isNullOrEmpty(feedbackEmail) || !StringUtils.validEmail(feedbackEmail)) {
            ToastUtils.showWarning(mContext, "Please valid your email", Toast.LENGTH_LONG, false);
            editTextFeedbackEmail.requestFocus();

            return;
        }

        sendFeedback(feedbackDetail, feedbackEmail, suggestion);
    }

    private void sendFeedback(String detail, String email, String suggestion) {
        String subject = "From " + email + ": " + suggestion;
        CommonUtils.sendEmail(this, Constants.EMAILS, subject, detail);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // *** //
        f();
        // *** //
    }

}
