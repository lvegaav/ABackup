/**
 * ownCloud Android client application
 *
 * @author Bartek Przybylski
 * @author Tobias Kaminsky
 * @author David A. Velasco
 * @author masensio
 * Copyright (C) 2011  Bartek Przybylski
 * Copyright (C) 2016 ownCloud Inc.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.americavoice.backup.explorer.ui.adapter;


import android.accounts.Account;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.datamodel.FileDataStorageManager;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.datamodel.ThumbnailsCacheManager;
import com.americavoice.backup.db.PreferenceManager;
import com.americavoice.backup.files.service.FileDownloader;
import com.americavoice.backup.files.service.FileUploader;
import com.americavoice.backup.service.OperationsService;
import com.americavoice.backup.utils.ComponentsGetter;
import com.americavoice.backup.utils.DisplayUtils;
import com.americavoice.backup.utils.FileStorageUtils;
import com.americavoice.backup.utils.MimeTypeUtil;
import com.owncloud.android.lib.common.utils.Log_OC;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;


/**
 * This Adapter populates a ListView with all files and folders in an ownCloud
 * instance.
 */
public class FileListListAdapter extends BaseAdapter {

    private Context mContext;
    private Vector<OCFile> mFilesAll = new Vector<OCFile>();
    private Vector<OCFile> mFiles = null;
    private boolean mJustFolders;

    private FileDataStorageManager mStorageManager;
    private Account mAccount;
    private ComponentsGetter mTransferServiceGetter;

    private FilesFilter mFilesFilter;
    private OCFile currentDirectory;
    private static final String TAG = FileListListAdapter.class.getSimpleName();

    public FileListListAdapter(boolean justFolders,
                               Context context,
                               ComponentsGetter transferServiceGetter) {

        mJustFolders = justFolders;
        mContext = context;
        mAccount = AccountUtils.getCurrentOwnCloudAccount(mContext);

        mTransferServiceGetter = transferServiceGetter;

        // Read sorting order, default to sort by name ascending
        FileStorageUtils.mSortOrder = PreferenceManager.getSortOrder(mContext);
        FileStorageUtils.mSortAscending = PreferenceManager.getSortAscending(mContext);

        // initialise thumbnails cache on background thread
        new ThumbnailsCacheManager.InitDiskCacheTask().execute();
    }

