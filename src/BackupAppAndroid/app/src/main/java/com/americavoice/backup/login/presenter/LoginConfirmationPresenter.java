
package com.americavoice.backup.login.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.login.ui.LoginConfirmationView;
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
public class LoginConfirmationPresenter extends BasePresenter implements IPresenter {
    private String mUsername;
    private String mDevice;

    private LoginConfirmationView mView;

    @Inject
    LoginConfirmationPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull LoginConfirmationView view) {
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
    public void initialize(String username, String device) {
        mUsername = username;
        mDevice = device;
    }

    public void submit(final String code) {

        boolean hasError = false;
        if (TextUtils.isEmpty(code)) {
            hasError = true;
            mView.showConfirmationCodeInvalid();
        }
        if (hasError) return;


        dtos.ValidatePhoneVerificationCode request = new dtos.ValidatePhoneVerificationCode();
        request.setVerificationCode(code);
        mNetworkProvider.ValidatePhoneVerificationCode(request, new AsyncResult<dtos.ValidatePhoneVerificationCodeResponse>() {
            @Override
            public void success(dtos.ValidatePhoneVerificationCodeResponse response) {
                mView.hideLoading();
                mView.showGettingServerInfo();
                mView.loginWithCredentials(mNetworkProvider.getLoginCloudClient(mUsername, mDevice).getCredentials());
                mSharedPrefsUtils.setBooleanPreference(NetworkProvider.KEY_FIRST_TIME, true);
            }

            @Override
            public void error(Exception ex) {
                Crashlytics.setString("PerformResetPassword", mUsername);
                Crashlytics.logException(ex);
                mView.hideLoading();
                mView.showConfirmationCodeInvalid();
            }
        });

        /*dtos.PerformResetPassword request = new dtos.PerformResetPassword();
        request.setPhoneNumber(mNetworkProvider.getUserName(phoneNumber));
        request.setNewPassword(mNetworkProvider.getDeviceId());
        request.setResetPasswordCode(code);
        mView.showLoading();
        mNetworkProvider.PerformResetPassword(request, new AsyncResult<dtos.PerformResetPasswordResponse>() {
            @Override
            public void success(dtos.PerformResetPasswordResponse response) {
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
        });*/
    }

    private void showErrorMessage(ErrorBundle errorBundle) {
        String errorMessage = ErrorMessageFactory.create(this.mView.getContext(),
                errorBundle.getException());
        this.mView.showError(errorMessage);
    }

}
