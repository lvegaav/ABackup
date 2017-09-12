package com.americavoice.backup.main.ui;

/**
 * Interface representing a View in a model view presenter (MVP) pattern.
 */
public interface MainView extends ILoadDataView, AppCompatFragmentView {
    void render();
    void setBadgePhotos(int size);
    void setBadgeVideos(int size);
    void setBadgeContacts(int size);
    void setBadgeFiles(int size);
    void setBadgeSms(int size);
    void setBadgeCallLog(int size);
}
