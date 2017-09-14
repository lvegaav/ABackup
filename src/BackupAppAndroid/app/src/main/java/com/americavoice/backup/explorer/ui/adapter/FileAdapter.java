package com.americavoice.backup.explorer.ui.adapter;

import android.accounts.Account;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.datamodel.FileDataStorageManager;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.datamodel.ThumbnailsCacheManager;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.utils.DisplayUtils;
import com.americavoice.backup.utils.MimeTypeUtil;
import com.bumptech.glide.Glide;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.files.RemoteFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.TransactionViewHolder> {

    private FileDataStorageManager mStorageManager;

    public interface OnItemClickListener {
        void onItemClicked(OCFile file);
    }

    private List<OCFile> mCollection;
    private List<OCFile> mSelectedCollection;
    private final LayoutInflater mLayoutInflater;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;
    private Account mAccount;

    public FileAdapter(Context context, Collection<OCFile> collection,Collection<OCFile> selectedCollection, FileDataStorageManager fileDataStorageManager) {
        this.validateTransactionCollection(collection);
        this.mLayoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mCollection = (List<OCFile>) collection;
        this.mSelectedCollection= (List<OCFile>) selectedCollection;

        this.mContext = context;
        this.mStorageManager = fileDataStorageManager;
        mAccount = AccountUtils.getCurrentOwnCloudAccount(mContext);
    }

    public void refresh(List<OCFile> selectedCollection)
    {
        mSelectedCollection = selectedCollection;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        OCFile file = null;
        int viewType;
        if (mCollection != null && mCollection.size() > position) {
            file = mCollection.get(position);
        }

        if (file != null &&  (MimeTypeUtil.isImage(file.getMimetype()) || MimeTypeUtil.isVideo(file.getMimetype()))) {
            viewType = ViewType.GRID_IMAGE;
        } else {
            viewType = ViewType.LIST_ITEM;
        }
        return viewType;
    }

    @Override
    public int getItemCount() {
        return (this.mCollection != null) ? this.mCollection.size() : 0;
    }

    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == ViewType.GRID_IMAGE) {
             view = this.mLayoutInflater.inflate(R.layout.grid_image, parent, false);
        } else if (viewType == ViewType.GRID_ITEM) {
            view = this.mLayoutInflater.inflate(R.layout.grid_item, parent, false);
        } else if (viewType == ViewType.LIST_ITEM) {
            view = this.mLayoutInflater.inflate(R.layout.list_item, parent, false);
        }
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder, final int position) {
        final OCFile model = this.mCollection.get(position);

        holder.tvName.setText(model.getRemotePath().substring(model.getRemotePath().lastIndexOf('/') + 1));
        holder.ivIcon.setTag(model.getRemoteId());
        // If ListView
        if (getItemViewType(position) == ViewType.LIST_ITEM) {
            holder.tvFileSize.setText(DisplayUtils.bytesToHumanReadable(model.getFileLength()));
            holder.tvLastMod.setText(DisplayUtils.getRelativeTimestamp(mContext, model.getModificationTimestamp()));
        }

        if (model.isDown()) {
            holder.ivLocalFileIndicator.setImageResource(R.drawable.ic_synced);
            holder.ivLocalFileIndicator.setVisibility(View.VISIBLE);
        } else
        {
            holder.ivLocalFileIndicator.setVisibility(View.GONE);
        }

        if (model.isFolder()) {
            holder.tvName.setText(model.getRemotePath().substring(model.getRemotePath().substring(0, model.getRemotePath().length() -1).lastIndexOf('/') + 1));
            holder.ivIcon.setImageResource(
                    MimeTypeUtil.getFolderTypeIconId());
        } else if (model.getRemotePath().contains("Photos") || model.getRemotePath().contains("Videos")) {
            // Thumbnail in Cache?
            Bitmap thumbnail = ThumbnailsCacheManager.getBitmapFromDiskCache(model.getRemoteId());
            if (thumbnail != null) {
                if (MimeTypeUtil.isVideo(model.getMimetype())) {
                    Bitmap withOverlay = ThumbnailsCacheManager.addVideoOverlay(thumbnail);
                    holder.ivIcon.setImageBitmap(withOverlay);
                } else {
                    holder.ivIcon.setImageBitmap(thumbnail);
                }
            } else {
                // generate new Thumbnail
                if (ThumbnailsCacheManager.cancelPotentialThumbnailWork(model, holder.ivIcon)) {
                    try {
                        final ThumbnailsCacheManager.ThumbnailGenerationTask task =
                                new ThumbnailsCacheManager.ThumbnailGenerationTask(
                                        holder.ivIcon, mStorageManager, mAccount
                                );

                        if (MimeTypeUtil.isVideo(model.getMimetype())) {
                            thumbnail = ThumbnailsCacheManager.mDefaultVideo;
                        } else {
                            thumbnail = ThumbnailsCacheManager.mDefaultImg;
                        }

                        final ThumbnailsCacheManager.AsyncThumbnailDrawable asyncDrawable =
                                new ThumbnailsCacheManager.AsyncThumbnailDrawable(
                                        mContext.getResources(),
                                        thumbnail,
                                        task
                                );
                        holder.ivIcon.setImageDrawable(asyncDrawable);
                        task.execute(model);
                    } catch (IllegalArgumentException e) {
                        Log_OC.d(FileAdapter.class.getSimpleName(), "ThumbnailGenerationTask : " + e.getMessage());
                    }
                }
            }

            if (model.getMimetype().equalsIgnoreCase("image/png")) {
                holder.ivIcon.setBackgroundColor(mContext.getResources()
                        .getColor(R.color.white));
            }

        } else {
            Drawable drawable = MimeTypeUtil.getFileTypeIcon(model.getMimetype(), model.getRemotePath(), mAccount);
            holder.ivIcon.setImageDrawable(drawable);
        }
        holder.ivCheckBox.setVisibility(View.GONE);
        if (mSelectedCollection.size() > 0)
        {
            if (mSelectedCollection.contains(model)){
                holder.view.setBackgroundColor(mContext.getResources().getColor(
                        R.color.selected_item_background));
                holder.ivCheckBox.setImageResource(
                        R.drawable.ic_checkbox_marked);
            } else {
                holder.view.setBackgroundColor(Color.WHITE);
                holder.ivCheckBox.setImageResource(
                        R.drawable.ic_checkbox_blank_outline);
            }
            holder.ivCheckBox.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FileAdapter.this.mOnItemClickListener != null) {
                    FileAdapter.this.mOnItemClickListener.onItemClicked(model);
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<OCFile> getCollection()
    {
        return mCollection;
    }

    public List<OCFile> getSelectedCollection()
    {
        return mSelectedCollection;
    }

    public OCFile getSelectedItem(int position)
    {
        if (position < mSelectedCollection.size())
        {
            mSelectedCollection.get(position);
        }
        return  null;

    }

    public void setTransactionCollection(Collection<OCFile> collection) {
        this.validateTransactionCollection(collection);
        if (this.mCollection == null) {
            this.mCollection = (List<OCFile>) collection;
        } else {
            this.mCollection.addAll(collection);
        }
        this.notifyDataSetChanged();
    }
    public void resetSelectedCollection() {
        this.mSelectedCollection = new ArrayList<>();
        this.notifyDataSetChanged();
    }
    public void addOrRemoveSelectedItem(int position) {
        if (this.mSelectedCollection == null) {
            this.mSelectedCollection = new ArrayList<>();
        }

        if (mSelectedCollection.contains(mCollection.get(position)))
            mSelectedCollection.remove(mCollection.get(position));
        else
            mSelectedCollection.add(mCollection.get(position));

        this.notifyDataSetChanged();
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    private void validateTransactionCollection(Collection<OCFile> collection) {
        if (collection == null) {
            throw new IllegalArgumentException("The list cannot be null");
        }
    }

    private int getThumbnailDimension(){
        // Converts dp to pixel
        Resources r = mContext.getResources();
        Double d = Math.pow(2,Math.floor(Math.log(r.getDimension(R.dimen.file_icon_size_grid))/Math.log(2)));
        return d.intValue();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_icon)
        ImageView ivIcon;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_file_size)
        TextView tvFileSize;
        @BindView(R.id.tv_last_mod)
        TextView tvLastMod;
        @BindView(R.id.iv_local_file_indicator)
        ImageView ivLocalFileIndicator;
        @BindView(R.id.custom_checkbox)
        ImageView ivCheckBox;
        @BindView(R.id.ListItemLayout)
        View view;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


}