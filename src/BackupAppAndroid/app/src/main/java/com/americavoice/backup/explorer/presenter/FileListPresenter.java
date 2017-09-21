
package com.americavoice.backup.explorer.presenter;

import android.accounts.Account;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.calls.ui.CallsBackupFragment;
import com.americavoice.backup.datamodel.ArbitraryDataProvider;
import com.americavoice.backup.datamodel.FileDataStorageManager;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.explorer.Const;
import com.americavoice.backup.explorer.ui.FileListFragment;
import com.americavoice.backup.explorer.ui.FileListView;
import com.americavoice.backup.files.service.FileUploader;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.exception.ErrorBundle;
import com.americavoice.backup.main.exception.ErrorMessageFactory;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.operations.UploadFileOperation;
import com.americavoice.backup.utils.FileStorageUtils;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.files.DownloadRemoteFileOperation;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;
import com.owncloud.android.lib.resources.files.UploadRemoteFileOperation;

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
                FileUploader.LOCAL_BEHAVIOUR_MOVE,
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
        mView.showLoading();
        mAccount = account;
        mStorageManager = new FileDataStorageManager(account, context);

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
                return;
            }

            mView.downloadFile(remoteFile);
        }
    }

    private void showErrorMessage(ErrorBundle errorBundle) {
        String errorMessage = ErrorMessageFactory.create(this.mView.getContext(),
                errorBundle.getException());
        this.mView.showError(errorMessage);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
        mView.hideLoading();
        mView.hideDLoading();

        if (!result.isSuccess()) {
            mView.showRetry();
        } else if (operation instanceof ReadRemoteFolderOperation) {
            onSuccessfulRefresh((ReadRemoteFolderOperation)operation, result);
        } else if (operation instanceof DownloadRemoteFileOperation) {
            onSuccessfulDownload((DownloadRemoteFileOperation)operation, result);
        }  else if (operation instanceof UploadRemoteFileOperation) {
            onSuccessfulUpload((UploadRemoteFileOperation)operation, result);
        }

    }

    private void onSuccessfulUpload(UploadRemoteFileOperation operation, RemoteOperationResult result) {
        mView.hideLoading();
        mView.hideDLoading();
        initialize(mContext, mPath, mAccount);
    }

    private void onSuccessfulDownload(DownloadRemoteFileOperation operation, RemoteOperationResult result) {
        mView.viewDetail(mRemoteFile);
        mView.notifyDataSetChanged();
        mRemoteFile = null;
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
            case Const.Documents:
                arbitraryDataProvider.storeOrUpdateKeyValue(account,
                        FileListFragment.PREFERENCE_DOCUMENTS_LAST_TOTAL,
                        String.valueOf(size));
                break;
            case Const.Photos:
                arbitraryDataProvider.storeOrUpdateKeyValue(account,
                        FileListFragment.PREFERENCE_PHOTOS_LAST_TOTAL,
                        String.valueOf(size));
                break;
            case Const.Videos:
                arbitraryDataProvider.storeOrUpdateKeyValue(account,
                        FileListFragment.PREFERENCE_VIDEOS_LAST_TOTAL,
                        String.valueOf(size));
                break;
            default:
                break;
        }
    }
}
