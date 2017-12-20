    package com.americavoice.backup.main.ui;

/**
 * Interface representing a View in a model view presenter (MVP) pattern.
 */
public interface SplashScreenView extends ILoadDataView {
    void viewHome();
    void showNoInternetDialog();
    void showUpdateDialog();
    void saveSerials(String serialB1, String serialB2);
}
