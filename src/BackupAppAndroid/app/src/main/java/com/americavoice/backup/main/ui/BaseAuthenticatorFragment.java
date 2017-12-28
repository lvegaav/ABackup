package com.americavoice.backup.main.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountAuthenticatorActivity;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.login.presenter.LoginPresenter;
import com.americavoice.backup.login.ui.LoginFragment;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.ui.activity.LoginActivity;
import com.americavoice.backup.operations.GetServerInfoOperation;
import com.americavoice.backup.utils.BaseConstants;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
import com.owncloud.android.lib.common.UserInfo;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

/**
 * Created by angelchanquin on 8/15/17.
 */

public class BaseAuthenticatorFragment extends BaseFragment {

    protected static final String TAG = "AuthenticatorActivity";

    protected byte mAction;
    protected Account mAccount;
    protected AccountManager mAccountMgr;
    protected GetServerInfoOperation.ServerInfo mServerInfo = new GetServerInfoOperation.ServerInfo();

    protected void initialize(){
        mAccountMgr = AccountManager.get(getContext());
        /// get input values
        mAction = getActivity().getIntent().getByteExtra(BaseConstants.EXTRA_ACTION, LoginActivity.ACTION_CREATE);
        if (getActivity().getIntent().hasExtra(BaseConstants.EXTRA_ACCOUNT)) {
            mAccount = getActivity().getIntent().getParcelableExtra(BaseConstants.EXTRA_ACCOUNT);
        }
        mServerInfo.mBaseUrl = getResources().getString(R.string.baseUrlOwnCloud);

    }

    /**
     * Updates the authentication token.
     *
     * Sets the proper response so that the AccountAuthenticator that started this activity
     * saves a new authorization token for mAccount.
     *
     * Kills the session kept by OwnCloudClientManager so that a new one will created with
     * the new credentials when needed.
     * @param deviceId
     */
    public void updateAccountAuthentication(String deviceId) throws com.owncloud.android.lib.common.accounts.AccountUtils.AccountNotFoundException {


        Bundle response = new Bundle();
        response.putString(AccountManager.KEY_ACCOUNT_NAME, mAccount.name);
        response.putString(AccountManager.KEY_ACCOUNT_TYPE, mAccount.type);

        response.putString(AccountManager.KEY_AUTHTOKEN, deviceId);
        mAccountMgr.setPassword(mAccount, deviceId);

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
    public boolean createAccount(RemoteOperationResult authResult, String username, String password, String backupUser, String backupPassword) {
        /// create and save new ownCloud account

        String lastPermanentLocation = authResult.getLastPermanentLocation();
        if (lastPermanentLocation != null) {
            mServerInfo.mBaseUrl = AccountUtils.trimWebdavSuffix(lastPermanentLocation);
        }

        Uri uri = Uri.parse(mServerInfo.mBaseUrl);

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
                    mAccount, password, null
            );

            mAccountMgr.setUserData(mAccount, "backupUser", backupUser);
            mAccountMgr.setUserData(mAccount, "backupPassword", backupPassword);

            // include account version with the new account
            mAccountMgr.setUserData(
                    mAccount,
                    com.owncloud.android.lib.common.accounts.AccountUtils.Constants.KEY_OC_ACCOUNT_VERSION,
                    Integer.toString(AccountUtils.ACCOUNT_VERSION)
            );

            /// add the new account as default in preferences, if there is none already
            Account defaultAccount = AccountUtils.getCurrentOwnCloudAccount(getContext());
            if (defaultAccount == null) {
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(getContext()).edit();
                editor.putString("select_oc_account", accountName);
                editor.apply();
            }

            /// prepare result to return to the Authenticator
            //  TODO check again what the Authenticator makes with it; probably has the same
            //  effect as addAccountExplicitly, but it's not well done
            final Intent intent = new Intent();
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, BaseConstants.ACCOUNT_TYPE);
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mAccount.name);
            intent.putExtra(AccountManager.KEY_USERDATA, username);

            mAccountMgr.setUserData(
                    mAccount, com.owncloud.android.lib.common.accounts.AccountUtils.Constants.KEY_OC_VERSION, "12.0.2"
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
}
