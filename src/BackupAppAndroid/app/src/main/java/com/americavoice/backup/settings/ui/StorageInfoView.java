package com.americavoice.backup.settings.ui;

import com.americavoice.backup.main.ui.ILoadDataView;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * Interface representing a View in a model view presenter (MVP) pattern.
 */
public interface StorageInfoView extends ILoadDataView {
    void showPercent(HashMap<String, BigDecimal> sizes, BigDecimal total, BigDecimal totalAvailable);
    void showDefaultError();
    void showGettingPending();
    void showSyncDialog(int pendingPhotos, int pendingVideos, int pendingMusic);
    void scheduleSyncJob(List<String> pendingPhotos, List<String> pendingVideos, List<String> pendingMusic);
    void showRequestPermissionDialog();
}
