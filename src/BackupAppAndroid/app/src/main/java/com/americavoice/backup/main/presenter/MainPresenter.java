
package com.americavoice.backup.main.presenter;

import android.accounts.Account;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

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
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;

import net.servicestack.client.AsyncResult;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;

import javax.inject.Inject;


/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class MainPresenter extends BasePresenter implements IPresenter, OnRemoteOperationListener {

    public static final String SHOW_CASE_ALREADY = "MAIN_SHOW_CASE_ALREADY";

    private MainView mView;
    private Handler mHandler;
    private Context mContext;

    @Inject
    MainPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
        mHandler = new Handler();
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
        synchronizeRootFolder();
    }

    private void synchronizeRootFolder() {
        OwnCloudClient client = mNetworkProvider.getCloudClient();
        if (client != null) {
            ReadRemoteFolderOperation mReadRemoteOperation = new ReadRemoteFolderOperation("/");
            mReadRemoteOperation.execute(client, this, mHandler);
        }
    }

    private void initBadges() {
        if (mContext == null || mView == null)
            return;

        Account account = AccountUtils.getCurrentOwnCloudAccount(mContext);
        if (account == null) return;

        ArbitraryDataProvider arbitraryDataProvider = new ArbitraryDataProvider(mContext.getContentResolver());

        mView.setBadgePhotos(arbitraryDataProvider.getIntegerValue(account, FileListFragment.PREFERENCE_PHOTOS_LAST_TOTAL));
        mView.setBadgeVideos(arbitraryDataProvider.getIntegerValue(account, FileListFragment.PREFERENCE_VIDEOS_LAST_TOTAL));
        mView.setBadgeFiles(arbitraryDataProvider.getIntegerValue(account, FileListFragment.PREFERENCE_DOCUMENTS_LAST_TOTAL));
        mView.setBadgeContacts(arbitraryDataProvider.getIntegerValue(account, ContactsBackupFragment.PREFERENCE_CONTACTS_LAST_TOTAL));
        mView.setBadgeSms(arbitraryDataProvider.getIntegerValue(account, SmsBackupFragment.PREFERENCE_SMS_LAST_TOTAL));
        mView.setBadgeCallLog(arbitraryDataProvider.getIntegerValue(account, CallsBackupFragment.PREFERENCE_CALLS_LAST_TOTAL));
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation remoteOperation, RemoteOperationResult remoteOperationResult) {
        if (remoteOperationResult.getData() == null)
            return;

        BigDecimal total = new BigDecimal(0);
        BigDecimal totalAvailable = new BigDecimal(0);
        for(Object obj: remoteOperationResult.getData()) {

            RemoteFile remoteFile = (RemoteFile) obj;

            if (remoteFile.getRemotePath().equals("/")) {
                try {
                    Field field = RemoteFile.class.getDeclaredField("mQuotaAvailableBytes");
                    field.setAccessible(true);
                    total = (BigDecimal) field.get(remoteFile);
                    totalAvailable = (BigDecimal) field.get(remoteFile);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if (remoteFile.getRemotePath().equals(BaseConstants.DOCUMENTS_REMOTE_FOLDER)
                    || remoteFile.getRemotePath().equals(BaseConstants.PHOTOS_REMOTE_FOLDER)
                    || remoteFile.getRemotePath().equals(BaseConstants.VIDEOS_REMOTE_FOLDER)
                    || remoteFile.getRemotePath().equals(BaseConstants.CONTACTS_REMOTE_FOLDER)
                    || remoteFile.getRemotePath().equals(BaseConstants.CALLS_REMOTE_FOLDER)
                    || remoteFile.getRemotePath().equals(BaseConstants.SMS_REMOTE_FOLDER)) {

                BigDecimal size = BigDecimal.valueOf(remoteFile.getSize());
                total = total.add(size);
            }
        }

        float availablePercentage = getPercent(totalAvailable, total);
        float totalAvInGB = totalAvailable.divide(new BigDecimal(1073741824), 3, BigDecimal.ROUND_HALF_UP).floatValue();
        float totalInGB = total.divide(new BigDecimal(1073741824), 3, BigDecimal.ROUND_HALF_UP).floatValue();
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
}