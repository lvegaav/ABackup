
package com.americavoice.backup.settings.presenter;

import android.Manifest;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.files.utils.FileUtils;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.settings.ui.SettingsView;
import com.americavoice.backup.settings.ui.StorageInfoView;
import com.americavoice.backup.utils.BaseConstants;
import com.americavoice.backup.utils.MimeType;
import com.americavoice.backup.utils.PermissionUtil;
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
public class StorageInfoPresenter extends BasePresenter implements IPresenter, OnRemoteOperationListener {

    private StorageInfoView mView;
    private Handler mHandler;

    private Map<String, RemoteFile> mRemotePhotosMap = new HashMap<>();
    private Map<String, RemoteFile> mRemoteVideosMap = new HashMap<>();
    private ReadRemoteFolderOperation mReadRemoteOperation;
    private ReadRemoteFolderOperation mReadRemotePhotosOperation;
    private ReadRemoteFolderOperation mReadRemoteVideosOperation;
    private List<String> mPendingPhotos;
    private List<String> mPendingVideos;

    private boolean mPhotosRemoteDone;
    private boolean mVideosRemoteDone;

    @Inject
    public StorageInfoPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
        mHandler = new Handler();
    }

    public void setView(@NonNull StorageInfoView view) {
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
            if (mView != null) {
                if (PermissionUtil.checkSelfPermission(mView.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    getPendingFiles();
                } else {
                    mView.showRequestPermissionDialog();
                }
            }
        }
    }
    /**
     * Initializes the presenter
     */
    public void initialize() {
        mView.showLoading();
        mReadRemoteOperation = new ReadRemoteFolderOperation("/");
        mReadRemoteOperation.execute(mNetworkProvider.getCloudClient(), this, mHandler);
    }

    public void logout() {
        mNetworkProvider.logout();
    }

    public void scheduleSync() {
        mView.scheduleSyncJob(mPendingPhotos, mPendingVideos);
    }

    public void getPendingFiles() {
        mView.showGettingPending();

        mPendingPhotos = new ArrayList<>();
        mPendingVideos = new ArrayList<>();

        mPhotosRemoteDone = false;
        mVideosRemoteDone = false;

        mReadRemotePhotosOperation = new ReadRemoteFolderOperation(BaseConstants.PHOTOS_REMOTE_FOLDER);
        mReadRemotePhotosOperation.execute(mNetworkProvider.getCloudClient(), this, mHandler);

        mReadRemoteVideosOperation = new ReadRemoteFolderOperation(BaseConstants.VIDEOS_REMOTE_FOLDER);
        mReadRemoteVideosOperation.execute(mNetworkProvider.getCloudClient(), this, mHandler);
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

            localFiles = isPhotos ? FileUtils.getListOfCameraImages(mView.getContext()) : FileUtils.getListOfCameraVideos(mView.getContext());

            for (String item : localFiles) {
                if (isPhotos) {
                    if (!mRemotePhotosMap.containsKey(FileUtils.getFileName(item))) {
                        mPendingPhotos.add(item);
                    }
                } else {
                    if (!mRemoteVideosMap.containsKey(FileUtils.getFileName(item))) {
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
                    mRemotePhotosMap.put(FileUtils.getFileName(remoteFile.getRemotePath()), remoteFile);
                } else if (isVideo){
                    mRemoteVideosMap.put(FileUtils.getFileName(remoteFile.getRemotePath()), remoteFile);
                }
            }
        }
    }

    public void setFirstTimeFalse() {
        mSharedPrefsUtils.setBooleanPreference(NetworkProvider.KEY_FIRST_TIME, false);
    }
}
