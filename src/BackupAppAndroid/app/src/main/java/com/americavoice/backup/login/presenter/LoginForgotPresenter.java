
package com.americavoice.backup.login.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.americavoice.backup.Const;
import com.americavoice.backup.R;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.login.model.SpinnerItem;
import com.americavoice.backup.login.ui.LoginForgotView;
import com.americavoice.backup.login.ui.LoginRegisterView;
import com.americavoice.backup.main.data.SharedPrefsUtils;
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
public class LoginForgotPresenter extends BasePresenter implements IPresenter {

    private LoginForgotView mView;

    @Inject
    public LoginForgotPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull LoginForgotView view) {
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
                        items.add(new SpinnerItem(country.getPhoneCode(), "+" + country.getPhoneCode()));
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

    public void submit(final String countryCode, final String phoneNumber) {
        boolean hasError = false;

        if (TextUtils.isEmpty(countryCode)) {
            hasError = true;
            mView.showCountryCodeRequired();
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            hasError = true;
            mView.showPhoneNumberRequired();
        }
        if (hasError) return;

        mView.showLoading();
        dtos.SendPasswordResetCode request = new dtos.SendPasswordResetCode();
        request.setCompanyId(Const.COMPANY_ID);
        request.setCountryCallingCode(countryCode.trim());
        request.setPhoneNumber(phoneNumber.trim());

        mNetworkProvider.SendPasswordResetCode(request, new AsyncResult<dtos.SendPasswordResetCodeResponse>() {
            @Override
            public void success(dtos.SendPasswordResetCodeResponse response) {
                mView.hideLoading();
                mView.viewLoginNewPassword(countryCode, phoneNumber);
            }

            @Override
            public void error(Exception ex) {
                Crashlytics.logException(ex);
                if (ex instanceof WebServiceException) {
                    WebServiceException webEx = (WebServiceException) ex;
                    if (webEx.getErrorCode() != null && (webEx.getErrorCode().equals("UserNotFound") || webEx.getErrorCode().equals("UserNotRegistered"))) {
                        mView.hideLoading();
                        mView.showPhoneNumberInvalid();
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
