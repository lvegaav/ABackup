package com.americavoice.backup.main.ui.activity;

import android.os.Bundle;

import com.americavoice.backup.R;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.di.components.DaggerAppComponent;
import com.americavoice.backup.main.ui.SplashScreenFragment;

import butterknife.ButterKnife;


public class SplashScreenActivity extends BaseActivity implements HasComponent<AppComponent>,
        SplashScreenFragment.Listener {

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
    protected void onResume() {
        super.onResume();
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
        navigator.navigateToConfirmationActivity(this);
        finish();
    }

    @Override
    public void showPhoneNumber() {
        navigator.navigateToLoginActivity(this);
        finish();
    }
}
