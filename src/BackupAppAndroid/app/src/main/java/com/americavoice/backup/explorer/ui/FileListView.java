package com.americavoice.backup.explorer.ui;

import com.americavoice.backup.main.ui.ILoadDataView;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.resources.files.RemoteFile;

import java.util.List;

/**
 * Interface representing a View in a model view presenter (MVP) pattern.
 */
public interface FileListView extends ILoadDataView {
    void renderList(List<RemoteFile> collection);
    void viewDetail(RemoteFile remoteFile);
    void viewFolder(String path);
    void renderEmpty();
    void showUploading();
    void showDownloading();
    void hideDLoading();
    void notifyDataSetChanged();
}
