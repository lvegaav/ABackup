package com.americavoice.backup.main.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.americavoice.backup.R;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.di.components.DaggerAppComponent;
import com.americavoice.backup.main.ui.SplashScreenFragment;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.ButterKnife;


public class SplashScreenActivity extends BaseActivity implements HasComponent<AppComponent>,
        SplashScreenFragment.Listener  {

    private AppComponent mAppComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        this.initializeActivity(savedInstanceState);
        this.initializeInjector();
        this.initializeView();
    }

    /**
     * Initializes this activity.
     */
    private void initializeActivity(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            addFragment(R.id.fl_fragment, SplashScreenFragment.newInstance());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initializeView() {
    }

    private void initializeInjector() {
        ButterKnife.bind(this);
        this.mAppComponent = DaggerAppComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build();
    }

    @Override
    public AppComponent getComponent() {
        return mAppComponent;
    }


    @Override
    public void showHome() {
        navigator.navigateToMainActivity(this);
        finish();
    }

    @Override
    public void showValidation() {
        navigator.navigateToLoginActivity(this);
        finish();
    }

    @Override
    public void showPhoneNumber() {
        navigator.navigateToLoginActivity(this);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
