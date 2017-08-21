/**
 *   ownCloud Android client application
 *
 *   @author David A. Velasco
 *   Copyright (C) 2011  Bartek Przybylski
 *   Copyright (C) 2016 ownCloud Inc.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2,
 *   as published by the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.americavoice.backup.main.ui.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.files.service.FileDownloader;
import com.americavoice.backup.files.service.FileUploader;
import com.americavoice.backup.operations.SynchronizeFileOperation;
import com.americavoice.backup.operations.SynchronizeFolderOperation;
import com.americavoice.backup.service.OperationsService;
import com.americavoice.backup.utils.BaseConstants;
import com.americavoice.backup.utils.ComponentsGetter;
import com.americavoice.backup.utils.ErrorMessageAdapter;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.network.CertificateCombinedException;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode;
import com.owncloud.android.lib.common.utils.Log_OC;


/**
 * Activity with common behaviour for activities handling {@link OCFile}s in ownCloud {@link Account}s .
 */
public abstract class FileActivity extends BaseOwncloudActivity
        implements OnRemoteOperationListener, ComponentsGetter {

    public static final String EXTRA_FILE = "com.owncloud.android.ui.activity.FILE";
    public static final String EXTRA_ACCOUNT = "com.owncloud.android.ui.activity.ACCOUNT";
    public static final String EXTRA_FROM_NOTIFICATION =
            "com.owncloud.android.ui.activity.FROM_NOTIFICATION";

    public static final String TAG = FileActivity.class.getSimpleName();

    private static final String DIALOG_WAIT_TAG = "DIALOG_WAIT";

    private static final String KEY_WAITING_FOR_OP_ID = "WAITING_FOR_OP_ID";
    private static final String KEY_ACTION_BAR_TITLE = "ACTION_BAR_TITLE";

    public static final int REQUEST_CODE__UPDATE_CREDENTIALS = 0;
    public static final int REQUEST_CODE__LAST_SHARED = REQUEST_CODE__UPDATE_CREDENTIALS;

    protected static final long DELAY_TO_REQUEST_OPERATIONS_LATER = 200;

    /* Dialog tags */
    private static final String DIALOG_UNTRUSTED_CERT = "DIALOG_UNTRUSTED_CERT";
    private static final String DIALOG_CERT_NOT_SAVED = "DIALOG_CERT_NOT_SAVED";

     /** Main {@link OCFile} handled by the activity.*/
    private OCFile mFile;

    /** Flag to signal if the activity is launched by a notification */
    private boolean mFromNotification;

    /** Messages handler associated to the main thread and the life cycle of the activity */
    private Handler mHandler;

    private ServiceConnection mOperationsServiceConnection = null;

    private OperationsService.OperationsServiceBinder mOperationsServiceBinder = null;

    private boolean mResumed = false;

    protected FileDownloader.FileDownloaderBinder mDownloaderBinder = null;
    protected FileUploader.FileUploaderBinder mUploaderBinder = null;
    private ServiceConnection mDownloadServiceConnection, mUploadServiceConnection = null;



    /**
     * Loads the ownCloud {@link Account} and main {@link OCFile} to be handled by the instance of
     * the {@link FileActivity}.
     *
     * Grants that a valid ownCloud {@link Account} is associated to the instance, or that the user
     * is requested to create a new one.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
//        mFileOperationsHelper = new FileOperationsHelper(this);
        Account account = null;
        if(savedInstanceState != null) {
            mFile = savedInstanceState.getParcelable(FileActivity.EXTRA_FILE);
            mFromNotification = savedInstanceState.getBoolean(FileActivity.EXTRA_FROM_NOTIFICATION);
//            mFileOperationsHelper.setOpIdWaitingFor(
//                    savedInstanceState.getLong(KEY_WAITING_FOR_OP_ID, Long.MAX_VALUE)
//                    );

        } else {
            account = getIntent().getParcelableExtra(FileActivity.EXTRA_ACCOUNT);
            mFile = getIntent().getParcelableExtra(FileActivity.EXTRA_FILE);
            mFromNotification = getIntent().getBooleanExtra(FileActivity.EXTRA_FROM_NOTIFICATION,
                    false);
        }

        AccountUtils.updateAccountVersion(this); // best place, before any access to AccountManager
                                                 // or database

        setAccount(account, savedInstanceState != null);

        mOperationsServiceConnection = new OperationsServiceConnection();
        bindService(new Intent(this, OperationsService.class), mOperationsServiceConnection,
                Context.BIND_AUTO_CREATE);

        mDownloadServiceConnection = newTransferenceServiceConnection();
        if (mDownloadServiceConnection != null) {
            bindService(new Intent(this, FileDownloader.class), mDownloadServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
        mUploadServiceConnection = newTransferenceServiceConnection();
        if (mUploadServiceConnection != null) {
            bindService(new Intent(this, FileUploader.class), mUploadServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }




    @Override
    protected void onResume() {
        super.onResume();
        mResumed = true;
        if (mOperationsServiceBinder != null) {
//            doOnResumeAndBound();
        }
    }

    @Override
    protected void onPause()  {
        if (mOperationsServiceBinder != null) {
            mOperationsServiceBinder.removeOperationListener(this);
        }
        mResumed = false;
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        if (mOperationsServiceConnection != null) {
            unbindService(mOperationsServiceConnection);
            mOperationsServiceBinder = null;
        }
        if (mDownloadServiceConnection != null) {
            unbindService(mDownloadServiceConnection);
            mDownloadServiceConnection = null;
        }
        if (mUploadServiceConnection != null) {
            unbindService(mUploadServiceConnection);
            mUploadServiceConnection = null;
        }

        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(FileActivity.EXTRA_FILE, mFile);
        outState.putBoolean(FileActivity.EXTRA_FROM_NOTIFICATION, mFromNotification);
        if(getSupportActionBar() != null && getSupportActionBar().getTitle() != null) {
            // Null check in case the actionbar is used in ActionBar.NAVIGATION_MODE_LIST
            // since it doesn't have a title then
            outState.putString(KEY_ACTION_BAR_TITLE, getSupportActionBar().getTitle().toString());
        }
    }

    /**
     * Getter for the main {@link OCFile} handled by the activity.
     *
     * @return  Main {@link OCFile} handled by the activity.
     */
    public OCFile getFile() {
        return mFile;
    }


    /**
     * Setter for the main {@link OCFile} handled by the activity.
     *
     * @param file  Main {@link OCFile} to be handled by the activity.
     */
    public void setFile(OCFile file) {
        mFile = file;
    }

    /**
     * @return Value of mFromNotification: True if the Activity is launched by a notification
     */
    public boolean fromNotification() {
        return mFromNotification;
    }

    public OperationsService.OperationsServiceBinder getOperationsServiceBinder() {
        return mOperationsServiceBinder;
    }

    protected ServiceConnection newTransferenceServiceConnection() {
        return null;
    }

    public OnRemoteOperationListener getRemoteOperationListener() {
        return this;
    }


    public Handler getHandler() {
        return mHandler;
    }


    /**
     *
     * @param operation     Operation performed.
     * @param result        Result of the removal.
     */
    @Override
    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
        Log_OC.d(TAG, "Received result of operation in FileActivity - common behaviour for all the "
            + "FileActivities ");

        if (!result.isSuccess() && (
                result.getCode() == ResultCode.UNAUTHORIZED ||
                (result.isException() && result.getException() instanceof AuthenticatorException)
                )) {

            requestCredentialsUpdate(this);

            if (result.getCode() == ResultCode.UNAUTHORIZED) {
                Toast t = Toast.makeText(this, ErrorMessageAdapter.getErrorCauseMessage(result,
                        operation, getResources()),
                    Toast.LENGTH_LONG);
                t.show();
            }

        } else if (operation == null || operation instanceof SynchronizeFolderOperation) {
            if (result.isSuccess()) {
                updateFileFromDB();

            } else if (result.getCode() != ResultCode.CANCELLED) {
                Toast t = Toast.makeText(this,
                        ErrorMessageAdapter.getErrorCauseMessage(result, operation, getResources()),
                        Toast.LENGTH_LONG);
                t.show();
            }

        } else if (operation instanceof SynchronizeFileOperation) {
            onSynchronizeFileOperationFinish((SynchronizeFileOperation) operation, result);

        }
    }

    /**
     * Invalidates the credentials stored for the current OC account and requests new credentials to the user,
     * navigating to {@link LoginActivity}
     *
     * Equivalent to call requestCredentialsUpdate(context, null);
     *
     * @param context   Android Context needed to access the {@link AccountManager}. Received as a parameter
     *                  to make the method accessible to {@link android.content.BroadcastReceiver}s.
     */
    protected void requestCredentialsUpdate(Context context) {
        requestCredentialsUpdate(context, null);
    }

    /**
     * Invalidates the credentials stored for the given OC account and requests new credentials to the user,
     * navigating to {@link LoginActivity}
     *
     * @param context   Android Context needed to access the {@link AccountManager}. Received as a parameter
     *                  to make the method accessible to {@link android.content.BroadcastReceiver}s.
     * @param account   Stored OC account to request credentials update for. If null, current account will
     *                  be used.
     */
    protected void requestCredentialsUpdate(Context context, Account account) {

        try {
            /// step 1 - invalidate credentials of current account
            if (account == null) {
                account = getAccount();
            }
            OwnCloudClient client;
            OwnCloudAccount ocAccount = new OwnCloudAccount(account, context);
            client = (OwnCloudClientManagerFactory.getDefaultSingleton().
                    removeClientFor(ocAccount));
            if (client != null) {
                OwnCloudCredentials cred = client.getCredentials();
                if (cred != null) {
                    AccountManager am = AccountManager.get(context);
                    if (cred.authTokenExpires()) {
                        am.invalidateAuthToken(
                                account.type,
                                cred.getAuthToken()
                        );
                    } else {
                        am.clearPassword(account);
                    }
                }
            }

            /// step 2 - request credentials to user
            Intent updateAccountCredentials = new Intent(this, LoginActivity.class);
            updateAccountCredentials.putExtra(BaseConstants.EXTRA_ACCOUNT, account);
            updateAccountCredentials.putExtra(
                    BaseConstants.EXTRA_ACTION,
                    LoginActivity.ACTION_UPDATE_EXPIRED_TOKEN);
            updateAccountCredentials.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivityForResult(updateAccountCredentials, REQUEST_CODE__UPDATE_CREDENTIALS);

        } catch (com.owncloud.android.lib.common.accounts.AccountUtils.AccountNotFoundException e) {
            Toast.makeText(context, R.string.auth_account_does_not_exist, Toast.LENGTH_SHORT).show();
        }

    }

    private void onSynchronizeFileOperationFinish(SynchronizeFileOperation operation,
                                                  RemoteOperationResult result) {
        OCFile syncedFile = operation.getLocalFile();
        if (!result.isSuccess()) {
//            if (result.getCode() == ResultCode.SYNC_CONFLICT) {
//                Intent i = new Intent(this, ConflictsResolveActivity.class);
//                i.putExtra(ConflictsResolveActivity.EXTRA_FILE, syncedFile);
//                i.putExtra(ConflictsResolveActivity.EXTRA_ACCOUNT, getAccount());
//                startActivity(i);
//            }

        } else {
            if (!operation.transferWasRequested()) {
                Toast msg = Toast.makeText(this, ErrorMessageAdapter.getErrorCauseMessage(result,
                        operation, getResources()), Toast.LENGTH_LONG);
                msg.show();
            }
            invalidateOptionsMenu();
        }
    }

    protected void updateFileFromDB(){
        OCFile file = getFile();
        if (file != null) {
            file = getStorageManager().getFileByPath(file.getRemotePath());
            setFile(file);
        }
    }


