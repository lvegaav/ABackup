
package com.americavoice.backup.login.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountAuthenticatorActivity;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.authentication.AuthenticatorAsyncTask;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.login.presenter.LoginPresenter;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.main.ui.activity.LoginActivity;
import com.americavoice.backup.operations.GetServerInfoOperation;
import com.americavoice.backup.utils.BaseConstants;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.UserInfo;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment that shows details of a certain political party.
 */
public class LoginFragment extends BaseFragment implements LoginView, AuthenticatorAsyncTask.OnAuthenticatorTaskListener {

    private static final String TAG = LoginFragment.class.getSimpleName();

    private byte mAction;
    private Account mAccount;
    private AccountManager mAccountMgr;
    private GetServerInfoOperation.ServerInfo mServerInfo = new GetServerInfoOperation.ServerInfo();

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
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    mPresenter.submit(etPhoneNumber.getText().toString());
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

        mAccountMgr = AccountManager.get(getContext());
        /// get input values
        mAction = getActivity().getIntent().getByteExtra(BaseConstants.EXTRA_ACTION, LoginActivity.ACTION_CREATE);
        mAccount = getActivity().getIntent().getExtras().getParcelable(BaseConstants.EXTRA_ACCOUNT);
        if (savedInstanceState != null) {
            //TODO:Init Values
        }
        mServerInfo.mBaseUrl = NetworkProvider.getBaseUrlOwnCloud();
    }

    @Override
    public void showLoading() {
        if (mProgress != null) {
            mProgress.hide();
            mProgress.dismiss();
            mProgress = null;
        }
        mProgress = ProgressDialog.show(getActivity(),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.common_loading),
                true,
                false);
    }

    @Override
    public void hideLoading() {
        if (mProgress != null) {
            mProgress.hide();
            mProgress.dismiss();
            mProgress = null;
        }
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

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


    /**
     * Processes the result of the access check performed to try the user credentials.
     *
     * Creates a new account through the AccountManager.
     *
     * @param result Result of the operation.
     */
    @Override
    public void onAuthenticatorTaskCallback(RemoteOperationResult result) {

        if (result.isSuccess()) {
            Log_OC.d(TAG, "Successful access - time to save the account");

            boolean success = false;

            if (mAction == LoginActivity.ACTION_CREATE) {
                if (mPresenter != null)
                    success = createAccount(result);

            } else {
                try {
                    updateAccountAuthentication();
                    success = true;

                } catch (com.owncloud.android.lib.common.accounts.AccountUtils.AccountNotFoundException e) {
                    Log_OC.e(TAG, "Account " + mAccount + " was removed!", e);
                    showToastMessage(getContext().getString(R.string.auth_account_does_not_exist));
                    getActivity().finish();
                }
            }

            if (success) {
                viewHome();
            } else {
                // init view again
            }

        } else if (result.isServerFail() || result.isException()) {
            /// server errors or exceptions in authorization take to requiring a new check of
            /// the server
            //TODO: display error.
            showToastMessage(result.getLogMessage());

        } else {    // authorization fail due to client side - probably wrong credentials
            //TODO: display error.
        }
    }

    /**
     * Updates the authentication token.
     *
     * Sets the proper response so that the AccountAuthenticator that started this activity
     * saves a new authorization token for mAccount.
     *
     * Kills the session kept by OwnCloudClientManager so that a new one will created with
     * the new credentials when needed.
     */
    public void updateAccountAuthentication() throws com.owncloud.android.lib.common.accounts.AccountUtils.AccountNotFoundException {


        Bundle response = new Bundle();
        response.putString(AccountManager.KEY_ACCOUNT_NAME, mAccount.name);
        response.putString(AccountManager.KEY_ACCOUNT_TYPE, mAccount.type);

        response.putString(AccountManager.KEY_AUTHTOKEN, mPresenter.getDeviceId());
        mAccountMgr.setPassword(mAccount, mPresenter.getDeviceId());

        // remove managed clients for this account to enforce creation with fresh credentials
        OwnCloudAccount ocAccount = new OwnCloudAccount(mAccount, getContext());
        OwnCloudClientManagerFactory.getDefaultSingleton().removeClientFor(ocAccount);

        ((AccountAuthenticatorActivity) getActivity()).setAccountAuthenticatorResult(response);

        final Intent intent = new Intent();
        intent.putExtras(response);
        getActivity().setResult(RESULT_OK, intent);

    }

    /**
     * Creates a new account through the Account Authenticator that started this activity.
     *
     * This makes the account permanent.
     *
     * TODO Decide how to name the OAuth accounts
     */
    public boolean createAccount(RemoteOperationResult authResult) {
        /// create and save new ownCloud account

        String lastPermanentLocation = authResult.getLastPermanentLocation();
        if (lastPermanentLocation != null) {
            mServerInfo.mBaseUrl = AccountUtils.trimWebdavSuffix(lastPermanentLocation);
        }

        Uri uri = Uri.parse(mServerInfo.mBaseUrl);
        String username;
        username = mPresenter.getUsername();
        String accountName = com.owncloud.android.lib.common.accounts.AccountUtils.
                buildAccountName(uri, username);
        Account newAccount = new Account(accountName, BaseConstants.ACCOUNT_TYPE);
        if (AccountUtils.exists(newAccount, getContext())) {
            // fail - not a new account, but an existing one; disallow
            RemoteOperationResult result = new RemoteOperationResult(RemoteOperationResult.ResultCode.ACCOUNT_NOT_NEW);

            Log_OC.d(TAG, result.getLogMessage());
            return false;

        } else {
            mAccount = newAccount;

            mAccountMgr.addAccountExplicitly(
                    mAccount, mPresenter.getDeviceId(), null
            );

            // include account version with the new account
            mAccountMgr.setUserData(
                    mAccount,
                    com.owncloud.android.lib.common.accounts.AccountUtils.Constants.KEY_OC_ACCOUNT_VERSION,
                    Integer.toString(AccountUtils.ACCOUNT_VERSION)
            );

            /// add the new account as default in preferences, if there is none already
            Account defaultAccount = AccountUtils.getCurrentOwnCloudAccount(getContext());
            if (defaultAccount == null) {
                mPresenter.setDefaultAccountName(accountName);
            }

            /// prepare result to return to the Authenticator
            //  TODO check again what the Authenticator makes with it; probably has the same
            //  effect as addAccountExplicitly, but it's not well done
            final Intent intent = new Intent();
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, BaseConstants.ACCOUNT_TYPE);
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mAccount.name);
            intent.putExtra(AccountManager.KEY_USERDATA, username);

            mAccountMgr.setUserData(
                    mAccount, com.owncloud.android.lib.common.accounts.AccountUtils.Constants.KEY_OC_VERSION, mServerInfo.mVersion.getVersion()
            );
            mAccountMgr.setUserData(
                    mAccount, com.owncloud.android.lib.common.accounts.AccountUtils.Constants.KEY_OC_BASE_URL, mServerInfo.mBaseUrl
            );
            if (authResult.getData() != null) {
                try {
                    UserInfo userInfo = (UserInfo) authResult.getData().get(0);
                    mAccountMgr.setUserData(
                            mAccount, com.owncloud.android.lib.common.accounts.AccountUtils.Constants.KEY_DISPLAY_NAME, userInfo.getDisplayName()
                    );
                } catch (ClassCastException c) {
                    Log_OC.w(TAG, "Couldn't get display name for " + username);
                }
            } else {
                Log_OC.w(TAG, "Couldn't get display name for " + username);
            }

            ((AccountAuthenticatorActivity) getActivity()).setAccountAuthenticatorResult(intent.getExtras());
            getActivity().setResult(RESULT_OK, intent);

            return true;
        }
    }

    @Override
    public void loginWithCredentials(OwnCloudCredentials credentials) {
        AuthenticatorAsyncTask loginAsyncTask = new AuthenticatorAsyncTask(this);
        Object[] params = {mServerInfo.mBaseUrl, credentials};
        loginAsyncTask.execute(params);
    }
}

