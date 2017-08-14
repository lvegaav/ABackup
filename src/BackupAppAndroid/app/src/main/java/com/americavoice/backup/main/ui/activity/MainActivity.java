package com.americavoice.backup.main.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.americavoice.backup.R;
import com.americavoice.backup.contacts.ContactsBackupFragment;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.di.components.DaggerAppComponent;
import com.americavoice.backup.explorer.Const;
import com.americavoice.backup.explorer.ui.FileListFragment;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.MainFragment;
import com.americavoice.backup.settings.ui.SettingsFragment;
import com.americavoice.backup.utils.PermissionUtil;
import com.americavoice.backup.utils.ThemeUtils;
import com.owncloud.android.lib.resources.files.RemoteFile;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.ButterKnife;


public class MainActivity extends BaseOwncloudActivity implements HasComponent<AppComponent>,
        MainFragment.Listener,
        FileListFragment.Listener,
        SettingsFragment.Listener,
        ContactsBackupFragment.Listener {

    private AppComponent mAppComponent;
    private  RemoteFile mTempRemoteFile;
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
        replaceFragment(R.id.fl_fragment, FileListFragment.newInstance(Const.Photos), true, true);
    }

    @Override
    public void viewVideos() {
        replaceFragment(R.id.fl_fragment, FileListFragment.newInstance(Const.Videos), true, true);
    }

    @Override
    public void viewContacts() {
        replaceFragment(R.id.fl_fragment, ContactsBackupFragment.newInstance(), true, true);
    }

    @Override
    public void viewDocuments() {
        replaceFragment(R.id.fl_fragment, FileListFragment.newInstance(Const.Documents), true, true);
    }

    @Override
    public void viewSettings() {
        replaceFragment(R.id.fl_fragment, SettingsFragment.newInstance(), true, true);
    }

    @Override
    public void onMainBackPressed() {
        finish();
    }

    @Override
    public void onFileClicked(RemoteFile remoteFile) {
        //File downFolder = new File(getExternalCacheDir(), getString(R.string.download_folder_path) + "/" + mTempRemoteFile.getRemotePath().substring(0, mTempRemoteFile.getRemotePath().lastIndexOf('/') - 1));
        File downFolder = new File(getExternalCacheDir(), getString(R.string.files_download_folder_path) + "/" + remoteFile.getRemotePath());
        //Get file extension and mime type
        Uri selectedUri = Uri.fromFile(downFolder.getAbsoluteFile());
        String fileExtension =  MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        //Start Activity to view the selected file
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, mimeType);
        startActivity(Intent.createChooser(intent, "Open File..."));
    }

    @Override
    public void onFolderClicked(String path) {
        if (path == null) {
            replaceFragment(R.id.fl_fragment, MainFragment.newInstance(), false, false);
        } else {
            replaceFragment(R.id.fl_fragment, FileListFragment.newInstance(path), true, true);
        }
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
    public void onContactsBackPressed() {
        replaceFragment(R.id.fl_fragment, MainFragment.newInstance(), false, false);
    }
}
