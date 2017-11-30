
package com.americavoice.backup.login.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.americavoice.backup.R;
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
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;

import net.servicestack.client.AsyncResult;
import net.servicestack.client.WebServiceException;

import javax.inject.Inject;


/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class LoginConfirmationPresenter extends BasePresenter implements IPresenter {
    private String mUsername;
    private String mPassword;

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
    public void initialize(String username, String password) {
        mUsername = username;
        mPassword = password;
    }

    public void submit(final String code) {

        boolean hasError = false;
        if (TextUtils.isEmpty(code)) {
            hasError = true;
            mView.showConfirmationCodeInvalid();
        }
        if (hasError) return;

        mView.showLoading();
        dtos.ValidatePhoneVerificationCode request = new dtos.ValidatePhoneVerificationCode();
        request.setVerificationCode(code.trim());
        mNetworkProvider.ValidatePhoneVerificationCode(request, new AsyncResult<dtos.ValidatePhoneVerificationCodeResponse>() {
            @Override
            public void success(dtos.ValidatePhoneVerificationCodeResponse response) {
                mView.hideLoading();
                mView.showGettingServerInfo();
                int lastIndex = mUsername.indexOf("@");
                if (lastIndex == -1) {
                    lastIndex = mUsername.length();
                }
                final String user = mUsername.substring(0, lastIndex);
                mView.loginWithCredentials(OwnCloudCredentialsFactory.newBasicCredentials(user, mPassword));
                mSharedPrefsUtils.setBooleanPreference(NetworkProvider.KEY_FIRST_TIME, true);
            }

            @Override
            public void error(Exception ex) {
                mView.hideLoading();
                if (ex instanceof WebServiceException) {
                    WebServiceException wex = (WebServiceException) ex;
                    if (wex.getErrorCode() != null && wex.getErrorCode().equals("CodeExpired")) {
                        mView.showConfirmationCodeExpired();
                    } else if (wex.getErrorCode() != null && wex.getErrorCode().equals("InvalidCode")) {
                        mView.showConfirmationCodeInvalid();
                    } else {
                        Crashlytics.logException(ex);
                        mView.showError(mView.getContext().getString(R.string.exception_message_generic));
                    }
                } else {
                    Crashlytics.logException(ex);
                    mView.showError(mView.getContext().getString(R.string.exception_message_generic));
                }
            }
        });
    }

    public void sendCode() {
        mView.showLoading();
        mNetworkProvider.SendPhoneVerificationCode(new AsyncResult<dtos.SendPhoneVerificationCodeResponse>() {
            @Override
            public void success(dtos.SendPhoneVerificationCodeResponse response) {
                mView.hideLoading();
            }

            @Override
            public void error(Exception ex) {
                Crashlytics.logException(ex);
                mView.hideLoading();
                if (mView.getContext() != null) {
                    mView.showError(mView.getContext().getString(R.string.exception_message_generic));
                }
            }
        });
    }

    private void showErrorMessage(ErrorBundle errorBundle) {
        String errorMessage = ErrorMessageFactory.create(this.mView.getContext(),
                errorBundle.getException());
        this.mView.showError(errorMessage);
    }

}
