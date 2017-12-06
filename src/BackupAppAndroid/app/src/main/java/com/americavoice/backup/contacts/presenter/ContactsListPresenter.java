package com.americavoice.backup.contacts.presenter;

import android.support.annotation.NonNull;

import com.americavoice.backup.contacts.ui.ContactsBackupView;
import com.americavoice.backup.contacts.ui.ContactsListView;
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
public class ContactsListPresenter extends BasePresenter implements IPresenter {

    private ContactsListView mView;

    public void setView(@NonNull ContactsListView view) {
        this.mView = view;
    }

    @Inject
    public ContactsListPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
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
