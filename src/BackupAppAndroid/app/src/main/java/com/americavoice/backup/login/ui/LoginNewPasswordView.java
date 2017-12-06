package com.americavoice.backup.login.ui;

import com.americavoice.backup.login.model.SpinnerItem;
import com.americavoice.backup.main.ui.ILoadDataView;
import com.owncloud.android.lib.common.OwnCloudCredentials;

import java.util.List;

/**
 * Interface representing a View in a model view presenter (MVP) pattern.
 */
public interface LoginNewPasswordView extends ILoadDataView {
    void viewNewPasswordSuccess();

    void showVerificationCodeRequired();
    void showNewPasswordRequired();
    void showConfirmPasswordRequired();
    void showConfirmPasswordInvalid();
    void showNewPasswordInvalid();
}
