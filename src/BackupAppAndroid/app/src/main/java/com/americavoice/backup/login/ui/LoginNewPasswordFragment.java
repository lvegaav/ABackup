
package com.americavoice.backup.login.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AuthenticatorAsyncTask;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.login.model.SpinnerItem;
import com.americavoice.backup.login.presenter.LoginNewPasswordPresenter;
import com.americavoice.backup.login.presenter.LoginRegisterPresenter;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.ui.BaseAuthenticatorFragment;
import com.americavoice.backup.main.ui.activity.LoginActivity;
import com.americavoice.backup.utils.FirebaseUtils;
import com.crashlytics.android.Crashlytics;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Fragment that shows details of a certain political party.
 */
public class LoginNewPasswordFragment extends BaseAuthenticatorFragment implements LoginNewPasswordView {

    public static final String ARGUMENT_COUNTRY_CODE = "com.americavoice.backup.ARGUMENT_COUNTRY_CODE";
    public static final String ARGUMENT_PHONE_NUMBER = "com.americavoice.backup.ARGUMENT_PHONE_NUMBER";


    /**
     * Interface for listening submit button.
     */
    public interface Listener {
        void viewNewPasswordSuccess();
        void onBackLoginNewPasswordClicked();
    }


    @Inject
    LoginNewPasswordPresenter mPresenter;
    private Listener mListener;
    private Unbinder mUnBind;
    @BindView(R.id.et_password)
    public EditText etPassword;
    @BindView(R.id.et_confirm_password)
    public EditText etConfirmPassword;
    @BindView(R.id.et_confirmation_code)
    public EditText etConfirmationCode;

    public LoginNewPasswordFragment() {
        super();
    }

    public static LoginNewPasswordFragment newInstance(String countryCode, String phoneNumber) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_COUNTRY_CODE, countryCode);
        bundle.putString(ARGUMENT_PHONE_NUMBER, phoneNumber);

        LoginNewPasswordFragment fragment = new LoginNewPasswordFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            this.mListener = (Listener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_login_new_password, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.initialize(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mPresenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mPresenter.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBind.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mPresenter.destroy();
    }


    private void initialize(Bundle savedInstanceState) {
        this.getComponent(AppComponent.class).inject(this);
        this.mPresenter.setView(this);
        this.mPresenter.initialize(getArguments().getString(ARGUMENT_COUNTRY_CODE),getArguments().getString(ARGUMENT_PHONE_NUMBER));

        super.initialize();

        if (savedInstanceState != null) {
            //TODO:Init Values
        }

    }

    @Override
    public void showLoading() {
        showDialog(getString(R.string.common_loading));
    }

    @Override
    public void hideLoading() {
        hideDialog();
    }

    @Override
    public void showRetry() {
        showDialog(getString(R.string.common_sending));
    }

    @Override
    public void hideRetry() {
        hideDialog();
    }

    @Override
    public void showError(String message) {
        this.showDialogMessage(message);
    }


    @Override
    public Context getContext() {
        return getActivity().getApplicationContext();
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        if (this.mListener != null) this.mListener.onBackLoginNewPasswordClicked();
    }

    @Override
    public void viewNewPasswordSuccess() {
        if (this.mListener != null) this.mListener.viewNewPasswordSuccess();
    }

    @Override
    public void showVerificationCodeRequired() {
        etConfirmationCode.requestFocus();
        etConfirmationCode.setError(getString(R.string.login_validationVerificationCodeInvalid));
    }

    @Override
    public void showNewPasswordRequired() {
        etPassword.requestFocus();
        etPassword.setError(getString(R.string.login_validationNewPasswordInvalid));
    }

    @Override
    public void showConfirmPasswordRequired() {
        etConfirmPassword.requestFocus();
        etConfirmPassword.setError(getString(R.string.login_validationConfirmPasswordInvalid));
    }

    @Override
    public void showConfirmPasswordInvalid() {
        etConfirmPassword.requestFocus();
        etConfirmPassword.setError(getString(R.string.login_validationConfirmPasswordInvalid));
    }

   @OnClick(R.id.btn_register)
    public void onRegister(View v)
   {
       mPresenter.submit(
               etConfirmationCode.getText().toString(),
               etPassword.getText().toString(),
               etConfirmPassword.getText().toString());
   }
}

