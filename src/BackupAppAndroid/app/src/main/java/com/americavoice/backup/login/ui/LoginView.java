package com.americavoice.backup.login.ui;

import com.americavoice.backup.main.ui.ILoadDataView;
import com.owncloud.android.lib.common.OwnCloudCredentials;

/**
 * Interface representing a View in a model view presenter (MVP) pattern.
 */
public interface LoginView extends ILoadDataView {
    void viewHome();
    void viewValidation();
    void loginWithCredentials(OwnCloudCredentials credentials);
    void showGettingServerInfo();
    void hideGettingServerInfo();
    void showPhoneNumberRequired();
    void showPhoneNumberInvalid();
}
