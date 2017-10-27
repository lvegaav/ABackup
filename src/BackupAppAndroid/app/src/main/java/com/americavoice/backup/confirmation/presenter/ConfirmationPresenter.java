
package com.americavoice.backup.confirmation.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.americavoice.backup.confirmation.ui.ConfirmationView;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.login.ui.LoginView;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.exception.ErrorBundle;
import com.americavoice.backup.main.exception.ErrorMessageFactory;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.crashlytics.android.Crashlytics;

import net.servicestack.client.AsyncResult;

import javax.inject.Inject;


/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class ConfirmationPresenter extends BasePresenter implements IPresenter {

    private ConfirmationView mView;

    @Inject
    ConfirmationPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull ConfirmationView view) {
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
        mNetworkProvider.logout();    //Logout server
    }

    public void submit(final String code) {

        boolean hasError = false;
        if (TextUtils.isEmpty(code)) {
            hasError = true;
            mView.showConfirmationCodeInvalid();
        }
        if (hasError) return;

        final String phoneNumber = mSharedPrefsUtils.getStringPreference(NetworkProvider.KEY_PHONE_NUMBER, null);

        dtos.PerformResetPassword request = new dtos.PerformResetPassword();
        request.setPhoneNumber(mNetworkProvider.getUserName(phoneNumber));
        request.setNewPassword(mNetworkProvider.getDeviceId());
        request.setResetPasswordCode(code);
        mView.showLoading();
        mNetworkProvider.PerformResetPassword(request, new AsyncResult<dtos.AuthenticateResponse>() {
            @Override
            public void success(dtos.AuthenticateResponse response) {
                mSharedPrefsUtils.setBooleanPreference(NetworkProvider.KEY_FIRST_TIME, true);
                mView.hideLoading();
                mView.showGettingServerInfo();
                mView.loginWithCredentials(mNetworkProvider.getCloudClient(phoneNumber).getCredentials());
            }

            @Override
            public void error(Exception ex) {
                Crashlytics.setString("PerformResetPassword", mNetworkProvider.getUserName(phoneNumber));
                Crashlytics.logException(ex);
                mView.hideLoading();
                mView.showConfirmationCodeInvalid();
            }
        });
    }

    private void showErrorMessage(ErrorBundle errorBundle) {
        String errorMessage = ErrorMessageFactory.create(this.mView.getContext(),
                errorBundle.getException());
        this.mView.showError(errorMessage);
    }

}
