package com.americavoice.backup.login.ui;

import com.americavoice.backup.login.model.SpinnerItem;
import com.americavoice.backup.main.ui.ILoadDataView;
import com.owncloud.android.lib.common.OwnCloudCredentials;

import java.util.List;

/**
 * Interface representing a View in a model view presenter (MVP) pattern.
 */
public interface LoginView extends ILoadDataView {
    void viewValidation(String username, String device);

    void loginWithCredentials(OwnCloudCredentials credentials);
    void showGettingServerInfo();
    void hideGettingServerInfo();
    void showUsernameRequired();
    void showPasswordRequired();

}
