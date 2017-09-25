package com.americavoice.backup.settings.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.americavoice.backup.R;
import com.owncloud.android.lib.common.utils.Log_OC;

/**
 * Created by javier on 9/25/17.
 * Contact us activity
 */

public class ContactUsActivity extends AppCompatActivity implements View.OnClickListener{

    private View mBlankView;
    private View mFacebookLink;
    private View mPhoneLink;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        mBlankView = findViewById(R.id.blank_space);
        mFacebookLink = findViewById(R.id.facebook_link);
        mPhoneLink = findViewById(R.id.phone_link);
        mBlankView.setOnClickListener(this);
        mFacebookLink.setOnClickListener(this);
        mPhoneLink.setOnClickListener(this);
        setTitle("");

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
                intent.setData(Uri.parse(getString(R.string.contact_us_facebook_link)));
                startActivity(intent);
                break;
            case R.id.phone_link:
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + getString(R.string.contact_us_phone)));
                startActivity(intent);
                break;
        }
        finish();
    }
}
