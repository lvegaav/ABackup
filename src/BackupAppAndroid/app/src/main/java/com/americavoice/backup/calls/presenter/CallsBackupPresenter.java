package com.americavoice.backup.calls.presenter;

import android.support.annotation.NonNull;

import com.americavoice.backup.calls.ui.CallsBackupView;
import com.americavoice.backup.contacts.ui.ContactsBackupView;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;

import javax.inject.Inject;

/**
 * Created by angelchanquin on 8/10/17.
 */

/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class CallsBackupPresenter extends BasePresenter implements IPresenter {

    private CallsBackupView mView;

    public void setView(@NonNull CallsBackupView view) {
        this.mView = view;
    }

    @Inject
    public CallsBackupPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    /**
     * Initializes the presenter
     * @param title title of the activity.
     */
    public void initialize(String title) {
        if (this.mView != null) {
            mView.setTitle(title);
        }

    }
}
