
package com.americavoice.backup.main.presenter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.ui.SplashScreenView;

import net.servicestack.client.AsyncResult;

import javax.inject.Inject;


/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class SplashScreenPresenter extends BasePresenter implements IPresenter {
    private static final long SPLASH_DISPLAY_LENGTH = 3000;
    private SplashScreenView mView;

    @Inject
    public SplashScreenPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull SplashScreenView view) {
        this.mView = view;
    }

    @Override
    public void resume() {
        String phoneNumber = mSharedPrefsUtils
                .getStringPreference(NetworkProvider.KEY_PHONE_NUMBER, "");

        mNetworkProvider.login(phoneNumber, new AsyncResult<dtos.AuthenticateResponse>() {
            @Override
            public void error(Exception ex) {
                mNetworkProvider.logout();
            }
        });
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

    }
}
