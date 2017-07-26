package com.americavoice.backup.confirmation.ui;

import com.americavoice.backup.main.ui.ILoadDataView;

/**
 * Interface representing a View in a model view presenter (MVP) pattern.
 */
public interface ConfirmationView extends ILoadDataView {
    void viewHome();
    void showConfirmationCodeInvalid();
}
