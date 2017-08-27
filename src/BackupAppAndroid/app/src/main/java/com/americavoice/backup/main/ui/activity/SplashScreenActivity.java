package com.americavoice.backup.main.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import butterknife.ButterKnife;


public class SplashScreenActivity extends BaseActivity implements HasComponent<AppComponent>,
        SplashScreenFragment.Listener,  ProviderInstaller.ProviderInstallListener  {

    private AppComponent mAppComponent;
    private static final int ERROR_DIALOG_REQUEST_CODE = 1;
    private boolean mRetryProviderInstall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        this.initializeActivity(savedInstanceState);
        this.initializeInjector();
        this.initializeView();
        ProviderInstaller.installIfNeededAsync(this, this);
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
    public void onProviderInstalled() {
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                showHome();
            }
        }, 2000);
    }

    @Override
    public void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            // Recoverable error. Show a dialog prompting the user to
            // install/update/enable Google Play services.
            GooglePlayServicesUtil.showErrorDialogFragment(
                    errorCode,
                    this,
                    ERROR_DIALOG_REQUEST_CODE,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            // The user chose not to take the recovery action
                            onProviderInstallerNotAvailable();
                        }
                    });
        } else {
            // Google Play services is not available.
            onProviderInstallerNotAvailable();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ERROR_DIALOG_REQUEST_CODE) {
            // Adding a fragment via GooglePlayServicesUtil.showErrorDialogFragment
            // before the instance state is restored throws an error. So instead,
            // set a flag here, which will cause the fragment to delay until
            // onPostResume.
            mRetryProviderInstall = true;
        }
    }
    /**
     * On resume, check to see if we flagged that we need to reinstall the
     * provider.
     */

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mRetryProviderInstall) {
            // We can now safely retry installation.
            ProviderInstaller.installIfNeededAsync(this, this);
        }
        mRetryProviderInstall = false;
    }

    private void onProviderInstallerNotAvailable() {
        Crashlytics.logException(new Exception("onProviderInstallerNotAvailable"));
        Toast.makeText(this, "Version no soportada...", Toast.LENGTH_LONG).show();
        // This is reached if the provider cannot be updated for some reason.
        // App should consider all HTTP communication to be vulnerable, and take
        // appropriate action.
    }

}
