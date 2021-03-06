
package com.americavoice.backup.main.ui;

import com.google.firebase.analytics.FirebaseAnalytics;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.americavoice.backup.R;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.main.ui.dialog.DialogError;

import org.greenrobot.eventbus.EventBus;


/**
 * Base {@link Fragment} class for every fragment in this application.
 */
public abstract class BaseFragment extends Fragment {

    protected ProgressDialog mProgress;
    protected FirebaseAnalytics mFirebaseAnalytics;
    protected boolean mDialogIsShowing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Shows a {@link Toast} message.
     *
     * @param message An string representing a message to be shown.
     */
    protected void showToastMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows a {@link Toast} message.
     *
     * @param message An string representing a message to be shown.
     */
    protected void showSnackMessage(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    protected void showDialogMessage(String message) {
        DialogError.newInstance(message).show(getFragmentManager(), "Error Fragment");
    }

    /**
     * Gets a component for dependency injection by its type.
     */
    @SuppressWarnings("unchecked")
    protected <C> C getComponent(Class<C> componentType) {
        return componentType.cast(((HasComponent<C>) getActivity()).getComponent());
    }

    public void setTitle(String title) {
        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        final ActionBar toolBar = activity.getSupportActionBar();
        if (null != toolBar) {
            toolBar.setTitle(title);
        }
    }

    protected void showDialog(String message) {
        hideDialog();
        mProgress = new ProgressDialog(getActivity(), R.style.WhiteDialog);
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
    public void onDestroy() {
        super.onDestroy();
        if (mProgress != null) {
            mProgress.dismiss();
            mProgress = null;
        }
    }
}
