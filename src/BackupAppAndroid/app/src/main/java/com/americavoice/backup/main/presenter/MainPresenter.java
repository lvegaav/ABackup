
package com.americavoice.backup.main.presenter;

import android.accounts.Account;
import android.content.Context;
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
import com.americavoice.backup.sms.ui.SmsBackupFragment;

import net.servicestack.client.AsyncResult;

import javax.inject.Inject;


/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class MainPresenter extends BasePresenter implements IPresenter {

    private MainView mView;
    private Context mContext;

    @Inject
    public MainPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull MainView view) {
        this.mView = view;
    }

    @Override
    public void resume() {
        initBadges();
        if (mSharedPrefsUtils.getBooleanPreference(NetworkProvider.KEY_FIRST_TIME, false)) {
            mView.showSync();
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
    public void initialize(Context context, String title) {
        mView.setTitle(title);
        mContext = context;
    }

    public void initBadges() {
        Account account = AccountUtils.getCurrentOwnCloudAccount(mContext);
        ArbitraryDataProvider arbitraryDataProvider = new ArbitraryDataProvider(mContext.getContentResolver());

        if (account == null) return;
        //Photos
        mView.setBadgePhotos(arbitraryDataProvider.getIntegerValue(account, FileListFragment.PREFERENCE_PHOTOS_LAST_TOTAL));
        mView.setBadgeVideos(arbitraryDataProvider.getIntegerValue(account, FileListFragment.PREFERENCE_VIDEOS_LAST_TOTAL));
        mView.setBadgeFiles(arbitraryDataProvider.getIntegerValue(account, FileListFragment.PREFERENCE_DOCUMENTS_LAST_TOTAL));
        mView.setBadgeContacts(arbitraryDataProvider.getIntegerValue(account, ContactsBackupFragment.PREFERENCE_CONTACTS_LAST_TOTAL));
        mView.setBadgeSms(arbitraryDataProvider.getIntegerValue(account, SmsBackupFragment.PREFERENCE_SMS_LAST_TOTAL));
        mView.setBadgeCallLog(arbitraryDataProvider.getIntegerValue(account, CallsBackupFragment.PREFERENCE_CALLS_LAST_TOTAL));
    }
}