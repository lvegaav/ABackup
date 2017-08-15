
package com.americavoice.backup.login.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.login.ui.LoginView;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.exception.ErrorBundle;
import com.americavoice.backup.main.exception.ErrorMessageFactory;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;

import net.servicestack.client.AsyncResult;

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

    public void submit(final String phoneNumber) {
        boolean hasError = false;
        if (TextUtils.isEmpty(phoneNumber)) {
            hasError = true;
            mView.showPhoneNumberRequired();
        }
        if (hasError) return;

        mNetworkProvider.login(phoneNumber, new AsyncResult<dtos.AuthenticateResponse>() {
            @Override
            public void success(dtos.AuthenticateResponse response) {
                mSharedPrefsUtils.setStringPreference(NetworkProvider.KEY_PHONE_NUMBER, phoneNumber);
                mView.loginWithCredentials(mNetworkProvider.getCloudClient(phoneNumber).getCredentials());
            }

            @Override
            public void error(Exception ex) {
                dtos.SendResetPasswordSms request = new dtos.SendResetPasswordSms();
                request.setPhoneNumber(mNetworkProvider.getUserName(phoneNumber));

                mNetworkProvider.SendResetPasswordSms(request, new AsyncResult<dtos.SendResetPasswordSmsResponse>() {
                    @Override
                    public void success(dtos.SendResetPasswordSmsResponse response) {
                        mSharedPrefsUtils.setStringPreference(NetworkProvider.KEY_PHONE_NUMBER, phoneNumber);
                        mView.viewValidation();
                    }

                    @Override
                    public void error(Exception ex) {
                        mView.showPhoneNumberInvalid();
                    }
                });
            }
        });


    }

    private void showErrorMessage(ErrorBundle errorBundle) {
        String errorMessage = ErrorMessageFactory.create(this.mView.getContext(),
                errorBundle.getException());
        this.mView.showError(errorMessage);
    }

    public String getDeviceId() {
        return mNetworkProvider.getDeviceId();
    }

    public String getUsername() {
        return NetworkProvider.COMPANY_ID + "_" + getPhoneNumber();
    }

    public void setDefaultAccountName(String accountName) {
        mSharedPrefsUtils.setStringPreference("select_oc_account", accountName);
    }

}
