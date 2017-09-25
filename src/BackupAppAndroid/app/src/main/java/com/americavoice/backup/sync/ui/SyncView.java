package com.americavoice.backup.sync.ui;

import com.americavoice.backup.main.ui.ILoadDataView;

import java.util.List;

/**
 * Interface representing a View in a model view presenter (MVP) pattern.
 */
public interface SyncView extends ILoadDataView {
    void syncJob(List<String> pendingPhotos, List<String> pendingVideos);
    void totalImages(int count);
    void totalVideos(int count);
}
