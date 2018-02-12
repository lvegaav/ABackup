
package com.americavoice.backup.main.presenter;

import android.accounts.Account;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.calls.ui.CallsBackupFragment;
import com.americavoice.backup.contacts.ui.ContactsBackupFragment;
import com.americavoice.backup.datamodel.ArbitraryDataProvider;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.explorer.ui.FileListFragment;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.ui.MainView;
import com.americavoice.backup.main.ui.activity.MainActivity;
import com.americavoice.backup.sms.ui.SmsBackupFragment;
import com.americavoice.backup.utils.BaseConstants;
import com.crashlytics.android.Crashlytics;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;

import net.servicestack.client.AsyncResult;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;


/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class MainPresenter extends BasePresenter implements IPresenter, OnRemoteOperationListener {

    private static final String SHOW_CASE_ALREADY = "MAIN_SHOW_CASE_ALREADY";

    private MainView mView;
    private Context mContext;

    private Handler mHandler;

    private ReadRemoteFolderOperation mReadRemotePhotosOperation;
    private ReadRemoteFolderOperation mReadRemoteVideosOperation;
    private ReadRemoteFolderOperation mReadRemoteDocumentsOperation;

    @Inject
    MainPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull MainView view) {
        this.mView = view;
    }

    @Override
    public void resume() {
        initBadges();
        if (mSharedPrefsUtils.getBooleanPreference(MainActivity.EXTRA_REFRESH_DATA, false)) {
            synchronizeRootFolder();
        }
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
    public void initialize(String title) {
        if (mView != null) {
            mView.setTitle(title);
            mContext = mView.getContext();
        }
        mHandler = new Handler();
        synchronizeRootFolder();
        getFilesCount();
    }

    public void getFilesCount() {
        readRemoteFiles(BaseConstants.PHOTOS_REMOTE_FOLDER);
        readRemoteFiles(BaseConstants.VIDEOS_REMOTE_FOLDER);
        readRemoteFiles(BaseConstants.DOCUMENTS_REMOTE_FOLDER);
    }

    private void synchronizeRootFolder() {

        mNetworkProvider.getUserAccountUsage(new AsyncResult<dtos.GetAccountUsageResponse>() {
            @Override
            public void success(dtos.GetAccountUsageResponse response) {
                if (response != null) {

                    BigDecimal totalQuota = new BigDecimal(response.getTotalQuota());
                    BigDecimal availableQuota = new BigDecimal(response.getAvailableQuota());

                    float availablePercentage = getPercent(availableQuota, totalQuota);
                    float totalAvInGB = availableQuota.divide(new BigDecimal(1073741824), 3, BigDecimal.ROUND_HALF_UP).floatValue();
                    float totalInGB = totalQuota.divide(new BigDecimal(1073741824), 3, BigDecimal.ROUND_HALF_UP).floatValue();
                    //percent lower than 10 or total less than 1GB
                    mSharedPrefsUtils.setBooleanPreference(FileListFragment.PREFERENCE_STORAGE_ALMOST_FULL, (availablePercentage < 10 && availablePercentage > 0) || totalInGB < 1);
                    // availablePercentage lower or equal than 1 or total available lower or equal than 0.01 GB
                    mSharedPrefsUtils.setBooleanPreference(BaseConstants.PreferenceKeys.STORAGE_FULL, availablePercentage <= 1 || totalAvInGB <= 0.01);

                    if (mSharedPrefsUtils.getBooleanPreference(BaseConstants.PreferenceKeys.STORAGE_FULL, false)) {
                        if (mView != null) {
                            mView.showStorageFullDialog(false);
                        }
                    }

                    mSharedPrefsUtils.setBooleanPreference(MainActivity.EXTRA_REFRESH_DATA, false);
                }

            }

            @Override
            public void error(Exception ex) {
                Crashlytics.logException(ex);
            }

            @Override
            public void complete() {
                mView.hideLoading();
            }
        });
    }

    private void initBadges() {
        if (mContext == null || mView == null)
            return;

        Account account = AccountUtils.getCurrentOwnCloudAccount(mContext);
        if (account == null) return;

        ArbitraryDataProvider arbitraryDataProvider = new ArbitraryDataProvider(mContext.getContentResolver());

        mView.setBadgePhotos(arbitraryDataProvider.getIntegerValue(account, FileListFragment.PREFERENCE_PHOTOS_LAST_TOTAL + account.name));
        mView.setBadgeVideos(arbitraryDataProvider.getIntegerValue(account, FileListFragment.PREFERENCE_VIDEOS_LAST_TOTAL + account.name));
        mView.setBadgeFiles(arbitraryDataProvider.getIntegerValue(account, FileListFragment.PREFERENCE_DOCUMENTS_LAST_TOTAL + account.name));
        mView.setBadgeContacts(arbitraryDataProvider.getIntegerValue(account, ContactsBackupFragment.PREFERENCE_CONTACTS_LAST_TOTAL + account.name));
        mView.setBadgeSms(arbitraryDataProvider.getIntegerValue(account, SmsBackupFragment.PREFERENCE_SMS_LAST_TOTAL + account.name));
        mView.setBadgeCallLog(arbitraryDataProvider.getIntegerValue(account, CallsBackupFragment.PREFERENCE_CALLS_LAST_TOTAL + account.name));
    }

    private float getPercent(BigDecimal value, BigDecimal size) {
        float x = value != null ? value.floatValue() * 100 : 0;
        float x1 = x / size.floatValue();
        BigDecimal x2= new BigDecimal(x1).setScale(1, BigDecimal.ROUND_HALF_UP);
        return x2.floatValue();
    }

    public void showCaseFinished() {
        mSharedPrefsUtils.setBooleanPreference(SHOW_CASE_ALREADY, true);
    }

    public boolean getShowCaseFinished() {
        return mSharedPrefsUtils.getBooleanPreference(SHOW_CASE_ALREADY, false);
    }

    public void readRemoteFiles(String path) {
        OwnCloudClient client = mNetworkProvider.getCloudClient();
        if (client != null) {
            if (mReadRemotePhotosOperation == null){
                mReadRemotePhotosOperation = new ReadRemoteFolderOperation(BaseConstants.PHOTOS_REMOTE_FOLDER);
            }
            if (mReadRemoteVideosOperation == null) {
                mReadRemoteVideosOperation = new ReadRemoteFolderOperation(BaseConstants.VIDEOS_REMOTE_FOLDER);
            }
            if (mReadRemoteDocumentsOperation == null) {
                mReadRemoteDocumentsOperation = new ReadRemoteFolderOperation(BaseConstants.DOCUMENTS_REMOTE_FOLDER);
            }
            switch (path) {
                case BaseConstants.PHOTOS_REMOTE_FOLDER:
                    mReadRemotePhotosOperation.execute(client, this, mHandler);
                    break;
                case BaseConstants.VIDEOS_REMOTE_FOLDER:
                    mReadRemoteVideosOperation.execute(client, this, mHandler);
                    break;
                case BaseConstants.DOCUMENTS_REMOTE_FOLDER:
                    mReadRemoteDocumentsOperation.execute(client, this, mHandler);
                    break;
            }
        }
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation remoteOperation, RemoteOperationResult remoteOperationResult) {
        final Account account = AccountUtils.getCurrentOwnCloudAccount(mContext);
        if (account == null) {
             return;
        }
        ArbitraryDataProvider arbitraryDataProvider = new ArbitraryDataProvider(mContext.getContentResolver());
        if (remoteOperationResult.isSuccess() && remoteOperation instanceof ReadRemoteFolderOperation) {
            List<Object> data = remoteOperationResult.getData();
            int count = 0;
            if (data != null) {
                count = data.size() - 1;
            }
            if (remoteOperation.equals(mReadRemotePhotosOperation)){
                mView.setBadgePhotos(count);
                arbitraryDataProvider.storeOrUpdateKeyValue(account,
                        FileListFragment.PREFERENCE_PHOTOS_LAST_TOTAL + account.name,
                        String.valueOf(count));
            } else if (remoteOperation.equals(mReadRemoteVideosOperation)) {
                mView.setBadgeVideos(count);
                arbitraryDataProvider.storeOrUpdateKeyValue(account,
                        FileListFragment.PREFERENCE_VIDEOS_LAST_TOTAL + account.name,
                        String.valueOf(count));
            } else if (remoteOperation.equals(mReadRemoteDocumentsOperation)) {
                mView.setBadgeFiles(count);
                arbitraryDataProvider.storeOrUpdateKeyValue(account,
                        FileListFragment.PREFERENCE_DOCUMENTS_LAST_TOTAL + account.name,
                        String.valueOf(count));
            }
        }
    }
}