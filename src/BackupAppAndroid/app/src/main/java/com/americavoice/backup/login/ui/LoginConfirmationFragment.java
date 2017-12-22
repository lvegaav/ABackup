
package com.americavoice.backup.login.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatDrawableManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.americavoice.backup.AndroidApplication;
import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AuthenticatorAsyncTask;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.login.presenter.LoginConfirmationPresenter;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.ui.BaseAuthenticatorFragment;
import com.americavoice.backup.main.ui.activity.LoginActivity;
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
public class LoginConfirmationFragment extends BaseAuthenticatorFragment
        implements LoginConfirmationView, AuthenticatorAsyncTask.OnAuthenticatorTaskListener {

    public static final String ARGUMENT_USERNAME = "com.americavoice.backup.ARGUMENT_USERNAME";
    public static final String ARGUMENT_DEVICE = "com.americavoice.backup.ARGUMENT_DEVICE";

    @Override
    public void viewHome() {
        if (mListener != null) mListener.viewHome();
    }

    @Override
    public void showConfirmationCodeExpired() {
        etConfirmationCode.requestFocus();
        etConfirmationCode.setError(getString(R.string.confirmation_verification_code_expired));
    }

    @Override
    public void showConfirmationCodeInvalid() {
        etConfirmationCode.requestFocus();
        etConfirmationCode.setError(getString(R.string.confirmation_verification_code_invalid));
    }
    /**
     * Interface for listening submit button.
     */
    public interface Listener {
        void viewHome();
        void onBackConfirmationClicked();
    }


    @Inject
    LoginConfirmationPresenter mPresenter;
    private Listener mListener;
    private Unbinder mUnBind;
    @BindView(R.id.et_confirmation_code)
    public EditText etConfirmationCode;


    public LoginConfirmationFragment() {
        super();
    }

    public static LoginConfirmationFragment newInstance(String username, String device) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_USERNAME, username);
        bundle.putString(ARGUMENT_DEVICE, device);

        LoginConfirmationFragment fragment = new LoginConfirmationFragment();
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

        View fragmentView = inflater.inflate(R.layout.fragment_login_confirmation, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            etConfirmationCode.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_glove, 0, 0, 0);
        } else {
            Drawable globe = AppCompatDrawableManager.get().getDrawable(getContext(), R.drawable.ic_wand);
            etConfirmationCode.setCompoundDrawablesWithIntrinsicBounds(globe, null, null, null);
        }
        etConfirmationCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    mPresenter.submit(etConfirmationCode.getText().toString());
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
        this.mPresenter.initialize(
                getArguments().getString(ARGUMENT_USERNAME),
                getArguments().getString(ARGUMENT_DEVICE)
        );
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
        if (this.mListener != null) this.mListener.onBackConfirmationClicked();
    }

    @Override
    public void onAuthenticatorTaskCallback(RemoteOperationResult result) {
        hideGettingServerInfo();
        if (result.isSuccess()) {
            Log_OC.d(TAG, "Successful access - time to save the account");

            boolean success = false;

            if (mAction == LoginActivity.ACTION_CREATE) {
                AndroidApplication application = (AndroidApplication) getActivity().getApplication();
                success = createAccount(result, application.getSerialB1(), application.getSerialB2(),
                        getArguments().getString(ARGUMENT_USERNAME), getArguments().getString(ARGUMENT_DEVICE));


            } else {
                try {
                    updateAccountAuthentication(getArguments().getString(ARGUMENT_DEVICE));
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
                Crashlytics.logException(new Throwable("Couldn't create the account, please try again"));
                showErrorMessage(getString(R.string.exception_message_generic));
            }

        } else if (result.isServerFail() || result.isException()) {
            Crashlytics.logException(new Throwable(result.getLogMessage()));
            showErrorMessage(getString(R.string.exception_message_generic));

        } else {    // authorization fail due to client side - probably wrong credentials
            showErrorMessage(getString(R.string.exception_message_generic));
        }
    }

    public void showErrorMessage(String message) {
        new AlertDialog.Builder(getActivity(), R.style.WhiteDialog)
                .setTitle(R.string.app_name)
                .setMessage(message)
                .setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mListener != null) {
                            mListener.onBackConfirmationClicked();
                        }
                    }
                })
                .show();
    }

    @Override
    public void loginWithCredentials() {
        AndroidApplication application = (AndroidApplication) getActivity().getApplication();
        OwnCloudCredentials credentials = OwnCloudCredentialsFactory.newBasicCredentials(application.getSerialB1(), application.getSerialB2());
        AuthenticatorAsyncTask loginAsyncTask = new AuthenticatorAsyncTask(this);
        Object[] params = {getResources().getString(R.string.baseUrlOwnCloud), credentials};
        loginAsyncTask.execute(params);
    }

    @Override
    public void saveSerials(String serialB1, String serialB2) {
        AndroidApplication application = (AndroidApplication) getActivity().getApplication();
        application.setSerialB1(serialB1);
        application.setSerialB2(serialB2);
    }

    @OnClick(R.id.tv_resend)
    public void resendCode() {
        mPresenter.sendCode();
    }

    @OnClick(R.id.btn_send)
    public void sendConfirmation() {
        mPresenter.submit(etConfirmationCode.getText().toString());
    }
}

