package com.americavoice.backup.settings.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.crashlytics.android.Crashlytics;
import com.owncloud.android.lib.common.utils.Log_OC;

/**
 * Created by javier on 9/25/17.
 * Contact us activity
 */

public class ContactUsActivity extends AppCompatActivity implements View.OnClickListener{

    SharedPrefsUtils mPrefsUtils;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        View mBlankView = findViewById(R.id.blank_space);
        View mFacebookLink = findViewById(R.id.facebook_link);
        View mPhoneLink = findViewById(R.id.phone_link);
        mBlankView.setOnClickListener(this);
        mFacebookLink.setOnClickListener(this);
        mPhoneLink.setOnClickListener(this);
        setTitle("");

        mPrefsUtils = new SharedPrefsUtils(this);

        TextView tvPhoneNumber = findViewById(R.id.phone_number);
        String callCenterPhone = mPrefsUtils.getStringPreference("callCenterPhone", "");
        tvPhoneNumber.setText(callCenterPhone);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.slide_up_activity, R.anim.slide_down_activity);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.facebook_link:
                intent = new Intent(Intent.ACTION_VIEW);
                String facebookUrl = mPrefsUtils.getStringPreference("facebookUrl", "");
                try {
                    intent.setData(Uri.parse(facebookUrl));
                    startActivity(intent);
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
                break;
            case R.id.phone_link:
                intent = new Intent(Intent.ACTION_DIAL);
                String callCenterPhone = mPrefsUtils.getStringPreference("callCenterPhone", "");
                try {
                    intent.setData(Uri.parse("tel:" + callCenterPhone));
                    startActivity(intent);
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
                break;
        }
        finish();
    }
}
