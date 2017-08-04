
package com.americavoice.backup.explorer.presenter;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;

import com.americavoice.backup.R;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.explorer.ui.FileListView;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.exception.ErrorBundle;
import com.americavoice.backup.main.exception.ErrorMessageFactory;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
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

    private FileListView mView;
    private Handler mHandler;
    private String mPath;
    private RemoteFile mRemoteFile;
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

        mView.showLoading();

        File upFile = new File(path);
        if (!upFile.exists()) {
            mView.hideLoading();
        }

        //Get file extension and mime type
        Uri selectedUri = Uri.fromFile(upFile.getAbsoluteFile());
        String fileExtension =  MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);

        // Get the last modification date of the file from the file system
        Long timeStampLong = upFile.lastModified() / 1000;
        String timeStamp = timeStampLong.toString();


        UploadRemoteFileOperation uploadOperation =
                new UploadRemoteFileOperation(upFile.getAbsolutePath(), mPath + FileUtils.PATH_SEPARATOR + upFile.getName(), mimeType, timeStamp);
        uploadOperation.execute(mNetworkProvider.getCloudClient(getPhoneNumber()), this, mHandler);

    }
    /**
     * Initializes the presenter
     */
    public void initialize(String path) {
        mPath = path;
        mHandler = new Handler();
        mView.showLoading();
        ReadRemoteFolderOperation refreshOperation = new ReadRemoteFolderOperation(path);
        refreshOperation.execute(mNetworkProvider.getCloudClient(getPhoneNumber()), this, mHandler);
    }

    public void onFileClicked(Context context, RemoteFile remoteFile) {
        if (mRemoteFile != null) return;

        if ("DIR".equals(remoteFile.getMimeType())) {
            mView.viewFolder(remoteFile.getRemotePath());
        } else {
            //Check cache
            File downFile = new File(context.getExternalCacheDir(), context.getString(R.string.download_folder_path) + "/" + remoteFile.getRemotePath());
            if (downFile.exists()) {
                mView.viewDetail(remoteFile);
                return;
            }

            mRemoteFile = remoteFile;
            mView.showLoading();
            File downFolder = new File(context.getExternalCacheDir(), context.getString(R.string.download_folder_path));
            if (!downFolder.exists()) {
                downFolder.mkdir();
            }
            DownloadRemoteFileOperation downloadOperation = new DownloadRemoteFileOperation(remoteFile.getRemotePath(), downFolder.getAbsolutePath());
            downloadOperation.execute(mNetworkProvider.getCloudClient(getPhoneNumber()), this, mHandler);
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
        if (!result.isSuccess()) {

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
        initialize(mPath);
    }

    private void onSuccessfulDownload(DownloadRemoteFileOperation operation, RemoteOperationResult result) {
        mView.viewDetail(mRemoteFile);
        mRemoteFile = null;
    }

    private void onSuccessfulRefresh(ReadRemoteFolderOperation operation, RemoteOperationResult result) {
        List<RemoteFile> files = new ArrayList<>();
        if (result.getData() == null) {
            mView.renderEmpty("No hay informacion");
            return;
        }
        for(Object obj: result.getData()) {
            RemoteFile remoteFile = (RemoteFile) obj;
            if (mPath.equals(remoteFile.getRemotePath())) continue;
            files.add(remoteFile);
        }
        mView.renderList(files);
    }
}
