
package com.americavoice.backup.login.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AuthenticatorAsyncTask;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.login.presenter.LoginPresenter;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.ui.BaseAuthenticatorFragment;
import com.americavoice.backup.main.ui.activity.LoginActivity;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Fragment that shows details of a certain political party.
 */
public class LoginFragment extends BaseAuthenticatorFragment implements LoginView, AuthenticatorAsyncTask.OnAuthenticatorTaskListener {

    /**
     * Interface for listening submit button.
     */
    public interface Listener {
        void viewHome();
        void viewValidation();
        void onBackLoginClicked();
    }


    @Inject
    LoginPresenter mPresenter;
    private Listener mListener;
    private Unbinder mUnBind;
    @BindView(R.id.et_phone_number)
    public EditText etPhoneNumber;


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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_login, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);
        etPhoneNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE) {
                    mPresenter.submit("502", etPhoneNumber.getText().toString());
                }
                return false;
            }
        });
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
        showDialog(getString(R.string.common_getting_server_info));
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
    public void viewHome() {
        if (mListener != null) mListener.viewHome();
    }

    @Override
    public void viewValidation() {
        if (mListener != null) mListener.viewValidation();
    }

    @Override
    public void showPhoneNumberRequired() {
        etPhoneNumber.requestFocus();
        etPhoneNumber.setError(getString(R.string.login_validationPhoneNumberRequired));
    }

    @Override
    public void showPhoneNumberInvalid() {
        etPhoneNumber.requestFocus();
        etPhoneNumber.setError(getString(R.string.login_validationPhoneNumberInvalid));
    }

    @Override
    public void onAuthenticatorTaskCallback(RemoteOperationResult result) {
        hideGettingServerInfo();
        if (result.isSuccess()) {
            Log_OC.d(TAG, "Successful access - time to save the account");

            boolean success = false;

            if (mAction == LoginActivity.ACTION_CREATE) {
                success = createAccount(result, mPresenter.getUsername(), mPresenter.getDeviceId());

            } else {
                try {
                    updateAccountAuthentication(mPresenter.getDeviceId());
                    success = true;

                } catch (com.owncloud.android.lib.common.accounts.AccountUtils.AccountNotFoundException e) {
                    Log_OC.e(TAG, "Account " + mAccount + " was removed!", e);
                    showToastMessage(getContext().getString(R.string.auth_account_does_not_exist));
                    getActivity().finish();
                }
            }

            if (success) {
                getActivity().finish();
            } else {
                showToastMessage("Couldn't create the account, please try again");
            }

        } else if (result.isServerFail() || result.isException()) {
            showToastMessage(result.getLogMessage());

        } else {    // authorization fail due to client side - probably wrong credentials
            showToastMessage("Check credentials, please try again");
        }
    }

    @Override
    public void loginWithCredentials(OwnCloudCredentials credentials) {
        AuthenticatorAsyncTask loginAsyncTask = new AuthenticatorAsyncTask(this);
        Object[] params = {NetworkProvider.getBaseUrlOwnCloud(), credentials};
        loginAsyncTask.execute(params);
    }

}

