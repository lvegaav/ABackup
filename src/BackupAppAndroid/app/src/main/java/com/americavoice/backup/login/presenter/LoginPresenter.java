
package com.americavoice.backup.login.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.login.model.SpinnerItem;
import com.americavoice.backup.login.ui.LoginView;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.exception.DefaultErrorBundle;
import com.americavoice.backup.main.exception.ErrorBundle;
import com.americavoice.backup.main.exception.ErrorMessageFactory;
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
        List<SpinnerItem> items = new ArrayList<>();
        items.add(new SpinnerItem("1","+1"));
        items.add(new SpinnerItem("502","+502"));
        mView.populateCountries(items);
    }

    public void submit(final String countryCode, final String phoneNumber) {
        boolean hasError = false;
        if (TextUtils.isEmpty(phoneNumber)) {
            hasError = true;
            mView.showPhoneNumberRequired();
        }
        final String phoneNumberWithCode = countryCode + phoneNumber;
        if (hasError) return;
        mView.showLoading();
        mNetworkProvider.login(phoneNumberWithCode, new AsyncResult<dtos.AuthenticateResponse>() {
            @Override
            public void success(dtos.AuthenticateResponse response) {
                mView.hideLoading();
                mView.showGettingServerInfo();
                mSharedPrefsUtils.setStringPreference(NetworkProvider.KEY_PHONE_NUMBER, phoneNumberWithCode);
                mView.loginWithCredentials(mNetworkProvider.getCloudClient(phoneNumberWithCode).getCredentials());
            }

            @Override
            public void error(Exception ex) {
                if (ex instanceof WebServiceException){
                    WebServiceException webEx = (WebServiceException) ex;
                    if (webEx.getStatusCode() == 401) {
                        dtos.SendResetPasswordSms request = new dtos.SendResetPasswordSms();
                        request.setPhoneNumber(mNetworkProvider.getUserName(phoneNumberWithCode));
                        mNetworkProvider.SendResetPasswordSms(request, new AsyncResult<dtos.SendResetPasswordSmsResponse>() {
                            @Override
                            public void success(dtos.SendResetPasswordSmsResponse response) {
                                mSharedPrefsUtils.setStringPreference(NetworkProvider.KEY_PHONE_NUMBER, phoneNumberWithCode);
                                mView.viewValidation();
                            }
                            @Override
                            public void error(Exception ex) {
                                mView.showPhoneNumberInvalid();
                            }

                            @Override
                            public void complete() {
                                mView.hideLoading();
                            }
                        });
                    } else {
                        showErrorMessage(new DefaultErrorBundle(ex));
                    }
                } else {
                    mView.hideLoading();
                    showErrorMessage(new DefaultErrorBundle(ex));
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
