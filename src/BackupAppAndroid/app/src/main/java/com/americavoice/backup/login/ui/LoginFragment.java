
package com.americavoice.backup.login.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.americavoice.backup.AndroidApplication;
import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AuthenticatorAsyncTask;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.login.presenter.LoginPresenter;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseAuthenticatorFragment;
import com.americavoice.backup.main.ui.activity.LoginActivity;
import com.americavoice.backup.utils.ConnectivityUtils;
import com.americavoice.backup.utils.FirebaseUtils;
import com.crashlytics.android.Crashlytics;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Fragment that shows details of a certain political party.
 */
public class LoginFragment extends BaseAuthenticatorFragment implements LoginView, AuthenticatorAsyncTask.OnAuthenticatorTaskListener {

    /**
     * Interface for listening submit button.
     */
    public interface Listener {
        void viewValidation(String username, String device);

        void viewRegister();

        void viewForgot();

        void onBackLoginClicked();
    }


    @Inject
    LoginPresenter mPresenter;
    private Listener mListener;
    private Unbinder mUnBind;
    @BindView(R.id.et_username)
    public EditText etUsername;
    @BindView(R.id.et_password)
    public EditText etPassword;


    public LoginFragment() {
        super();
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
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

        View fragmentView = inflater.inflate(R.layout.fragment_login, container, false);
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
        this.mPresenter.initialize();

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
    public void showGettingServerInfo() {
        if (getActivity() != null) {
            showDialog(getString(R.string.common_getting_server_info));
        }
    }

    @Override
    public void hideGettingServerInfo() {
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
        if (this.mListener != null) this.mListener.onBackLoginClicked();
    }

    @Override
    public void viewValidation(String username, String device) {
        if (mListener != null) mListener.viewValidation(username, device);
    }

    @Override
    public void showUsernameRequired() {
        etUsername.requestFocus();
        etUsername.setError(getString(R.string.login_validationUsernameRequired));
    }

    @Override
    public void showPasswordRequired() {
        etPassword.requestFocus();
        etPassword.setError(getString(R.string.login_validationPasswordRequired));
    }

    @Override
    public void onAuthenticatorTaskCallback(RemoteOperationResult result) {
        if (getActivity() != null) {
            hideGettingServerInfo();
            if (result.isSuccess()) {
                Log_OC.d(TAG, "Successful access - time to save the account");

                boolean success = false;

                if (mAction == LoginActivity.ACTION_CREATE) {
                    AndroidApplication application = (AndroidApplication) getActivity().getApplication();
                    success = createAccount(result, application.getSerialB1(), application.getSerialB2(),
                      etUsername.getText().toString(), etPassword.getText().toString());

                } else {
                    try {
                        updateAccountAuthentication(etPassword.getText().toString());
                        success = true;

                    } catch (com.owncloud.android.lib.common.accounts.AccountUtils.AccountNotFoundException e) {
                        Log_OC.e(TAG, "Account " + mAccount + " was removed!", e);
                        showToastMessage(getContext().getString(R.string.auth_account_does_not_exist));
                        getActivity().finish();
                    }
                }

                if (success) {
                    FirebaseUtils.createLoginEvent(mFirebaseAnalytics,
                      FirebaseUtils.LOGIN_METHOD_PHONE_NUMBER);
                    getActivity().finish();
                } else {
                    showToastMessage(getString(R.string.common_account_error));
                }

            } else if (result.isServerFail() || result.isException()) {
                Log_OC.e(TAG, "Something went wrong with the server: " + result.getLogMessage());
                Crashlytics.logException(result.getException());
                showToastMessage(getString(R.string.exception_message_generic));

            } else {    // authorization fail due to client side - probably wrong credentials
                showToastMessage(getString(R.string.common_wrong_credentials));
            }
        }
    }

    @Override
    public void loginWithCredentials() {
        if (getActivity() != null) {
            AndroidApplication application = (AndroidApplication) getActivity().getApplication();
            OwnCloudCredentials credentials = OwnCloudCredentialsFactory.newBasicCredentials(application.getSerialB1(), application.getSerialB2());
            AuthenticatorAsyncTask loginAsyncTask = new AuthenticatorAsyncTask(this);
            Object[] params = {getResources().getString(R.string.baseUrlOwnCloud), credentials};
            loginAsyncTask.execute(params);
        }
    }

    @OnClick(R.id.btn_login)
    public void Login(View v) {
        if (ConnectivityUtils.isAppConnected(getContext()))
            mPresenter.submit(
              etUsername.getText().toString(),
              etPassword.getText().toString());
        else
            showToastMessage(getString(R.string.common_connectivity_error));
    }

    @OnClick(R.id.btn_register)
    public void Register(View v) {
        if (this.mListener != null) this.mListener.viewRegister();
    }

    @OnClick(R.id.tv_forgot)
    public void Forgot(View v) {
        if (this.mListener != null) this.mListener.viewForgot();
    }

    @Override
    public void saveSerials(String serialB1, String serialB2) {
        if (getActivity() != null) {
            AndroidApplication application = (AndroidApplication) getActivity().getApplication();
            application.setSerialB1(serialB1);
            application.setSerialB2(serialB2);
        }
    }
}

