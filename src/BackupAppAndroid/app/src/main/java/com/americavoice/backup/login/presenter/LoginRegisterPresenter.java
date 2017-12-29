
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
import com.crashlytics.android.core.CrashlyticsCore;

import net.servicestack.client.AsyncResult;
import net.servicestack.client.WebServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
        initCountries();
    }

    private void initCountries() {
        mView.showLoading();
        final List<SpinnerItem> items = new ArrayList<>();
        items.add(new SpinnerItem("", ""));
        mNetworkProvider.getCountries(new AsyncResult<dtos.GetCountriesResponse>() {
            @Override
            public void success(dtos.GetCountriesResponse response) {
                ArrayList<dtos.Country> countries = response.getCountries();
                if (countries != null) {
                    for (dtos.Country country : countries) {
                        items.add(new SpinnerItem(country.getPhoneCode(), country.getName()));
                    }
                }
                mView.populateCountries(items);
            }

            @Override
            public void error(Exception ex) {
                mView.showError(mView.getContext().getString(R.string.exception_message_generic));
                mView.populateCountries(items);
            }

            @Override
            public void complete() {
                mView.hideLoading();
            }
        });
    }

    public void submit(final String countryCode, final String phoneNumber, final String username, final String newPassword, final String confirmPassword) {
        boolean hasError = false;
        if (TextUtils.isEmpty(countryCode)) {
            hasError = true;
            mView.showCountryCodeRequired();
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            hasError = true;
            mView.showPhoneNumberRequired();
        } else if (phoneNumber.length() < 4 && phoneNumber.length() > 20) {
            hasError = true;
            mView.showPhoneNumberInvalidRange();
        }

        if (TextUtils.isEmpty(username)) {
            hasError = true;
            mView.showUsernameRequired();
        }

        if (TextUtils.isEmpty(newPassword)) {
            hasError = true;
            mView.showNewPasswordRequired();
        }

        if (!Pattern.matches("^(?=(.*\\d){1}).{8,50}$", newPassword)) {
            hasError = true;
            mView.showNewPasswordInvalid();
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            hasError = true;
            mView.showConfirmPasswordRequired();
        }

        if (!newPassword.equals(confirmPassword)) {
            hasError = true;
            mView.showConfirmPasswordInvalid();
        }

        if (hasError) return;

        mView.showLoading();
        dtos.CustomRegister registerRequest = new dtos.CustomRegister();
        registerRequest.setCompanyId(Const.COMPANY_ID);
        registerRequest.setCountryCallingCode(countryCode.trim());
        registerRequest.setPhoneNumber(phoneNumber.trim());
        registerRequest.setPassword(newPassword.trim());
        registerRequest.setUsername(username.trim());

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
                        if (ex instanceof WebServiceException) {
                            Crashlytics.logException(new RuntimeException(((WebServiceException) ex).getErrorMessage()));
                        } else {
                            Crashlytics.logException(ex);
                        }
                    }
                });
            }

            @Override
            public void error(Exception ex) {
                mView.hideLoading();
                if (ex instanceof WebServiceException) {
                    WebServiceException webEx = (WebServiceException) ex;
                    if (webEx.getErrorCode() != null && (webEx.getErrorCode().equals("InvalidPhoneNumber"))) {
                        mView.showPhoneNumberExists();
                        return;
                    }

                    if (webEx.getErrorCode() != null && (webEx.getErrorCode().equals("InvalidUsername"))) {
                        mView.showUsernameExists();
                        return;
                    }

                    if (webEx.getErrorCode() != null && (webEx.getErrorCode().equals("ArgumentException"))) {
                        mView.showUsernameInvalid();
                        return;
                    }
                    Crashlytics.logException(new RuntimeException(((WebServiceException) ex).getErrorMessage()));
                } else {
                    Crashlytics.logException(ex);
                }
                if (mView.getContext() != null) {
                    mView.showError(mView.getContext().getString(R.string.exception_message_generic));
                }
            }
        });
    }
}
