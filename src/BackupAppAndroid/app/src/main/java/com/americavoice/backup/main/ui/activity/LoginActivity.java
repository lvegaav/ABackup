package com.americavoice.backup.main.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountAuthenticatorActivity;
import com.americavoice.backup.confirmation.ui.ConfirmationFragment;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.di.components.DaggerAppComponent;
import com.americavoice.backup.login.ui.LoginFragment;

import butterknife.ButterKnife;

public class LoginActivity extends AccountAuthenticatorActivity implements HasComponent<AppComponent>,
        LoginFragment.Listener,
        ConfirmationFragment.Listener {



    public static final byte ACTION_CREATE = 0;
    public static final byte ACTION_UPDATE_TOKEN = 1;               // requested by the user
    public static final byte ACTION_UPDATE_EXPIRED_TOKEN = 2;       // detected by the app

    private AppComponent mAppComponent;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.initializeActivity(savedInstanceState);
        this.initializeInjector();
        this.initializeView();
    }

    /**
     * Initializes this activity.
     */
    private void initializeActivity(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            addFragment(R.id.fl_fragment, LoginFragment.newInstance());
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
    public void viewHome() {
        navigator.navigateToMainActivity(this);
        finish();
    }

    @Override
    public void onBackConfirmationClicked() {
        finish();
    }

    @Override
    public void viewValidation() {
        replaceFragment(R.id.fl_fragment, ConfirmationFragment.newInstance(), true, true);
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        Bundle b = new Bundle();
        result.putExtras(b);
        setAccountAuthenticatorResult(null); // null means the user cancelled the authorization processs
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onBackLoginClicked() {

    }

}
