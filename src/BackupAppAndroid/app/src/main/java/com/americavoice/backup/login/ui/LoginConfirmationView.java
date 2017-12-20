package com.americavoice.backup.login.ui;

import com.americavoice.backup.main.ui.ILoadDataView;
import com.owncloud.android.lib.common.OwnCloudCredentials;

/**
 * Interface representing a View in a model view presenter (MVP) pattern.
 */
public interface LoginConfirmationView extends ILoadDataView {
    void viewHome();
    void showConfirmationCodeExpired();
    void showConfirmationCodeInvalid();
    void showGettingServerInfo();
    void hideGettingServerInfo();
    void loginWithCredentials();
}
