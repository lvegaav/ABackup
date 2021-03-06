
package com.americavoice.backup.explorer.presenter;

import android.accounts.Account;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.datamodel.ArbitraryDataProvider;
import com.americavoice.backup.datamodel.FileDataStorageManager;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.explorer.ui.FileListFragment;
import com.americavoice.backup.explorer.ui.FileListView;
import com.americavoice.backup.files.service.FileUploader;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.main.ui.activity.MainActivity;
import com.americavoice.backup.operations.UploadFileOperation;
import com.americavoice.backup.utils.BaseConstants;
import com.americavoice.backup.utils.FileStorageUtils;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class FileListPresenter extends BasePresenter implements IPresenter, OnRemoteOperationListener {

    private static final String SHOW_CASE_ALREADY = "FILES_SHOW_CASE_ALREADY";
    private FileDataStorageManager mStorageManager;
    private FileListView mView;
    private Handler mHandler;
    private String mPath;
    private Context mContext;

    @Inject
    public FileListPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull FileListView view) {
        this.mView = view;
    }

    @Override
    public void resume() {
        // no-op
    }

    @Override
    public void pause() {
        // no-op
    }

    @Override
    public void destroy() {
        // no-op
    }

    public void onFileUpload(String path) {
        mView.hideRetry();
        mView.showUploading();

        File upFile = new File(path);

        Account account = AccountUtils.getCurrentOwnCloudAccount(mContext);

        FileUploader.UploadRequester requester = new FileUploader.UploadRequester();
        requester.uploadNewFile(
          mContext,
          account,
          upFile.getAbsolutePath(),
          mPath + FileUtils.PATH_SEPARATOR + upFile.getName(),
          FileUploader.LOCAL_BEHAVIOUR_FORGET,
          null,
          true,
          UploadFileOperation.CREATED_BY_USER
        );
    }

    public void onFileListUpload(ArrayList<String> pathsList, ArrayList<String> namesList) {
        mView.hideRetry();
        mView.showUploading();

        String[] paths = pathsList.toArray(new String[pathsList.size()]);
        String[] names = new String[namesList.size()];

        int i = 0;
        for (String name : namesList) {
            names[i] = mPath + FileUtils.PATH_SEPARATOR + name;
            i++;
        }

        Account account = AccountUtils.getCurrentOwnCloudAccount(mContext);

        FileUploader.UploadRequester requester = new FileUploader.UploadRequester();
        requester.uploadNewFile(
          mContext,
          account,
          paths,
          names,
          null,
          FileUploader.LOCAL_BEHAVIOUR_FORGET,
          true,
          UploadFileOperation.CREATED_BY_USER
        );
    }

    /**
     * Initializes the presenter
     */
    public void initialize(Context context, String path, Account account) {
        mContext = context;
        mPath = path;
        mHandler = new Handler();
        mStorageManager = new FileDataStorageManager(account, context);
        if (! mSharedPrefsUtils.getBooleanPreference(SHOW_CASE_ALREADY, false)) {
            mView.showGuidedTour();
        } else if (mSharedPrefsUtils.getBooleanPreference(BaseConstants.PreferenceKeys.STORAGE_FULL, false)) {
            mView.showPersistenceUpgrade(R.string.common_cloud_storage_full);
        } else if (mSharedPrefsUtils.getBooleanPreference(FileListFragment.PREFERENCE_STORAGE_ALMOST_FULL, false)) {
            mView.showPersistenceUpgrade(R.string.files_cloud_almost_full);
        }
        readRemoteFiles(path);
    }

    public void readRemoteFiles(String path) {
        OwnCloudClient client = mNetworkProvider.getCloudClient();
        if (client != null) {
            ReadRemoteFolderOperation refreshOperation = new ReadRemoteFolderOperation(path);
            refreshOperation.execute(client, this, mHandler);
        }
    }

    public void onFileClicked(OCFile remoteFile) {
        if (remoteFile.isFolder()) {
            mView.viewFolder(remoteFile.getRemotePath());
        } else {
            //Check cache
            if (remoteFile.isDown()) {
                mView.viewDetail(remoteFile);
            } else {
                mView.downloadFile(remoteFile);
            }
        }
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
        mView.hideLoading();
        if (! result.isSuccess()) {
            if (result.getHttpCode() == 404) {
                mView.showLoading();
                CreateRemoteFolderOperation createRemoteFolderOperation = new CreateRemoteFolderOperation(mPath, true);
                OwnCloudClient client = mNetworkProvider.getCloudClient();
                createRemoteFolderOperation.execute(client, this, mHandler);
            } else {
                mView.showRetry();
            }
        } else if (operation instanceof ReadRemoteFolderOperation) {
            onSuccessfulRefresh(result);
        } else if (operation instanceof CreateRemoteFolderOperation) {
            mView.showLoading();
            readRemoteFiles(mPath);
        }

    }

    public void onSuccessfulDownload(String remotePath) {
        OCFile remoteFile = new OCFile(remotePath);
        mView.viewDetail(remoteFile);

    }

    private void onSuccessfulRefresh(RemoteOperationResult result) {
        if (result.isSuccess()) {
            List<OCFile> files = new ArrayList<>();
            for (Object obj : result.getData()) {
                RemoteFile remoteFile = (RemoteFile) obj;
                OCFile file = mStorageManager.getFileByPath(remoteFile.getRemotePath());
                if (file == null) {
                    file = FileStorageUtils.fillOCFile(remoteFile);
                    mStorageManager.saveFile(file);
                }
                if (mPath.equals(remoteFile.getRemotePath()))
                    continue;
                files.add(file);
            }
            refreshTotal(files.size());
            if (! files.isEmpty()) {
                mView.renderList(files);
            } else {
                mView.renderEmpty();
            }
        } else {
            mView.renderEmpty();
        }
    }

    public void refreshTotal(int size) {
        // store total
        final Account account = AccountUtils.getCurrentOwnCloudAccount(mContext);


        ArbitraryDataProvider arbitraryDataProvider = new ArbitraryDataProvider(mContext.getContentResolver());
        switch (mPath) {
            case BaseConstants.DOCUMENTS_REMOTE_FOLDER:
                arbitraryDataProvider.storeOrUpdateKeyValue(account,
                  FileListFragment.PREFERENCE_DOCUMENTS_LAST_TOTAL + account.name,
                  String.valueOf(size));
                break;
            case BaseConstants.PHOTOS_REMOTE_FOLDER:
                arbitraryDataProvider.storeOrUpdateKeyValue(account,
                  FileListFragment.PREFERENCE_PHOTOS_LAST_TOTAL + account.name,
                  String.valueOf(size));
                break;
            case BaseConstants.VIDEOS_REMOTE_FOLDER:
                arbitraryDataProvider.storeOrUpdateKeyValue(account,
                  FileListFragment.PREFERENCE_VIDEOS_LAST_TOTAL + account.name,
                  String.valueOf(size));
                break;
            case BaseConstants.MUSIC_REMOTE_FOLDER:
                arbitraryDataProvider.storeOrUpdateKeyValue(account,
                  FileListFragment.PREFERENCE_MUSIC_LAST_TOTAL + account.name,
                  String.valueOf(size));
                break;
            default:
                break;
        }
    }

    public void updateRefreshFlag() {
        mSharedPrefsUtils.setBooleanPreference(MainActivity.EXTRA_REFRESH_DATA, true);
    }

    public void showCaseFinished() {
        mSharedPrefsUtils.setBooleanPreference(SHOW_CASE_ALREADY, true);
    }
}
