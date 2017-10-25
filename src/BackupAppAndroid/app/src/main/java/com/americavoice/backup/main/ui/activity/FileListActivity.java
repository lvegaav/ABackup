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
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.americavoice.backup.R;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.di.components.DaggerAppComponent;
import com.americavoice.backup.explorer.ui.FileListFragment;
import com.americavoice.backup.files.utils.FileUtils;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.utils.ComponentsGetter;
import com.americavoice.backup.utils.PermissionUtil;
import com.americavoice.backup.utils.ThemeUtils;
import com.crashlytics.android.Crashlytics;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileListActivity extends FileActivity implements HasComponent<AppComponent>, FileListFragment.Listener, ComponentsGetter {

    public static final String EXTRA_FILE_TYPE = "EXTRA_FILE_TYPE";
    private AppComponent mAppComponent;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;


    public static Intent getCallingIntent(Context context) {
        return new Intent(context, FileListActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        this.initializeInjector();
        this.initializeView();
        this.initializeActivity(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Initializes this activity.
     */
    private void initializeActivity(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            final String typeConstant = getIntent().getStringExtra(EXTRA_FILE_TYPE);
            addFragment(R.id.fl_fragment, FileListFragment.newInstance(typeConstant));
        }
    }

    private void initializeView() {
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
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
                                PermissionUtil.requestWriteExternalStoragePermission(FileListActivity.this);
                            }
                        });
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

    @Override
    public void onFileClicked(OCFile remoteFile) {
        try {
            File file;
            if (remoteFile.getStoragePath() != null){
                file = new File(remoteFile.getStoragePath());
            } else {
                file = new File( FileUtils.EXTERNAL_FILES_PATH + OCFile.PATH_SEPARATOR + remoteFile.getFileName());
            }
            if (file.exists()) {
                Uri selectedUri = Uri.fromFile(file.getAbsoluteFile());
                //Get file extension and mime type
                String fileExtension =  MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                //Start Activity to view the selected file
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(selectedUri, mimeType);
                startActivity(Intent.createChooser(intent, "Open File..."));
            }

        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }

    }

    @Override
    public void onFolderClicked(String path) {
        if (path == null) {
            finish();
        } else {
            replaceFragment(R.id.fl_fragment, FileListFragment.newInstance(path), true, true);
        }
    }

    @Override
    public ActionMode startActivityActionMode(ActionMode.Callback actionMode) {
        mToolbar.setVisibility(View.GONE);
        return startActionMode(actionMode);
    }

    @Override
    public void finishActivityActionMode() {
        mToolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().post(new OnBackPress());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
