
package com.americavoice.backup.login.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.americavoice.backup.Const;
import com.americavoice.backup.R;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.login.model.SpinnerItem;
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
import net.servicestack.client.WebServiceException;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;



/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class LoginPresenter extends BasePresenter implements IPresenter {

    private LoginView mView;

    @Inject
    public LoginPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull LoginView view) {
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

    public void submit(final String username, final String password) {
        boolean hasError = false;
        if (TextUtils.isEmpty(username)) {
            hasError = true;
            mView.showUsernameRequired();
        }

        if (TextUtils.isEmpty(password)) {
            hasError = true;
            mView.showPasswordRequired();
        }
        if (hasError) return;
        mView.showLoading();
        mNetworkProvider.login(username, password, new AsyncResult<dtos.AuthenticateResponse>() {
            @Override
            public void success(dtos.AuthenticateResponse response) {
                //Try To get Full User Information
                mNetworkProvider.getUser(new AsyncResult<dtos.GetFullUserResponse>() {
                    @Override
                    public void success(dtos.GetFullUserResponse response) {
                        mView.hideLoading();
                        mView.showGettingServerInfo();
                        mView.loginWithCredentials(mNetworkProvider.getLoginCloudClient(username, password).getCredentials());
                        mSharedPrefsUtils.setBooleanPreference(NetworkProvider.KEY_FIRST_TIME, true);
                    }

                    @Override
                    public void error(Exception ex) {
                        //Send Verification Code
                        mNetworkProvider.SendPhoneVerificationCode(new AsyncResult<dtos.SendPhoneVerificationCodeResponse>() {
                            @Override
                            public void success(dtos.SendPhoneVerificationCodeResponse response) {
                                mView.hideLoading();
                                mView.viewValidation(username, password);
                            }

                            @Override
                            public void error(Exception ex) {
                                mView.hideLoading();
                                if (mView.getContext() != null) {
                                    mView.showError(mView.getContext().getString(R.string.exception_message_generic));
                                }
                            }
                        });

                    }
                });
            }

            @Override
            public void error(Exception ex) {
                if (ex instanceof WebServiceException) {
                    WebServiceException webEx = (WebServiceException) ex;
                    if (webEx.getErrorCode() != null && webEx.getErrorCode().equals("Unauthorized")) {
                        mView.hideLoading();
                        mView.showError(mView.getContext().getString(R.string.login_validationFailed));
                        return;
                    }
                }

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
