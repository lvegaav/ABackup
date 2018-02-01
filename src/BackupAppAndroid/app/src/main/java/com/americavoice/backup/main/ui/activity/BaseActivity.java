package com.americavoice.backup.main.ui.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.americavoice.backup.AndroidApplication;
import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.di.components.ApplicationComponent;
import com.americavoice.backup.di.modules.ActivityModule;
import com.americavoice.backup.main.navigation.Navigator;
import com.americavoice.backup.utils.DisplayUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Inject;

/**
 * Base {@link android.app.Activity} class for every Activity in this application.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Inject
    protected Navigator navigator;

    protected FirebaseAnalytics mFirebaseAnalytics;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getApplicationComponent().inject(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if (!DisplayUtils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * Adds a {@link Fragment} to this activity's layout.
     *
     * @param containerViewId The container view to where add the fragment.
     * @param fragment The fragment to be added.
     */
    protected void addFragment(int containerViewId, Fragment fragment) {
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(containerViewId, fragment);
        fragmentTransaction.commit();
    }

    /**
     * Adds a {@link Fragment} to this activity's layout.
     *
     * @param containerViewId The container view to where add the fragment.
     * @param fragment The fragment to be added.
     */
    public void replaceFragment(int containerViewId, Fragment fragment, boolean forward, boolean addToBackStack) {
        if(isFinishing()) return;

        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();

        if (forward) {
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        } else {
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        }

        fragmentTransaction.replace(containerViewId, fragment);
        if (addToBackStack){
            fragmentTransaction.addToBackStack(fragment.getTag());
        } else {
            fragmentTransaction.disallowAddToBackStack();
        }

        fragmentTransaction.commitAllowingStateLoss();
    }

    /**
     * Get the Main Application component for dependency injection.
     *
     * @return {@link ApplicationComponent}
     */
    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication)getApplication()).getApplicationComponent();
    }

    /**
     * Get an Activity module for dependency injection.
     *
     * @return {@link ActivityModule}
     */
    protected ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }

    protected void showDialog(String message) {
        hideDialog();
        mProgress = new ProgressDialog(this, R.style.WhiteDialog);
        mProgress.setTitle(getResources().getString(R.string.app_name));
        mProgress.setMessage(message);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);
        mProgress.show();
    }

    protected void hideDialog() {
        if (mProgress != null) {
            mProgress.hide();
            mProgress.dismiss();
            mProgress = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgress != null) {
            mProgress.dismiss();
            mProgress = null;
        }
    }
}
