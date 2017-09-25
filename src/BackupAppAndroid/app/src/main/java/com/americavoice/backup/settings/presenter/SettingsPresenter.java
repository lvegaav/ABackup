
package com.americavoice.backup.settings.presenter;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.americavoice.backup.R;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.exception.ErrorBundle;
import com.americavoice.backup.main.exception.ErrorMessageFactory;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.settings.ui.SettingsView;
import com.americavoice.backup.utils.BaseConstants;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;

import javax.inject.Inject;


/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class SettingsPresenter extends BasePresenter implements IPresenter, OnRemoteOperationListener {

    private SettingsView mView;
    private Handler mHandler;

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

    /**
     * Initializes the presenter
     */
    public void initialize() {
        mView.showLoading();
        ReadRemoteFolderOperation refreshOperation = new ReadRemoteFolderOperation("/");
        refreshOperation.execute(mNetworkProvider.getCloudClient(getPhoneNumber()), this, mHandler);
    }

    public void submit(final String phoneNumber) {
    }

    private void showErrorMessage(ErrorBundle errorBundle) {
        String errorMessage = ErrorMessageFactory.create(this.mView.getContext(),
                errorBundle.getException());
        this.mView.showError(errorMessage);
    }

    public void logout() {
        mNetworkProvider.logout();
        mSharedPrefsUtils.setStringPreference(NetworkProvider.KEY_PHONE_NUMBER, null);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation remoteOperation, RemoteOperationResult result) {
        HashMap<String, BigDecimal> mSizes = new HashMap<>();
        BigDecimal total = new BigDecimal(0);
        BigDecimal totalAvailable = new BigDecimal(0);
        if (result.getData() == null) {
            mView.hideLoading();
            mView.showDefaultError();
            return;
        }
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
                    || remoteFile.getRemotePath().equals(BaseConstants.CALLS_BACKUP_FOLDER)
                    || remoteFile.getRemotePath().equals(BaseConstants.SMS_BACKUP_FOLDER)) {
                BigDecimal size = new BigDecimal(0);
                try {
                    Field field =RemoteFile.class.getDeclaredField("mQuotaUsedBytes");
                    field.setAccessible(true);
                    size = (BigDecimal) field.get(remoteFile);
                    total = total.add(size);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                mSizes.put(remoteFile.getRemotePath(), size);
            }
        }
        mView.showPercent(mSizes, total, totalAvailable);
        mView.hideLoading();

    }
}
