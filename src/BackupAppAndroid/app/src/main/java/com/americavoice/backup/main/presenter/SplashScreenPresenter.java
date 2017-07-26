
package com.americavoice.backup.main.presenter;

import android.os.Handler;
import android.support.annotation.NonNull;

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
        final String phoneNumber = mSharedPrefsUtils.getStringPreference(NetworkProvider.KEY_PHONE_NUMBER, null);
        if (phoneNumber == null)
        {
            mView.viewPhoneNumber();
            return;
        }

        mNetworkProvider.login(phoneNumber, new AsyncResult<dtos.AuthenticateResponse>() {
            @Override
            public void success(dtos.AuthenticateResponse response) {
                mView.viewHome();
            }

            @Override
            public void error(Exception ex) {
                dtos.SendResetPasswordSms request = new dtos.SendResetPasswordSms();
                request.setPhoneNumber(mNetworkProvider.getUserName(phoneNumber));
                mNetworkProvider.SendResetPasswordSms(request, new AsyncResult<dtos.SendResetPasswordSmsResponse>() {
                    @Override
                    public void success(dtos.SendResetPasswordSmsResponse response) {
                        mView.viewValidation();
                    }

                    @Override
                    public void error(Exception ex) {
                        mView.viewPhoneNumber();
                    }
                });
            }
        });
    }
}
