package com.americavoice.backup.main.ui.activity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;

import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.calls.ui.CallsBackupFragment;
import com.americavoice.backup.contacts.ui.ContactsBackupFragment;
import com.americavoice.backup.datamodel.ArbitraryDataProvider;
import com.americavoice.backup.datamodel.FileDataStorageManager;
import com.americavoice.backup.explorer.ui.FileListFragment;
import com.americavoice.backup.service.MediaContentJob;
import com.americavoice.backup.service.WifiRetryJob;
import com.americavoice.backup.sms.ui.SmsBackupFragment;
import com.americavoice.backup.utils.BaseConstants;
import com.americavoice.backup.utils.PermissionUtil;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.status.OCCapability;
import com.americavoice.backup.datamodel.OCFile;

/**
 * Base activity with common behaviour for activities dealing with ownCloud {@link Account}s .
 */
public abstract class BaseOwncloudActivity extends BaseActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    public static final String EXTRA_ACCOUNT = "com.americavoice.backup.main.ui.activity.ACCOUNT";
    public static final String EXTRA_FROM_NOTIFICATION = "com.americavoice.backup.main.ui.activity.FROM_NOTIFICATION";
    public static final String EXTRA_STORAGE_FULL = "EXTRA_STORAGE_FULL";
    public static final String EXTRA_REFRESH_DATA = "EXTRA_REFRESH_DATA";

    /** Flag to signal if the activity is launched by a notification */
    private boolean mFromNotification;

    /**
     * ownCloud {@link Account} where the main {@link OCFile} handled by the activity is located.
     */
    protected Account mCurrentAccount;

    /**
     * Capabilities of the server where {@link #mCurrentAccount} lives.
     */
    private OCCapability mCapabilities;

    /**
     * Flag to signal that the activity will is finishing to enforce the creation of an ownCloud {@link Account}.
     */
    private boolean mRedirectingToSetupAccount = false;

    /**
     * Flag to signal when the value of mAccount was set.
     */
    protected boolean mAccountWasSet;

    /**
     * Flag to signal when the value of mAccount was restored from a saved state.
     */
    protected boolean mAccountWasRestored;

    /**
     * Access point to the cached database for the current ownCloud {@link Account}.
     */
    private FileDataStorageManager mStorageManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Account account = null;
        if(savedInstanceState != null) {
            mFromNotification = savedInstanceState.getBoolean(EXTRA_FROM_NOTIFICATION);
        } else {
            account = getIntent().getParcelableExtra(EXTRA_ACCOUNT);
            mFromNotification = getIntent().getBooleanExtra(EXTRA_FROM_NOTIFICATION, false);
        }

        AccountUtils.updateAccountVersion(this); // best place, before any access to AccountManager
        // or database

        setAccount(account, savedInstanceState != null);
    }

    @Override
    protected void onNewIntent (Intent intent) {
        Log_OC.v(TAG, "onNewIntent() start");
        Account current = AccountUtils.getCurrentOwnCloudAccount(this);
        if (current != null && mCurrentAccount != null && !mCurrentAccount.name.equals(current.name)) {
            mCurrentAccount = current;
        }
        Log_OC.v(TAG, "onNewIntent() stop");
    }

    /**
     *  Since ownCloud {@link Account}s can be managed from the system setting menu, the existence of the {@link
     *  Account} associated to the instance must be checked every time it is restarted.
     */
    @Override
    protected void onRestart() {
        Log_OC.v(TAG, "onRestart() start");
        super.onRestart();
        boolean validAccount = (mCurrentAccount != null && AccountUtils.exists(mCurrentAccount, this));
        if (!validAccount) {
            swapToDefaultAccount();
        }
        Log_OC.v(TAG, "onRestart() end");
    }

    /**
     * Sets and validates the ownCloud {@link Account} associated to the Activity.
     *
     * If not valid, tries to swap it for other valid and existing ownCloud {@link Account}.
     *
     * POSTCONDITION: updates {@link #mAccountWasSet} and {@link #mAccountWasRestored}.
     *
     * @param account      New {@link Account} to set.
     * @param savedAccount When 'true', account was retrieved from a saved instance state.
     */

    protected void setAccount(Account account, boolean savedAccount) {
        Account oldAccount = mCurrentAccount;
        boolean validAccount = (account != null && AccountUtils.setCurrentOwnCloudAccount(getApplicationContext(), account.name));
        if (validAccount) {
            mCurrentAccount = account;
            mAccountWasSet = true;
            mAccountWasRestored = (savedAccount || mCurrentAccount.equals(oldAccount));
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                ArbitraryDataProvider arbitraryDataProvider = new ArbitraryDataProvider(getContentResolver());
                arbitraryDataProvider.storeOrUpdateKeyValue(mCurrentAccount, FileListFragment.PREFERENCE_PHOTOS_AUTOMATIC_BACKUP, String.valueOf(true));
                arbitraryDataProvider.storeOrUpdateKeyValue(mCurrentAccount, FileListFragment.PREFERENCE_VIDEOS_AUTOMATIC_BACKUP, String.valueOf(true));
                arbitraryDataProvider.storeOrUpdateKeyValue(mCurrentAccount, FileListFragment.PREFERENCE_MUSIC_AUTOMATIC_BACKUP, String.valueOf(true));
                arbitraryDataProvider.storeOrUpdateKeyValue(mCurrentAccount, ContactsBackupFragment.PREFERENCE_CONTACTS_AUTOMATIC_BACKUP, String.valueOf(true));
                ContactsBackupFragment.startContactBackupJob(mCurrentAccount);
                arbitraryDataProvider.storeOrUpdateKeyValue(mCurrentAccount, SmsBackupFragment.PREFERENCE_SMS_AUTOMATIC_BACKUP, String.valueOf(true));
                SmsBackupFragment.startSmsBackupJob(mCurrentAccount);
                arbitraryDataProvider.storeOrUpdateKeyValue(mCurrentAccount, CallsBackupFragment.PREFERENCE_CALLS_AUTOMATIC_BACKUP, String.valueOf(true));
                CallsBackupFragment.startCallBackupJob(mCurrentAccount);
            }
        } else {
            swapToDefaultAccount();
        }
        if (mAccountWasSet && PermissionUtil.checkSelfPermission(
                getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                scheduleMediaJob();
                scheduleWifiJob();
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void scheduleMediaJob() {
        MediaContentJob.scheduleJob(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void scheduleWifiJob() {
        WifiRetryJob.scheduleJob(this);
    }

    /**
     * Tries to swap the current ownCloud {@link Account} for other valid and existing.
     *
     * If no valid ownCloud {@link Account} exists, the the user is requested
     * to create a new ownCloud {@link Account}.
     *
     * POSTCONDITION: updates {@link #mAccountWasSet} and {@link #mAccountWasRestored}.
     */
    protected void swapToDefaultAccount() {
        // default to the most recently used account
        Account newAccount = AccountUtils.getCurrentOwnCloudAccount(getApplicationContext());
        if (newAccount == null) {
            /// no account available: force account creation
            createAccount(true);
            mRedirectingToSetupAccount = true;
            mAccountWasSet = false;
            mAccountWasRestored = false;

        } else {
            mAccountWasSet = true;
            mAccountWasRestored = (newAccount.equals(mCurrentAccount));
            mCurrentAccount = newAccount;
        }
    }

    /**
     * Launches the account creation activity.
     *
     * @param mandatoryCreation     When 'true', if an account is not created by the user, the app will be closed.
     *                              To use when no ownCloud account is available.
     */
    protected void createAccount(boolean mandatoryCreation) {
        AccountManager am = AccountManager.get(getApplicationContext());
        am.addAccount(BaseConstants.ACCOUNT_TYPE,
                null,
                null,
                null,
                this,
                new AccountCreationCallback(mandatoryCreation),
                new Handler());
    }

    /**
     * Called when the ownCloud {@link Account} associated to the Activity was just updated.
     *
     * Child classes must grant that state depending on the {@link Account} is updated.
     */
    protected void onAccountSet(boolean stateWasRecovered) {
        if (getAccount() != null) {
            mStorageManager = new FileDataStorageManager(getAccount(), getApplicationContext());
            mCapabilities = mStorageManager.getCapability(mCurrentAccount.name);
        } else {
            Log_OC.e(TAG, "onAccountChanged was called with NULL account associated!");
        }
    }

    protected void setAccount(Account account) {
        mCurrentAccount = account;
    }

    /**
     * Getter for the capabilities of the server where the current OC account lives.
     *
     * @return Capabilities of the server where the current OC account lives. Null if the account is not
     * set yet.
     */
    public OCCapability getCapabilities() {
        return mCapabilities;
    }

    /**
     * Getter for the ownCloud {@link Account} where the main {@link OCFile} handled by the activity
     * is located.
     *
     * @return OwnCloud {@link Account} where the main {@link OCFile} handled by the activity
     * is located.
     */
    public Account getAccount() {
        return mCurrentAccount;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAccountWasSet) {
            onAccountSet(mAccountWasRestored);
        }
    }

    /**
     * @return 'True' when the Activity is finishing to enforce the setup of a new account.
     */
    protected boolean isRedirectingToSetupAccount() {
        return mRedirectingToSetupAccount;
    }

    public FileDataStorageManager getStorageManager() {
        return mStorageManager;
    }

    /**
     * Method that gets called when a new account has been successfully created.
     *
     * @param future
     */
    protected void onAccountCreationSuccessful(AccountManagerFuture<Bundle> future) {
        // no special handling in base activity
    }

    /**
     * Helper class handling a callback from the {@link AccountManager} after the creation of
     * a new ownCloud {@link Account} finished, successfully or not.
     */
    public class AccountCreationCallback implements AccountManagerCallback<Bundle> {

        boolean mMandatoryCreation;

        /**
         * Constructor
         *
         * @param mandatoryCreation     When 'true', if an account was not created, the app is closed.
         */
        public AccountCreationCallback(boolean mandatoryCreation) {
            mMandatoryCreation = mandatoryCreation;
        }

        @Override
        public void run(AccountManagerFuture<Bundle> future) {
            BaseOwncloudActivity.this.mRedirectingToSetupAccount = false;
            boolean accountWasSet = false;
            if (future != null) {
                try {
                    Bundle result;
                    result = future.getResult();
                    String name = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                    String type = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
                    if (AccountUtils.setCurrentOwnCloudAccount(getApplicationContext(), name)) {
                        setAccount(new Account(name, type), false);
                        accountWasSet = true;
                    }

                    onAccountCreationSuccessful(future);
                } catch (OperationCanceledException e) {
                    ActivityCompat.finishAffinity(BaseOwncloudActivity.this);
                    Log_OC.d(TAG, "Account creation canceled");

                } catch (Exception e) {
                    Log_OC.e(TAG, "Account creation finished in exception: ", e);
                }

            } else {
                Log_OC.e(TAG, "Account creation callback with null bundle");
            }
            if (mMandatoryCreation && !accountWasSet) {
                moveTaskToBack(true);
            }
        }
    }
}
