package com.americavoice.backup.login.ui;

import com.americavoice.backup.main.ui.ILoadDataView;

/**
 * Interface representing a View in a model view presenter (MVP) pattern.
 */
public interface LoginView extends ILoadDataView {
    void viewHome();
    void viewValidation();

    void showPhoneNumberRequired();
    void showPhoneNumberInvalid();
}
