
package com.americavoice.backup.sync.presenter;

import android.accounts.Account;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import com.americavoice.backup.Const;
import com.americavoice.backup.R;
import com.americavoice.backup.datamodel.FileDataStorageManager;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.files.service.FileUploader;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.exception.ErrorBundle;
import com.americavoice.backup.main.exception.ErrorMessageFactory;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.operations.UploadFileOperation;
import com.americavoice.backup.sync.ui.SyncView;
import com.americavoice.backup.utils.FileStorageUtils;
import com.americavoice.backup.utils.MimeType;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;


/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class SyncPresenter extends BasePresenter implements IPresenter, OnRemoteOperationListener {

    private SyncView mView;
    private Context mContext;
    private Handler mHandler;
    private FileDataStorageManager mStorageManager;
    private Account mAccount;
    private List<String> mPendingPhotos;
    private List<String> mPendingVideos;

    private ReadRemoteFolderOperation mDownloadPhotosOperation;
    private ReadRemoteFolderOperation mDownloadVideosOperation;
    @Inject
    SyncPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull SyncView view) {
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

    /**
     * Initializes the presenter
     */
    public void initialize(Context context, Account account) {
        mContext = context;
        mHandler = new Handler();
        mAccount = account;
        mStorageManager = new FileDataStorageManager(account, context);
        mView.showLoading();

        mDownloadPhotosOperation = new ReadRemoteFolderOperation("/Photos/");
        mDownloadPhotosOperation.execute(mNetworkProvider.getCloudClient(getPhoneNumber()), this, mHandler);

        mDownloadVideosOperation = new ReadRemoteFolderOperation("/Videos/");
        mDownloadVideosOperation.execute(mNetworkProvider.getCloudClient(getPhoneNumber()), this, mHandler);

        mSharedPrefsUtils.setBooleanPreference(NetworkProvider.KEY_FIRST_TIME, false);
    }

    public void sync() {
        mView.syncJob(mPendingPhotos,mPendingVideos);
    }

    private void showErrorMessage(ErrorBundle errorBundle) {
        String errorMessage = ErrorMessageFactory.create(this.mView.getContext(),
                errorBundle.getException());
        this.mView.showError(errorMessage);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation remoteOperation, RemoteOperationResult remoteOperationResult) {
        mView.hideLoading();
        ReadRemoteFolderOperation operation = (ReadRemoteFolderOperation) remoteOperation;
        Map<String, RemoteFile> remoteFilesMap = new HashMap<>();
        List<String> pendingFiles = new ArrayList<>();
        List<String> localFiles = new ArrayList<>();
        boolean isPhotos = mDownloadPhotosOperation.equals(operation);

        if (remoteOperationResult.isSuccess()) {
            for(Object obj: remoteOperationResult.getData()) {
                RemoteFile remoteFile = (RemoteFile) obj;
                if (remoteFile.getMimeType() != null && !remoteFile.getMimeType().equals(MimeType.DIRECTORY))
                {
                    remoteFilesMap.put(getFileName(remoteFile.getRemotePath()),remoteFile);
                }
            }
            localFiles = isPhotos? getAllShownImagesPath(mContext) : getAllShownVideosPath(mContext);

            for (String item : localFiles) {
                if (!remoteFilesMap.containsKey(getFileName(item))) {
                    pendingFiles.add(item);
                }
            }

            if (isPhotos) {
                mPendingPhotos = pendingFiles;
                mView.totalImages(pendingFiles.size());
            } else {
                mPendingVideos = pendingFiles;
                mView.totalVideos(pendingFiles.size());
            }

        }
    }

    private ArrayList<String> getAllShownImagesPath(Context context) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = context.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }

    private ArrayList<String> getAllShownVideosPath(Context context) {
        int int_position = 0;
        Uri uri;
        Cursor cursor;
        ArrayList<String> listOfAllVideos = new ArrayList<>();
        int column_index_data, column_index_folder_name, column_id, thum;
        String absolutePathOfImage = null;
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media._ID, MediaStore.Video.Thumbnails.DATA};

        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        cursor = context.getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
        column_id = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
        thum = cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA);

        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            listOfAllVideos.add(absolutePathOfImage);
        }

        return listOfAllVideos;
    }

    private String getFileName(String path)
    {
        return path.substring(path.lastIndexOf('/') + 1);
    }


}
