package com.americavoice.backup.login.ui;

import com.americavoice.backup.login.model.SpinnerItem;
import com.americavoice.backup.main.ui.ILoadDataView;
import com.owncloud.android.lib.common.OwnCloudCredentials;

import java.util.List;

/**
 * Interface representing a View in a model view presenter (MVP) pattern.
 */
public interface LoginForgotView extends ILoadDataView {
    void viewLoginNewPassword(String countryCode, String phoneNumber);
    void populateCountries(List<SpinnerItem> items);
    void showPhoneNumberRequired();
    void showPhoneNumberInvalid();
    void showCountryCodeRequired();
}
