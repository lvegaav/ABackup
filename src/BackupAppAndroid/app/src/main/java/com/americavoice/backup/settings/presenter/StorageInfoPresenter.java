
package com.americavoice.backup.settings.presenter;

import android.Manifest;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.files.utils.FileUtils;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.settings.ui.SettingsView;
import com.americavoice.backup.settings.ui.StorageInfoView;
import com.americavoice.backup.utils.BaseConstants;
import com.americavoice.backup.utils.MimeType;
import com.americavoice.backup.utils.PermissionUtil;
import com.crashlytics.android.Crashlytics;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;

import net.servicestack.client.AsyncResult;

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
    private ReadRemoteFolderOperation mReadRemotePhotosOperation;
    private ReadRemoteFolderOperation mReadRemoteVideosOperation;
    private List<String> mPendingPhotos;
    private List<String> mPendingVideos;

    private boolean mPhotosRemoteDone;
    private boolean mVideosRemoteDone;

    @Inject
    StorageInfoPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
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
        getStorageInfo();
    }

    private void getStorageInfo() {
        mView.showLoading();

        mNetworkProvider.getUserAccountUsage(new AsyncResult<dtos.GetAccountUsageResponse>() {
            @Override
            public void success(dtos.GetAccountUsageResponse response) {
                if (response != null) {
                    HashMap<String, BigDecimal> mSizes = new HashMap<>();
                    BigDecimal totalQuota = new BigDecimal(response.getTotalQuota());
                    BigDecimal availableQuota = new BigDecimal(response.getAvailableQuota());
                    for (dtos.UsedStorage folder : response.getUsedStorage()) {
                        mSizes.put(folder.getName(), new BigDecimal(folder.getUsedStorageSize()));
                    }
                    mView.showPercent(mSizes, totalQuota, availableQuota);
                }

            }

            @Override
            public void error(Exception ex) {
                Crashlytics.logException(ex);
            }

            @Override
            public void complete() {
                mView.hideLoading();
            }
        });
    }

    private float getPercent(BigDecimal value, BigDecimal size) {
        float x = value != null ? value.floatValue() * 100 : 0;
        float x1 = x / size.floatValue();
        BigDecimal x2= new BigDecimal(x1).setScale(1,BigDecimal.ROUND_HALF_UP);
        return x2.floatValue();
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

        OwnCloudClient client = mNetworkProvider.getCloudClient();
        if (client != null) {
            mReadRemotePhotosOperation = new ReadRemoteFolderOperation(BaseConstants.PHOTOS_REMOTE_FOLDER);
            mReadRemotePhotosOperation.execute(client, this, mHandler);

            mReadRemoteVideosOperation = new ReadRemoteFolderOperation(BaseConstants.VIDEOS_REMOTE_FOLDER);
            mReadRemoteVideosOperation.execute(client, this, mHandler);
        }
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation remoteOperation, RemoteOperationResult result) {

        boolean isPhotos = remoteOperation.equals(mReadRemotePhotosOperation);
        boolean isVideos = remoteOperation.equals(mReadRemoteVideosOperation);
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
