
package com.americavoice.backup.main.presenter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.americavoice.backup.AndroidApplication;
import com.americavoice.backup.BuildConfig;
import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.ui.SplashScreenView;
import com.americavoice.backup.utils.ConnectivityUtils;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.security.ProviderInstaller;

import net.servicestack.client.AsyncResult;

import javax.inject.Inject;


/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class SplashScreenPresenter extends BasePresenter implements IPresenter {
    private static final int SPLASH_DISPLAY_LENGTH = 3000;
    private SplashScreenView mView;

    @Inject
    SplashScreenPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull SplashScreenView view) {
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
        checkVersionAndConfig();
    }

    private void checkVersionAndConfig() {

        if (!ConnectivityUtils.isAppConnected(mView.getContext())) {
            mView.showNoInternetDialog();
            return;
        }

        mNetworkProvider.getAppConfig(new AsyncResult<dtos.GetMobileAppConfigResponse>() {
            @Override
            public void success(dtos.GetMobileAppConfigResponse response) {
                if (response != null) {
                    if (response.versions == null || response.versions.isEmpty()){
                        Crashlytics.logException(new Throwable("App versions are null, please check your configuration"));
                        mView.showError(mView.getContext().getString(R.string.exception_message_generic));
                    }else if (response.getVersions().contains(BuildConfig.VERSION_NAME)){
                        if (!TextUtils.isEmpty(response.getFacebookUrl())){
                            mSharedPrefsUtils.setStringPreference("facebookUrl", response.getFacebookUrl());
                        } else {
                            Crashlytics.logException(new Throwable("facebookUrl configuration is null, please check your configuration"));
                        }
                        if (!TextUtils.isEmpty(response.getTwitterUrl())){
                            mSharedPrefsUtils.setStringPreference("twitterUrl", response.getTwitterUrl());
                        } else {
                            Crashlytics.logException(new Throwable("twitterUrl configuration is null, please check your configuration"));
                        }
                        if (!TextUtils.isEmpty(response.getCallCenterPhone())){
                            mSharedPrefsUtils.setStringPreference("callCenterPhone", response.getCallCenterPhone());
                        } else {
                            Crashlytics.logException(new Throwable("callCenterPhone configuration is null, please check your configuration"));
                        }
                        if (response.getTermsAndPrivacyUrls() != null && !response.getTermsAndPrivacyUrls().isEmpty()){
                            if (response.getTermsAndPrivacyUrls().containsKey("en")){
                                mSharedPrefsUtils.setStringPreference("enTermsOfService", response.getTermsAndPrivacyUrls().get("en"));
                            }
                            if (response.getTermsAndPrivacyUrls().containsKey("es")){
                                mSharedPrefsUtils.setStringPreference("esTermsOfService", response.getTermsAndPrivacyUrls().get("es"));
                            }
                        } else {
                            Crashlytics.logException(new Throwable("TermsOfService configuration is null, please check your configuration"));
                        }
                        doLogin();
                    } else {
                        checkForUpdates();
                    }
                } else {
                    Crashlytics.logException(new Throwable("App configuration is null, please check your configuration"));
                    mView.showError(mView.getContext().getString(R.string.exception_message_generic));
                }
            }

            @Override
            public void error(Exception ex) {
                Crashlytics.logException(ex);
                if (ex instanceof java.net.UnknownHostException) {
                    mView.showNoInternetDialog();
                } else if (ex instanceof java.net.SocketTimeoutException) {
                    mView.showError(mView.getContext().getString(R.string.network_error_socket_timeout_exception));
                } else {
                    mView.showError(mView.getContext().getString(R.string.exception_message_generic));
                }
            }
        });
    }

    private void checkForUpdates() {
        mView.showUpdateDialog();
    }

    private void doLogin() {
        if (mView.getContext() != null) {
            Account account = AccountUtils.getCurrentOwnCloudAccount(mView.getContext());
            if (account != null) {
                AccountManager accountManager = AccountManager.get(mView.getContext());
                String password = accountManager.getUserData(account, "backupPassword");
                String name = accountManager.getUserData(account, "backupUser");
                if (name == null || password == null) {
                    mNetworkProvider.logout();
                    mView.viewHome();
                    return;
                }
                mNetworkProvider.login(name, password, new AsyncResult<dtos.AuthenticateResponse>() {
                    @Override
                    public void success(dtos.AuthenticateResponse response) {
                        if (response.getMeta() != null) {
                            mView.saveSerials(response.getMeta().get("SerialB1"), response.getMeta().get("SerialB2"));
                        }
                        mView.viewHome();
                    }

                    @Override
                    public void error(Exception ex) {
                        Crashlytics.logException(ex);
                        mNetworkProvider.logout();
                        mView.viewHome();
                    }
                });
            } else {
                mView.viewHome();
            }
        }
    }
}
