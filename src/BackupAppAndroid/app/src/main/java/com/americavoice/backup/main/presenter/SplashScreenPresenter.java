
package com.americavoice.backup.main.presenter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.americavoice.backup.BuildConfig;
import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.ui.SplashScreenView;
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
        mNetworkProvider.getAppConfig(new AsyncResult<dtos.GetMobileAppConfigResponse>() {
            @Override
            public void success(dtos.GetMobileAppConfigResponse response) {
                if (response != null) {
                    if (response.versions == null || response.versions.isEmpty()){
                        Crashlytics.logException(new Throwable("App versions are null, please check your configuration"));
                        mView.showError(mView.getContext().getString(R.string.exception_message_generic));
                        mView.finish();
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
                        mView.finish();
                    }
                } else {
                    Crashlytics.logException(new Throwable("App configuration is null, please check your configuration"));
                    mView.showError(mView.getContext().getString(R.string.exception_message_generic));
                    mView.finish();
                }
            }

            @Override
            public void error(Exception ex) {
                Crashlytics.logException(ex);
                mView.showError(mView.getContext().getString(R.string.exception_message_generic));
                mView.finish();
            }
        });
    }

    private void checkForUpdates() {
        final String appPackageName = mView.getContext().getPackageName(); // getPackageName() from Context or Activity object
        try {
            mView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            mView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void doLogin() {
        Account account = AccountUtils.getCurrentOwnCloudAccount(mView.getContext());
        if (account != null) {
            AccountManager accountManager = AccountManager.get(mView.getContext());
            String password = accountManager.getPassword(account);
            String name = AccountUtils.getAccountUsername(account.name);
            mNetworkProvider.login(name, password, new AsyncResult<dtos.AuthenticateResponse>() {
                @Override
                public void success(dtos.AuthenticateResponse response) {
                    mView.viewHome();
                }

                @Override
                public void error(Exception ex) {
                    Crashlytics.logException(ex);
                    mNetworkProvider.logout();
                    mView.viewHome();
                }
            });
        } else{
            mView.viewHome();
        }
    }
}
