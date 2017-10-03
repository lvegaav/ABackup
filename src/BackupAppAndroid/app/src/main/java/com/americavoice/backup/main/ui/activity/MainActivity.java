package com.americavoice.backup.main.ui.activity;

import android.Manifest;
import android.accounts.Account;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.afollestad.materialdialogs.MaterialDialog;
import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.calls.ui.CallsBackupFragment;
import com.americavoice.backup.contacts.ui.ContactsBackupFragment;
import com.americavoice.backup.datamodel.ArbitraryDataProvider;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.di.components.DaggerAppComponent;
import com.americavoice.backup.explorer.ui.FileListFragment;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.MainFragment;
import com.americavoice.backup.service.MediaContentJob;
import com.americavoice.backup.service.WifiRetryJob;
import com.americavoice.backup.settings.ui.SettingsFragment;
import com.americavoice.backup.sms.ui.SmsBackupFragment;
import com.americavoice.backup.sync.ui.SyncFragment;
import com.americavoice.backup.utils.BaseConstants;
import com.americavoice.backup.utils.PermissionUtil;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

import static com.americavoice.backup.utils.FirebaseUtils.MENU_BUTTON_CONTENT_TYPE;
import static com.americavoice.backup.utils.FirebaseUtils.createFirebaseEvent;


public class MainActivity extends BaseOwncloudActivity implements HasComponent<AppComponent>,
        SyncFragment.Listener,
        MainFragment.Listener,
        SettingsFragment.Listener {

    private AppComponent mAppComponent;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initializeActivity(savedInstanceState);
        this.initializeInjector();
        this.initializeView();
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     * Initializes this activity.
     */
    private void initializeActivity(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            addFragment(R.id.fl_fragment, MainFragment.newInstance());
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initializeView() {
    }

    private void initializeInjector() {
        ButterKnife.bind(this);
        this.mAppComponent = DaggerAppComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build();
    }

    @Override
    public AppComponent getComponent() {
        return mAppComponent;
    }

    @Override
    public void viewPhotos() {
        createFirebaseEvent(
                mFirebaseAnalytics,
                "Photos",
                "Photos button",
                MENU_BUTTON_CONTENT_TYPE,
                FirebaseAnalytics.Event.SELECT_CONTENT
        );
        navigator.navigateToFileListActivity(this, BaseConstants.PHOTOS_REMOTE_FOLDER);
    }

    @Override
    public void viewVideos() {
        createFirebaseEvent(
                mFirebaseAnalytics,
                "Videos",
                "Videos button",
                MENU_BUTTON_CONTENT_TYPE,
                FirebaseAnalytics.Event.SELECT_CONTENT
        );
        navigator.navigateToFileListActivity(this, BaseConstants.VIDEOS_REMOTE_FOLDER);
    }

    @Override
    public void viewContacts() {
        createFirebaseEvent(
                mFirebaseAnalytics,
                "Contacts",
                "Contacts button",
                MENU_BUTTON_CONTENT_TYPE,
                FirebaseAnalytics.Event.SELECT_CONTENT
        );
        navigator.navigateToContactsBackupActivity(this);
    }

    @Override
    public void viewCalls() {
        createFirebaseEvent(
                mFirebaseAnalytics,
                "Calls",
                "Calls button",
                MENU_BUTTON_CONTENT_TYPE,
                FirebaseAnalytics.Event.SELECT_CONTENT
        );
        navigator.navigateToCallsBackupActivity(this);
    }

    @Override
    public void viewSms() {
        createFirebaseEvent(
                mFirebaseAnalytics,
                "SMS",
                "SMS button",
                MENU_BUTTON_CONTENT_TYPE,
                FirebaseAnalytics.Event.SELECT_CONTENT
        );
        navigator.navigateToSmsBackupActivity(this);
    }

    @Override
    public void viewDocuments() {
        createFirebaseEvent(
                mFirebaseAnalytics,
                "Documents",
                "Documents button",
                MENU_BUTTON_CONTENT_TYPE,
                FirebaseAnalytics.Event.SELECT_CONTENT
        );
        navigator.navigateToFileListActivity(this, BaseConstants.DOCUMENTS_REMOTE_FOLDER);
    }

    @Override
    public void viewSettings() {
        createFirebaseEvent(
                mFirebaseAnalytics,
                "Settings",
                "Settings button",
                MENU_BUTTON_CONTENT_TYPE,
                FirebaseAnalytics.Event.SELECT_CONTENT
        );
        replaceFragment(R.id.fl_fragment, SettingsFragment.newInstance(), true, true);
    }

    @Override
    public void viewSync() {
        createFirebaseEvent(
                mFirebaseAnalytics,
                "Sync",
                "Sync button",
                MENU_BUTTON_CONTENT_TYPE,
                FirebaseAnalytics.Event.SELECT_CONTENT
        );
        replaceFragment(R.id.fl_fragment, SyncFragment.newInstance(), true, true);
    }

    @Override
    public void onMainBackPressed() {
        finish();
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().post(new OnBackPress());
    }

    @Override
    public void onBackSettingsClicked() {
        replaceFragment(R.id.fl_fragment, MainFragment.newInstance(), false, false);
    }

    @Override
    public void onBackSyncClicked() {
        replaceFragment(R.id.fl_fragment, MainFragment.newInstance(), false, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        ArbitraryDataProvider arbitraryDataProvider = new ArbitraryDataProvider(getContentResolver());
        Account currentAccount = AccountUtils.getCurrentOwnCloudAccount(this);
        if (requestCode == PermissionUtil.PERMISSIONS_MULTIPLE) {
            if (permissions.length > 0 && grantResults.length > 0 && currentAccount != null) {
                for (int i = 0; i < permissions.length; i++) {
                    // permission was granted
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        switch (permissions[i]) {
                            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    MediaContentJob.scheduleJob(this);
                                    WifiRetryJob.scheduleJob(this);
                                    arbitraryDataProvider.storeOrUpdateKeyValue(currentAccount, FileListFragment.PREFERENCE_FILES_AUTOMATIC_BACKUP, String.valueOf(true));
                                }
                                break;
                            case Manifest.permission.READ_CONTACTS:
                                arbitraryDataProvider.storeOrUpdateKeyValue(currentAccount, ContactsBackupFragment.PREFERENCE_CONTACTS_AUTOMATIC_BACKUP, String.valueOf(true));
                                ContactsBackupFragment.startContactBackupJob(currentAccount);
                                break;
                            case Manifest.permission.READ_SMS:
                                arbitraryDataProvider.storeOrUpdateKeyValue(currentAccount, SmsBackupFragment.PREFERENCE_SMS_AUTOMATIC_BACKUP, String.valueOf(true));
                                SmsBackupFragment.startSmsBackupJob(currentAccount);
                                break;
                            case Manifest.permission.READ_CALL_LOG:
                                arbitraryDataProvider.storeOrUpdateKeyValue(currentAccount, CallsBackupFragment.PREFERENCE_CALLS_AUTOMATIC_BACKUP, String.valueOf(true));
                                CallsBackupFragment.startCallBackupJob(currentAccount);
                                break;
                        }
                    }
                }
            }
        } else if (requestCode == PermissionUtil.PERMISSIONS_WRITE_EXTERNAL_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                MediaContentJob.scheduleJob(this);
                WifiRetryJob.scheduleJob(this);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