//    private void doOnResumeAndBound() {
//        mOperationsServiceBinder.addOperationListener(FileActivity.this, mHandler);
//        long waitingForOpId = mFileOperationsHelper.getOpIdWaitingFor();
//        if (waitingForOpId <= Integer.MAX_VALUE) {
//            boolean wait = mOperationsServiceBinder.dispatchResultIfFinished((int)waitingForOpId,
//                    this);
//            if (!wait ) {
//                dismissLoadingDialog();
//            }
//        }
//    }


    /**
     * Implements callback methods for service binding. Passed as a parameter to {
     */
    private class OperationsServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName component, IBinder service) {
            if (component.equals(new ComponentName(FileActivity.this, OperationsService.class))) {
                Log_OC.d(TAG, "Operations service connected");
                mOperationsServiceBinder = (OperationsService.OperationsServiceBinder) service;
                /*if (!mOperationsServiceBinder.isPerformingBlockingOperation()) {
                    dismissLoadingDialog();
                }*/
//                if (mResumed) {
//                    doOnResumeAndBound();
//                }

            } else {
                return;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName component) {
            if (component.equals(new ComponentName(FileActivity.this, OperationsService.class))) {
                Log_OC.d(TAG, "Operations service disconnected");
                mOperationsServiceBinder = null;
                // TODO whatever could be waiting for the service is unbound
            }
        }
    }

    @Override
    public FileDownloader.FileDownloaderBinder getFileDownloaderBinder() {
        return mDownloaderBinder;
    }

    @Override
    public FileUploader.FileUploaderBinder getFileUploaderBinder() {
        return mUploaderBinder;
    }

    protected OCFile getCurrentDir() {
        OCFile file = getFile();
        if (file != null) {
            if (file.isFolder()) {
                return file;
            } else if (getStorageManager() != null) {
                String parentPath = file.getParentRemotePath();
                return getStorageManager().getFileByPath(parentPath);
            }
        }
        return null;
    }

}
