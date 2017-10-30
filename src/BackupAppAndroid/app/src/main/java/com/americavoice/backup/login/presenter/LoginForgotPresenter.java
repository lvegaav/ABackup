
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
        List<SpinnerItem> items = new ArrayList<>();
        items.add(new SpinnerItem("1","+1"));
        items.add(new SpinnerItem("502","+502"));
        items.add(new SpinnerItem("503","+503"));
        mView.populateCountries(items);
    }

    public void submit(final String countryCode, final String phoneNumber) {
        boolean hasError = false;
        if (TextUtils.isEmpty(phoneNumber)) {
            hasError = true;
            mView.showPhoneNumberRequired();
        }
        if (hasError) return;

        mView.showLoading();
        dtos.SendPasswordResetCode request = new dtos.SendPasswordResetCode();
        request.setCompanyId(Const.COMPANY_ID);
        request.setCountryCallingCode(countryCode);
        request.setPhoneNumber(phoneNumber);

        mNetworkProvider.SendPasswordResetCode(request, new AsyncResult<dtos.SendPasswordResetCodeResponse>() {
            @Override
            public void success(dtos.SendPasswordResetCodeResponse response) {
                mView.hideLoading();
                mView.viewLoginNewPassword(countryCode, phoneNumber);
            }

            @Override
            public void error(Exception ex) {
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
