
package com.americavoice.backup.login.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.americavoice.backup.Const;
import com.americavoice.backup.R;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.login.model.SpinnerItem;
import com.americavoice.backup.login.ui.LoginNewPasswordView;
import com.americavoice.backup.login.ui.LoginRegisterView;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;

import net.servicestack.client.AsyncResult;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class LoginNewPasswordPresenter extends BasePresenter implements IPresenter {
    private  String mCountryCode;
    private  String mPhoneNumber;
    private LoginNewPasswordView mView;

    @Inject
    public LoginNewPasswordPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull LoginNewPasswordView view) {
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
    public void initialize(String countryCode, String phoneNumber) {
        mCountryCode = countryCode;
        mPhoneNumber = phoneNumber;
    }

    public void submit(final String verificationCode, final String newPassword, final String confirmPassword) {
        boolean hasError = false;
        if (TextUtils.isEmpty(verificationCode)) {
            hasError = true;
            mView.showVerificationCodeRequired();
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
        dtos.PerformResetPassword request = new dtos.PerformResetPassword();
        request.setCompanyId(Const.COMPANY_ID);
        request.setCountryCallingCode(mCountryCode);
        request.setPhoneNumber(mPhoneNumber);
        request.setNewPassword(newPassword);
        request.setResetPasswordCode(verificationCode);

        mNetworkProvider.PerformResetPassword(request, new AsyncResult<dtos.PerformResetPasswordResponse>() {
            @Override
            public void success(dtos.PerformResetPasswordResponse response) {
                mView.hideLoading();
                mView.viewNewPasswordSuccess();
            }

            @Override
            public void error(Exception ex) {
                mView.hideLoading();
                mView.showVerificationCodeRequired();
            }
        });
    }
}
