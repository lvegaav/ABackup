
package com.americavoice.backup.login.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.americavoice.backup.Const;
import com.americavoice.backup.R;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.login.model.SpinnerItem;
import com.americavoice.backup.login.ui.LoginRegisterView;
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
public class LoginRegisterPresenter extends BasePresenter implements IPresenter {

    private LoginRegisterView mView;

    @Inject
    public LoginRegisterPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull LoginRegisterView view) {
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
        List<SpinnerItem> items = new ArrayList<>();
        items.add(new SpinnerItem("1","+1"));
        items.add(new SpinnerItem("502","+502"));
        items.add(new SpinnerItem("503","+503"));
        mView.populateCountries(items);
    }

    public void submit(final String countryCode, final String phoneNumber, final String username, final String newPassword, final String confirmPassword) {
        boolean hasError = false;
        if (TextUtils.isEmpty(phoneNumber)) {
            hasError = true;
            mView.showPhoneNumberRequired();
        }

        if (TextUtils.isEmpty(username)) {
            hasError = true;
            mView.showUsernameRequired();
        }

        if (TextUtils.isEmpty(newPassword)) {
            hasError = true;
            mView.showNewPasswordRequired();
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            hasError = true;
            mView.showConfirmPasswordRequired();
        }

        if (!newPassword.equals(confirmPassword))
        {
            hasError = true;
            mView.showConfirmPasswordInvalid();
        }

        if (hasError) return;

        mView.showLoading();
        dtos.CustomRegister registerRequest = new dtos.CustomRegister();
        registerRequest.setCompanyId(Const.COMPANY_ID);
        registerRequest.setCountryCallingCode(countryCode);
        registerRequest.setPhoneNumber(phoneNumber);
        registerRequest.setPassword(newPassword);
        registerRequest.setUsername(username);

        mNetworkProvider.CustomRegister(registerRequest, new AsyncResult<dtos.CustomRegisterResponse>() {
            @Override
            public void success(dtos.CustomRegisterResponse response) {
                //Do Login
                mNetworkProvider.login(username, newPassword, new AsyncResult<dtos.AuthenticateResponse>() {
                    @Override
                    public void success(dtos.AuthenticateResponse response) {
                        mView.hideLoading();
                        mView.viewValidation(username, newPassword);
                    }

                    @Override
                    public void error(Exception ex) {
                        mView.hideLoading();
                        mView.showError(mView.getContext().getString(R.string.exception_message_generic));
                    }
                });
            }

            @Override
            public void error(Exception ex) {
                if (ex instanceof WebServiceException) {
                    WebServiceException webEx = (WebServiceException) ex;
                    if (webEx.getErrorCode() != null && (webEx.getErrorCode().equals("InvalidPhoneNumber"))) {
                        mView.hideLoading();
                        mView.showPhoneNumberInvalid();
                        return;
                    }

                    if (webEx.getErrorCode() != null && (webEx.getErrorCode().equals("InvalidUsername"))) {
                        mView.hideLoading();
                        mView.showUsernameInvalid();
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
}