    public FileListListAdapter(boolean justFolders,
                               Context context,
                               ComponentsGetter transferServiceGetter,
                               FileDataStorageManager fileDataStorageManager) {

        this(justFolders, context, transferServiceGetter);
        mStorageManager = fileDataStorageManager;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public int getCount() {
        return mFiles != null ? mFiles.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        if (mFiles == null || mFiles.size() <= position) {
            return null;
        }
        return mFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (mFiles == null || mFiles.size() <= position) {
            return 0;
        }
        return mFiles.get(position).getFileId();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        OCFile file = null;
        LayoutInflater inflator = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (mFiles != null && mFiles.size() > position) {
            file = mFiles.get(position);
        }

        // Find out which layout should be displayed
        ViewType viewType;
        if (parent instanceof GridView) {
            if (file != null && (MimeTypeUtil.isImage(file) || MimeTypeUtil.isVideo(file))) {
                viewType = ViewType.GRID_IMAGE;
            } else {
                viewType = ViewType.GRID_ITEM;
            }
        } else {
            viewType = ViewType.LIST_ITEM;
        }

        // create view only if differs, otherwise reuse
        if (convertView == null || convertView.getTag() != viewType) {
            switch (viewType) {
                case GRID_IMAGE:
                    view = inflator.inflate(R.layout.grid_image, parent, false);
                    view.setTag(ViewType.GRID_IMAGE);
                    break;
                case GRID_ITEM:
                    view = inflator.inflate(R.layout.grid_item, parent, false);
                    view.setTag(ViewType.GRID_ITEM);
                    break;
                case LIST_ITEM:
                    view = inflator.inflate(R.layout.list_item, parent, false);
                    view.setTag(ViewType.LIST_ITEM);
                    break;
            }
        }

        if (file != null) {

            ImageView fileIcon = (ImageView) view.findViewById(R.id.thumbnail);

            fileIcon.setTag(file.getFileId());
            TextView fileName;
            String name = file.getFileName();

            switch (viewType) {
                case LIST_ITEM:
                    TextView fileSizeV = (TextView) view.findViewById(R.id.file_size);
                    TextView fileSizeSeparatorV = (TextView) view.findViewById(R.id.file_separator);
                    TextView lastModV = (TextView) view.findViewById(R.id.last_mod);


                    lastModV.setVisibility(View.VISIBLE);
                    lastModV.setText(DisplayUtils.getRelativeTimestamp(mContext, file.getModificationTimestamp()));


                    fileSizeSeparatorV.setVisibility(View.VISIBLE);
                    fileSizeV.setVisibility(View.VISIBLE);
                    fileSizeV.setText(DisplayUtils.bytesToHumanReadable(file.getFileLength()));


                case GRID_ITEM:
                    // filename
                    fileName = (TextView) view.findViewById(R.id.Filename);
                    name = file.getFileName();
                    fileName.setText(name);

                case GRID_IMAGE:
                    // local state
                    ImageView localStateView = (ImageView) view.findViewById(R.id.localFileIndicator);
                    localStateView.bringToFront();
                    FileDownloader.FileDownloaderBinder downloaderBinder =
                            mTransferServiceGetter.getFileDownloaderBinder();
                    FileUploader.FileUploaderBinder uploaderBinder =
                            mTransferServiceGetter.getFileUploaderBinder();
                    OperationsService.OperationsServiceBinder opsBinder =
                            mTransferServiceGetter.getOperationsServiceBinder();

                    localStateView.setVisibility(View.INVISIBLE);   // default first

                    if ( //synchronizing
                            opsBinder != null &&
                                    opsBinder.isSynchronizing(mAccount, file)
                            ) {
                        localStateView.setImageResource(R.drawable.ic_synchronizing);
                        localStateView.setVisibility(View.VISIBLE);

                    } else if ( // downloading
                            downloaderBinder != null &&
                                    downloaderBinder.isDownloading(mAccount, file)
                            ) {
                        localStateView.setImageResource(R.drawable.ic_synchronizing);
                        localStateView.setVisibility(View.VISIBLE);

                    } else if ( //uploading
                            uploaderBinder != null &&
                                    uploaderBinder.isUploading(mAccount, file)
                            ) {
                        localStateView.setImageResource(R.drawable.ic_synchronizing);
                        localStateView.setVisibility(View.VISIBLE);

                    } else if (file.getEtagInConflict() != null) {   // conflict
                        localStateView.setImageResource(R.drawable.ic_synchronizing_error);
                        localStateView.setVisibility(View.VISIBLE);

                    } else if (file.isDown()) {
                        localStateView.setImageResource(R.drawable.ic_synced);
                        localStateView.setVisibility(View.VISIBLE);
                    }

                    break;
            }

            // For all Views

            ImageView checkBoxV = (ImageView) view.findViewById(R.id.custom_checkbox);
            checkBoxV.setVisibility(View.GONE);
            view.setBackgroundColor(Color.WHITE);

            AbsListView parentList = (AbsListView) parent;
            if (parentList.getChoiceMode() != AbsListView.CHOICE_MODE_NONE &&
                    parentList.getCheckedItemCount() > 0
                    ) {
                if (parentList.isItemChecked(position)) {
                    view.setBackgroundColor(mContext.getResources().getColor(
                            R.color.selected_item_background));
                    checkBoxV.setImageResource(
                            R.drawable.ic_checkbox_marked);
                } else {
                    view.setBackgroundColor(Color.WHITE);
                    checkBoxV.setImageResource(
                            R.drawable.ic_checkbox_blank_outline);
                }
                checkBoxV.setVisibility(View.VISIBLE);
            }


            // No Folder
            if (!file.isFolder()) {
                if ((MimeTypeUtil.isImage(file) || MimeTypeUtil.isVideo(file)) && file.getRemoteId() != null) {
                    // Thumbnail in Cache?
                    Bitmap thumbnail = ThumbnailsCacheManager.getBitmapFromDiskCache(file.getRemoteId());
                    if (thumbnail != null && !file.needsUpdateThumbnail()) {

                        if (MimeTypeUtil.isVideo(file)) {
                            Bitmap withOverlay = ThumbnailsCacheManager.addVideoOverlay(thumbnail);
                            fileIcon.setImageBitmap(withOverlay);
                        } else {
                            fileIcon.setImageBitmap(thumbnail);
                        }
                    } else {
                        // generate new Thumbnail
                        if (ThumbnailsCacheManager.cancelPotentialThumbnailWork(file, fileIcon)) {
                            try {
                                final ThumbnailsCacheManager.ThumbnailGenerationTask task =
                                        new ThumbnailsCacheManager.ThumbnailGenerationTask(
                                                fileIcon, mStorageManager, mAccount
                                        );

                                if (thumbnail == null) {
                                    if (MimeTypeUtil.isVideo(file)) {
                                        thumbnail = ThumbnailsCacheManager.mDefaultVideo;
                                    } else {
                                        thumbnail = ThumbnailsCacheManager.mDefaultImg;
                                    }
                                }
                                final ThumbnailsCacheManager.AsyncThumbnailDrawable asyncDrawable =
                                        new ThumbnailsCacheManager.AsyncThumbnailDrawable(
                                                mContext.getResources(),
                                                thumbnail,
                                                task
                                        );
                                fileIcon.setImageDrawable(asyncDrawable);
                                task.execute(file);
                            } catch (IllegalArgumentException e) {
                                Log_OC.d(TAG, "ThumbnailGenerationTask : " + e.getMessage());
                            }
                        }
                    }

                    if (file.getMimetype().equalsIgnoreCase("image/png")) {
                        fileIcon.setBackgroundColor(mContext.getResources()
                                .getColor(R.color.white));
                    }


                } else {
                    fileIcon.setImageResource(MimeTypeUtil.getFileTypeIconId(file.getMimetype(),
                            file.getFileName()));
                }


            } else {
                // Folder
                fileIcon.setImageResource(MimeTypeUtil.getFolderTypeIconId());
            }
        }
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return (mFiles == null || mFiles.isEmpty());
    }

    /**
     * Change the adapted directory for a new one
     *
     * @param directory             New folder to adapt. Can be NULL, meaning
     *                              "no content to adapt".
     * @param updatedStorageManager Optional updated storage manager; used to replace
     *                              mStorageManager if is different (and not NULL)
     */
    public void swapDirectory(OCFile directory, FileDataStorageManager updatedStorageManager
            , boolean onlyOnDevice) {
        if (updatedStorageManager != null && !updatedStorageManager.equals(mStorageManager)) {
            mStorageManager = updatedStorageManager;
            mAccount = AccountUtils.getCurrentOwnCloudAccount(mContext);
        }
        if (mStorageManager != null) {
            mFiles = mStorageManager.getFolderContent(directory, onlyOnDevice);

            if (mJustFolders) {
                mFiles = getFolders(mFiles);
            }
            if (!PreferenceManager.showHiddenFilesEnabled(mContext)) {
                mFiles = filterHiddenFiles(mFiles);
            }
            mFiles = FileStorageUtils.sortOcFolder(mFiles);
            mFilesAll.clear();
            mFilesAll.addAll(mFiles);

            currentDirectory = directory;
        } else {
            mFiles = null;
            mFilesAll.clear();
        }

        notifyDataSetChanged();
    }

    private void searchForLocalFileInDefaultPath(OCFile file) {
        if (file.getStoragePath() == null && !file.isFolder()) {
            File f = new File(FileStorageUtils.getDefaultSavePathFor(mAccount.name, file));
            if (f.exists()) {
                file.setStoragePath(f.getAbsolutePath());
                file.setLastSyncDateForData(f.lastModified());
            }
        }
    }

    /**
     * Filter for getting only the folders
     *
     * @param files Collection of files to filter
     * @return Folders in the input
     */
    public Vector<OCFile> getFolders(Vector<OCFile> files) {
        Vector<OCFile> ret = new Vector<>();
        OCFile current;
        for (int i = 0; i < files.size(); i++) {
            current = files.get(i);
            if (current.isFolder()) {
                ret.add(current);
            }
        }
        return ret;
    }


    public void setSortOrder(Integer order, boolean ascending) {

        PreferenceManager.setSortOrder(mContext, order);
        PreferenceManager.setSortAscending(mContext, ascending);

        FileStorageUtils.mSortOrder = order;
        FileStorageUtils.mSortAscending = ascending;

        mFiles = FileStorageUtils.sortOcFolder(mFiles);
        notifyDataSetChanged();
    }


    public ArrayList<OCFile> getCheckedItems(AbsListView parentList) {
        SparseBooleanArray checkedPositions = parentList.getCheckedItemPositions();
        ArrayList<OCFile> files = new ArrayList<>();
        Object item;
        for (int i = 0; i < checkedPositions.size(); i++) {
            if (checkedPositions.valueAt(i)) {
                item = getItem(checkedPositions.keyAt(i));
                if (item != null) {
                    files.add((OCFile) item);
                }
            }
        }
        return files;
    }

    public Filter getFilter() {
        if (mFilesFilter == null) {
            mFilesFilter = new FilesFilter();
        }
        return mFilesFilter;
    }

    private class FilesFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            Vector<OCFile> filteredFiles = new Vector<>();

            if (!TextUtils.isEmpty(constraint)) {
                for (int i = 0; i < mFilesAll.size(); i++) {
                    OCFile currentFile = mFilesAll.get(i);
                    if (currentFile.getParentRemotePath().equals(currentDirectory.getRemotePath()) &&
                            currentFile.getFileName().toLowerCase().contains(constraint.toString().toLowerCase()) &&
                            !filteredFiles.contains(currentFile)) {
                        filteredFiles.add(currentFile);
                    }
                }
            }

            results.values = filteredFiles;
            results.count = filteredFiles.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Vector<OCFile> ocFiles = (Vector<OCFile>) results.values;
            mFiles = new Vector<>();
            if (ocFiles != null && ocFiles.size() > 0) {
                mFiles.addAll(ocFiles);
                if (!PreferenceManager.showHiddenFilesEnabled(mContext)) {
                    mFiles = filterHiddenFiles(mFiles);
                }
                mFiles = FileStorageUtils.sortOcFolder(mFiles);
            }

            notifyDataSetChanged();

        }
    }


    /**
     * Filter for hidden files
     *
     * @param files Collection of files to filter
     * @return Non-hidden files
     */
    public Vector<OCFile> filterHiddenFiles(Vector<OCFile> files) {
        Vector<OCFile> ret = new Vector<>();
        OCFile current;
        for (int i = 0; i < files.size(); i++) {
            current = files.get(i);
            if (!current.isHidden() && !ret.contains(current)) {
                ret.add(current);
            }
        }
        return ret;
    }

}
