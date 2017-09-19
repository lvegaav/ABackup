package com.americavoice.backup.main.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.americavoice.backup.R;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.di.components.DaggerAppComponent;
import com.americavoice.backup.explorer.Const;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.MainFragment;
import com.americavoice.backup.sync.ui.SyncFragment;
import com.americavoice.backup.settings.ui.SettingsFragment;
import com.americavoice.backup.utils.PermissionUtil;
import com.americavoice.backup.utils.ThemeUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;


public class MainActivity extends BaseOwncloudActivity implements HasComponent<AppComponent>,
        SyncFragment.Listener,
        MainFragment.Listener,
        SettingsFragment.Listener {

    private FirebaseAnalytics mFirebaseAnalytics;
    private AppComponent mAppComponent;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContentView(R.layout.activity_main);
        this.initializeActivity(savedInstanceState);
        this.initializeInjector();
        this.initializeView();
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (!PermissionUtil.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Check if we should show an explanation
            if (PermissionUtil.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show explanation to the user and then request permission
                Snackbar snackbar = Snackbar.make(findViewById(R.id.fl_fragment), R.string.files_permission_storage_access,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.common_ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PermissionUtil.requestWriteExternalStoragePermission(MainActivity.this);
                            }
                        });
                ThemeUtils.colorSnackbar(this, snackbar);
                snackbar.show();
            } else {
                // No explanation needed, request the permission.
                PermissionUtil.requestWriteExternalStoragePermission(this);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtil.PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted

                    // toggle on is save since this is the only scenario this code gets accessed
                } else {
                    // permission denied --> do nothing
                    this.finish();
                    return;
                }
                return;
            }
            default:
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
