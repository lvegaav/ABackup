
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
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
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

    private FileDataStorageManager mStorageManager;
    private Account mAccount;
    private FileListView mView;
    private Handler mHandler;
    private String mPath;
    private OCFile mRemoteFile;
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
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
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
    /**
     * Initializes the presenter
     */
    public void initialize(Context context, String path, Account account) {
        mContext = context;
        mPath = path;
        mHandler = new Handler();
        mAccount = account;
        mStorageManager = new FileDataStorageManager(account, context);
        if (mSharedPrefsUtils.getBooleanPreference(BaseConstants.PreferenceKeys.STORAGE_FULL, false)) {
            mView.showPersistenceUpgrade(R.string.common_cloud_storage_full);
        } else if (mSharedPrefsUtils.getBooleanPreference(FileListFragment.PREFERENCE_STORAGE_ALMOST_FULL, false)){
            mView.showPersistenceUpgrade(R.string.files_cloud_almost_full);
        }
        ReadRemoteFolderOperation refreshOperation = new ReadRemoteFolderOperation(path);
        refreshOperation.execute(mNetworkProvider.getCloudClient(getPhoneNumber()), this, mHandler);
    }

    public void onFileClicked(Context context, OCFile remoteFile) {
        if (mRemoteFile != null) return;

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
        if (!result.isSuccess()) {
            mView.showRetry();
        } else if (operation instanceof ReadRemoteFolderOperation) {
            onSuccessfulRefresh((ReadRemoteFolderOperation) operation, result);
        }

    }

    public void onSuccessfulDownload() {
        if (mRemoteFile != null) {
            mView.viewDetail(mRemoteFile);
            mRemoteFile = null;
        }
    }

    private void onSuccessfulRefresh(ReadRemoteFolderOperation operation, RemoteOperationResult result) {
        if (result.isSuccess()) {
            List<OCFile> files = new ArrayList<>();
            for(Object obj: result.getData()) {
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
            if (files.size() > 0) {
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
                        FileListFragment.PREFERENCE_DOCUMENTS_LAST_TOTAL,
                        String.valueOf(size));
                break;
            case BaseConstants.PHOTOS_REMOTE_FOLDER:
                arbitraryDataProvider.storeOrUpdateKeyValue(account,
                        FileListFragment.PREFERENCE_PHOTOS_LAST_TOTAL,
                        String.valueOf(size));
                break;
            case BaseConstants.VIDEOS_REMOTE_FOLDER:
                arbitraryDataProvider.storeOrUpdateKeyValue(account,
                        FileListFragment.PREFERENCE_VIDEOS_LAST_TOTAL,
                        String.valueOf(size));
                break;
            default:
                break;
        }
    }

    public void updateRefreshFlag() {
        mSharedPrefsUtils.setBooleanPreference(MainActivity.EXTRA_REFRESH_DATA, true);
    }
}
