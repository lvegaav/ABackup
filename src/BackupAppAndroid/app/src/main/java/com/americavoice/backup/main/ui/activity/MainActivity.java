package com.americavoice.backup.main.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.calls.ui.CallsBackupFragment;
import com.americavoice.backup.contacts.ui.ContactsBackupFragment;
import com.americavoice.backup.datamodel.ArbitraryDataProvider;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.di.components.DaggerAppComponent;
import com.americavoice.backup.explorer.Const;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.MainFragment;
import com.americavoice.backup.settings.ui.SettingsFragment;
import com.americavoice.backup.sms.ui.SmsBackupFragment;
import com.americavoice.backup.sync.ui.SyncFragment;
import com.americavoice.backup.utils.PermissionUtil;
import com.americavoice.backup.utils.ThemeUtils;
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

        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();

        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add(getString(R.string.common_write_external_storage));
        if (!addPermission(permissionsList, Manifest.permission.READ_CONTACTS))
            permissionsNeeded.add(getString(R.string.common_read_contacts));
        if (!addPermission(permissionsList, Manifest.permission.READ_SMS))
            permissionsNeeded.add(getString(R.string.common_read_sms));
        if (!addPermission(permissionsList, Manifest.permission.READ_CALL_LOG))
            permissionsNeeded.add(getString(R.string.common_read_call_log));

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PermissionUtil.requestMultiplePermission(MainActivity.this, permissionsList.toArray(new String[permissionsList.size()]));
                            }
                        });
                return;
            }
            PermissionUtil.requestMultiplePermission(this, permissionsList.toArray(new String[permissionsList.size()]));
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (!PermissionUtil.checkSelfPermission(this, permission)) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (PermissionUtil.shouldShowRequestPermissionRationale(this, permission))
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        ArbitraryDataProvider arbitraryDataProvider = new ArbitraryDataProvider(getContentResolver());
        if (requestCode == PermissionUtil.PERMISSIONS_MULTIPLE) {
            if (permissions.length > 0 && grantResults.length > 0 && mAccountWasSet) {
                for (int i = 0; i < permissions.length; i++) {
                    // permission was granted
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        switch (permissions[i]) {
                            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    schedulePhotos();
                                    scheduleWifiJob();
                                }
                                break;
                            case Manifest.permission.READ_CONTACTS:
                                arbitraryDataProvider.storeOrUpdateKeyValue(mCurrentAccount, ContactsBackupFragment.PREFERENCE_CONTACTS_AUTOMATIC_BACKUP, String.valueOf(true));
                                ContactsBackupFragment.startContactBackupJob(mCurrentAccount);
                                break;
                            case Manifest.permission.READ_SMS:
                                arbitraryDataProvider.storeOrUpdateKeyValue(mCurrentAccount, SmsBackupFragment.PREFERENCE_SMS_AUTOMATIC_BACKUP, String.valueOf(true));
                                SmsBackupFragment.startSmsBackupJob(mCurrentAccount);
                                break;
                            case Manifest.permission.READ_CALL_LOG:
                                arbitraryDataProvider.storeOrUpdateKeyValue(mCurrentAccount, CallsBackupFragment.PREFERENCE_CALLS_AUTOMATIC_BACKUP, String.valueOf(true));
                                CallsBackupFragment.startCallBackupJob(mCurrentAccount);
                                break;
                        }
                    }
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
        navigator.navigateToFileListActivity(this, Const.Photos);
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
        navigator.navigateToFileListActivity(this, Const.Videos);
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
        navigator.navigateToFileListActivity(this, Const.Documents);
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
    public void onRestoreClicked() {
        replaceFragment(R.id.fl_fragment, SyncFragment.newInstance(), true, true);
    }

    @Override
    public void onBackConfirmationClicked() {
        replaceFragment(R.id.fl_fragment, MainFragment.newInstance(), false, false);
    }
}
