package com.americavoice.backup.payment.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.americavoice.backup.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by javier on 10/23/17.
 */

public class PaymentActivity extends AppCompatActivity {

    private Unbinder mUnbind;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.content)
    FrameLayout mContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        mUnbind = ButterKnife.bind(this);
        mToolbar.setTitle(getString(R.string.payment_title));
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbind.unbind();
    }
}
