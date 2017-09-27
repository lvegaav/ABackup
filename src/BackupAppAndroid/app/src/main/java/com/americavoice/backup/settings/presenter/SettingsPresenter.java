
package com.americavoice.backup.settings.presenter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.settings.ui.SettingsView;
import com.americavoice.backup.utils.BaseConstants;
import com.americavoice.backup.utils.MimeType;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;

import java.lang.reflect.Field;
import java.math.BigDecimal;
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
public class SettingsPresenter extends BasePresenter implements IPresenter, OnRemoteOperationListener {

    private SettingsView mView;
    private Handler mHandler;

    private Map<String, RemoteFile> mRemotePhotosMap = new HashMap<>();
    private Map<String, RemoteFile> mRemoteVideosMap = new HashMap<>();
    private ReadRemoteFolderOperation mReadRemoteOperation;
    private ReadRemoteFolderOperation mReadRemotePhotosOperation;
    private ReadRemoteFolderOperation mReadRemoteVideosOperation;
    private List<String> mPendingPhotos = new ArrayList<>();
    private List<String> mPendingVideos = new ArrayList<>();

    private boolean mPhotosRemoteDone;
    private boolean mVideosRemoteDone;

    @Inject
    public SettingsPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
        mHandler = new Handler();
    }

    public void setView(@NonNull SettingsView view) {
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

    public void showSyncAtFirst() {
        if (mSharedPrefsUtils.getBooleanPreference(NetworkProvider.KEY_FIRST_TIME, false)) {
            getPendingFiles();
        }
    }
    /**
     * Initializes the presenter
     */
    public void initialize() {
        mView.showLoading();
        mReadRemoteOperation = new ReadRemoteFolderOperation("/");
        mReadRemoteOperation.execute(mNetworkProvider.getCloudClient(getPhoneNumber()), this, mHandler);
    }

    public void logout() {
        mNetworkProvider.logout();
        mSharedPrefsUtils.setStringPreference(NetworkProvider.KEY_PHONE_NUMBER, null);
    }

    public void scheduleSync() {
        mView.scheduleSyncJob(mPendingPhotos, mPendingVideos);
    }

    public void getPendingFiles() {
        mView.showGettingPending();
        mPhotosRemoteDone = false;
        mVideosRemoteDone = false;

        mReadRemotePhotosOperation = new ReadRemoteFolderOperation(BaseConstants.PHOTOS_REMOTE_FOLDER);
        mReadRemotePhotosOperation.execute(mNetworkProvider.getCloudClient(getPhoneNumber()), this, mHandler);

        mReadRemoteVideosOperation = new ReadRemoteFolderOperation(BaseConstants.VIDEOS_REMOTE_FOLDER);
        mReadRemoteVideosOperation.execute(mNetworkProvider.getCloudClient(getPhoneNumber()), this, mHandler);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation remoteOperation, RemoteOperationResult result) {
        boolean isPhotos = remoteOperation.equals(mReadRemotePhotosOperation);
        boolean isVideos = remoteOperation.equals(mReadRemoteVideosOperation);
        boolean isRefresh = remoteOperation.equals(mReadRemoteOperation);
        if (result.getData() == null) {
            mView.hideLoading();
            mView.showDefaultError();
            if (isPhotos) mPhotosRemoteDone = true;
            if (isVideos) mVideosRemoteDone = true;
            return;
        }
        List<String> localFiles;
        if ( isPhotos || isVideos ) {
            processRemoteFiles(result.getData(), isPhotos, isVideos);

            localFiles = isPhotos ? getAllShownImagesPath(mView.getContext()) : getAllShownVideosPath(mView.getContext());

            for (String item : localFiles) {
                if (isPhotos) {
                    if (!mRemotePhotosMap.containsKey(getFileName(item))) {
                        mPendingPhotos.add(item);
                    }
                } else {
                    if (!mRemoteVideosMap.containsKey(getFileName(item))) {
                        mPendingVideos.add(item);
                    }
                }
            }
            if (isPhotos) mPhotosRemoteDone = true;
            if (isVideos) mVideosRemoteDone = true;

            if (mVideosRemoteDone && mPhotosRemoteDone) {
                mView.hideLoading();
                mView.showSyncDialog(mPendingPhotos.size(), mPendingVideos.size());
            }
        } else if (isRefresh) {
            HashMap<String, BigDecimal> mSizes = new HashMap<>();
            BigDecimal total = new BigDecimal(0);
            BigDecimal totalAvailable = new BigDecimal(0);
            for(Object obj: result.getData()) {

                RemoteFile remoteFile = (RemoteFile) obj;

                if (remoteFile.getRemotePath().equals("/")) {
                    try {
                        Field field =RemoteFile.class.getDeclaredField("mQuotaAvailableBytes");
                        field.setAccessible(true);
                        total = (BigDecimal) field.get(remoteFile);
                        totalAvailable = (BigDecimal) field.get(remoteFile);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                if (remoteFile.getRemotePath().equals(BaseConstants.DOCUMENTS_REMOTE_FOLDER)
                        || remoteFile.getRemotePath().equals(BaseConstants.PHOTOS_REMOTE_FOLDER)
                        || remoteFile.getRemotePath().equals(BaseConstants.VIDEOS_REMOTE_FOLDER)
                        || remoteFile.getRemotePath().equals(BaseConstants.CONTACTS_REMOTE_FOLDER)
                        || remoteFile.getRemotePath().equals(BaseConstants.CALLS_REMOTE_FOLDER)
                        || remoteFile.getRemotePath().equals(BaseConstants.SMS_REMOTE_FOLDER)) {

                    BigDecimal size = BigDecimal.valueOf(remoteFile.getSize());
                    total = total.add(size);
                    mSizes.put(remoteFile.getRemotePath(), size);
                }
            }
            mView.showPercent(mSizes, total, totalAvailable);
            mView.hideLoading();
        }
    }

    private void processRemoteFiles(ArrayList<Object> resultData, boolean isPhoto, boolean isVideo) {
        for(Object obj: resultData) {
            RemoteFile remoteFile = (RemoteFile) obj;
            if (remoteFile.getMimeType() != null && !remoteFile.getMimeType().equals(MimeType.DIRECTORY)){
                if (isPhoto) {
                    mRemotePhotosMap.put(getFileName(remoteFile.getRemotePath()), remoteFile);
                } else if (isVideo){
                    mRemoteVideosMap.put(getFileName(remoteFile.getRemotePath()), remoteFile);
                }
            }
        }
    }

    private static ArrayList<String> getAllShownImagesPath(Context context) {
        Uri uri;
        Cursor cursor;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = context.getContentResolver().query(uri, projection, null,
                null, null);
        if (cursor != null) {
            int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            while (cursor.moveToNext()) {
                // Getting the absolute path of the image.
                listOfAllImages.add(cursor.getString(column_index_data));
            }
            cursor.close();
        }

        return listOfAllImages;
    }

    private ArrayList<String> getAllShownVideosPath(Context context) {
        Uri uri;
        Cursor cursor;
        ArrayList<String> listOfAllVideos = new ArrayList<>();
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media._ID, MediaStore.Video.Thumbnails.DATA};

        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        cursor = context.getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        if (cursor != null) {
            int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            while (cursor.moveToNext()) {
                // Getting the absolute path of the video.
                listOfAllVideos.add(cursor.getString(column_index_data));
            }

            cursor.close();
        }

        return listOfAllVideos;
    }

    private String getFileName(String path)
    {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public void setFirstTimeFalse() {
        mSharedPrefsUtils.setBooleanPreference(NetworkProvider.KEY_FIRST_TIME, false);
    }
}
