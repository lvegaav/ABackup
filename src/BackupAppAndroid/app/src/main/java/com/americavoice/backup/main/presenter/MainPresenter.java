
package com.americavoice.backup.main.presenter;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.ui.MainView;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;

import net.servicestack.client.AsyncResult;
import net.servicestack.client.WebServiceException;

import javax.inject.Inject;


/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class MainPresenter extends BasePresenter implements IPresenter {

    private MainView mView;

    @Inject
    public MainPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull MainView view) {
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
    public void initialize(String title) {

        mView.setTitle(title);

        mNetworkProvider.getUser(new AsyncResult<dtos.GetFullUserResponse>() {
            @Override
            public void success(dtos.GetFullUserResponse response) {

            }

            @Override
            public void error(Exception ex) {
            }
        });

    }
}
