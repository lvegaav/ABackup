package com.americavoice.backup.main.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.webkit.MimeTypeMap;

import com.americavoice.backup.R;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.di.components.DaggerAppComponent;
import com.americavoice.backup.explorer.Const;
import com.americavoice.backup.explorer.ui.FileListFragment;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.MainFragment;
import com.americavoice.backup.settings.ui.SettingsFragment;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.DownloadRemoteFileOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends BaseActivity implements HasComponent<AppComponent>,
        MainFragment.Listener,
        FileListFragment.Listener,
        SettingsFragment.Listener {

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
        replaceFragment(R.id.fl_fragment, FileListFragment.newInstance(Const.Contacts), true, true);
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
        File downFolder = new File(getExternalCacheDir(), getString(R.string.download_folder_path) + "/" + remoteFile.getRemotePath());
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
}
