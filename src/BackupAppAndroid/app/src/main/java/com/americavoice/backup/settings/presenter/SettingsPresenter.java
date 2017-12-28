
package com.americavoice.backup.settings.presenter;

import android.support.annotation.NonNull;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.settings.ui.SettingsView;

import javax.inject.Inject;


/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class SettingsPresenter extends BasePresenter implements IPresenter {

    private SettingsView mView;

    private static final String SHOW_CASE_ALREADY = "SETTINGS_SHOW_CASE_ALREADY";

    @Inject
    SettingsPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull SettingsView view) {
        this.mView = view;
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
     */
    public void initialize() {
        if (!mSharedPrefsUtils.getBooleanPreference(SHOW_CASE_ALREADY, false)) {
            mView.showGuidedTour();
        }
    }

    public void logout() {
        mNetworkProvider.logout();
    }

    public void showStorageInfo() {

    }

    public void showCaseFinished() {
        mSharedPrefsUtils.setBooleanPreference(SHOW_CASE_ALREADY, true);
    }
}
