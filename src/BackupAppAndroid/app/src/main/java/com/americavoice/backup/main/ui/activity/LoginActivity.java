package com.americavoice.backup.main.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountAuthenticatorActivity;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.di.components.DaggerAppComponent;
import com.americavoice.backup.login.presenter.LoginNewPasswordPresenter;
import com.americavoice.backup.login.ui.LoginConfirmationFragment;
import com.americavoice.backup.login.ui.LoginConfirmationView;
import com.americavoice.backup.login.ui.LoginForgotFragment;
import com.americavoice.backup.login.ui.LoginFragment;
import com.americavoice.backup.login.ui.LoginNewPasswordFragment;
import com.americavoice.backup.login.ui.LoginNewPasswordSuccessFragment;
import com.americavoice.backup.login.ui.LoginRegisterFragment;
import com.americavoice.backup.main.event.OnBackPress;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;

public class LoginActivity extends AccountAuthenticatorActivity implements HasComponent<AppComponent>,
        LoginFragment.Listener,
        LoginConfirmationFragment.Listener,
        LoginRegisterFragment.Listener,
        LoginForgotFragment.Listener,
        LoginNewPasswordFragment.Listener,
        LoginNewPasswordSuccessFragment.Listener{



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
    public void onBackLoginClicked() {
        Intent result = new Intent();
        Bundle b = new Bundle();
        result.putExtras(b);
        setAccountAuthenticatorResult(null); // null means the user cancelled the authorization processs
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onBackLoginRegisterClicked() {
        replaceFragment(R.id.fl_fragment, LoginFragment.newInstance(), false, false);
    }

    @Override
    public void onBackConfirmationClicked() {
        replaceFragment(R.id.fl_fragment, LoginFragment.newInstance(), false, false);
    }

    @Override
    public void onBackLoginForgotClicked() {
        replaceFragment(R.id.fl_fragment, LoginFragment.newInstance(), false, false);
    }

    @Override
    public void onBackLoginNewPasswordClicked() {
        replaceFragment(R.id.fl_fragment, LoginForgotFragment.newInstance(), false, false);
    }

    @Override
    public void viewHome() {
        navigator.navigateToMainActivity(this);
        finish();
    }

    @Override
    public void viewValidation(String username, String device) {
        replaceFragment(R.id.fl_fragment, LoginConfirmationFragment.newInstance(username, device), true, false);
    }



    @Override
    public void viewRegister() {
        replaceFragment(R.id.fl_fragment, LoginRegisterFragment.newInstance(), true, false);
    }

    @Override
    public void viewForgot() {
        replaceFragment(R.id.fl_fragment, LoginForgotFragment.newInstance(), true, false);
    }

    @Override
    public void viewLoginNewPasswordView(String countryCode, String phoneNumber) {
        replaceFragment(R.id.fl_fragment, LoginNewPasswordFragment.newInstance(countryCode, phoneNumber), true, false);
    }

    @Override
    public void viewNewPasswordSuccess() {
        replaceFragment(R.id.fl_fragment, LoginNewPasswordSuccessFragment.newInstance(), true, false);
    }


    @Override
    public void viewLogin() {
        replaceFragment(R.id.fl_fragment, LoginFragment.newInstance(), true, false);
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().post(new OnBackPress());
    }

}
