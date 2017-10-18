
package com.americavoice.backup.settings.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.americavoice.backup.R;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.files.utils.FileUtils;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.settings.ui.SettingsView;
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
public class SettingsPresenter extends BasePresenter implements IPresenter {

    private SettingsView mView;

    @Inject
    public SettingsPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
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
    }

    public void logout() {
        mNetworkProvider.logout();
        mSharedPrefsUtils.setStringPreference(NetworkProvider.KEY_PHONE_NUMBER, null);
    }

    public void showStorageInfo() {

    }

}
