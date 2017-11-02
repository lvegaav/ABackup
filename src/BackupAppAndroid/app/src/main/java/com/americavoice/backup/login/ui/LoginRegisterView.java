package com.americavoice.backup.login.ui;

import com.americavoice.backup.login.model.SpinnerItem;
import com.americavoice.backup.main.ui.ILoadDataView;
import com.owncloud.android.lib.common.OwnCloudCredentials;

import java.util.List;

/**
 * Interface representing a View in a model view presenter (MVP) pattern.
 */
public interface LoginRegisterView extends ILoadDataView {
    void viewValidation(String username, String device);
    void populateCountries(List<SpinnerItem> items);

    void showPhoneNumberRequired();
    void showPhoneNumberInvalid();
    void showUsernameRequired();
    void showUsernameInvalid();
    void showNewPasswordRequired();
    void showConfirmPasswordRequired();
    void showConfirmPasswordInvalid();
    void showNewPasswordInvalid();
    void showUsernameExists();
    void showPhoneNumberExists();
}
