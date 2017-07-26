package com.americavoice.backup.explorer.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.owncloud.android.lib.resources.files.RemoteFile;

import java.util.Collection;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.TransactionViewHolder> {

    public interface OnItemClickListener {
        void onItemClicked(RemoteFile file);
    }

    private List<RemoteFile> mCollection;
    private final LayoutInflater mLayoutInflater;
    private OnItemClickListener mOnItemClickListener;

    public FileAdapter(Context context, Collection<RemoteFile> collection) {
        this.validateTransactionCollection(collection);
        this.mLayoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mCollection = (List<RemoteFile>) collection;
    }

    @Override
    public int getItemCount() {
        return (this.mCollection != null) ? this.mCollection.size() : 0;
    }

    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = this.mLayoutInflater.inflate(R.layout.row_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder, final int position) {
        final RemoteFile model = this.mCollection.get(position);
        holder.tvName.setText(model.getRemotePath().substring(model.getRemotePath().lastIndexOf('/') + 1));
        if (model.getMimeType().equals("DIR"))
        {
            holder.tvName.setText(model.getRemotePath().substring(model.getRemotePath().substring(0, model.getRemotePath().length() -1).lastIndexOf('/') + 1));
            holder.ivIcon.setImageResource(R.drawable.ic_folder);
        } else if (model.getRemotePath().contains("Contacts"))
        {
            holder.ivIcon.setImageResource(R.drawable.ic_contact);
        } else if (model.getRemotePath().contains("Videos"))
        {
            holder.ivIcon.setImageResource(R.drawable.ic_video);
        } else if (model.getRemotePath().contains("Photos"))
        {
            holder.ivIcon.setImageResource(R.drawable.ic_photo);
        } else
        {
            holder.ivIcon.setImageResource(R.drawable.ic_document);
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

    public void setTransactionCollection(Collection<RemoteFile> collection) {
        this.validateTransactionCollection(collection);
        if (this.mCollection == null) {
            this.mCollection = (List<RemoteFile>) collection;
        } else {
            this.mCollection.addAll(collection);
        }
        this.notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    private void validateTransactionCollection(Collection<RemoteFile> collection) {
        if (collection == null) {
            throw new IllegalArgumentException("The list cannot be null");
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_icon)
        ImageView ivIcon;
        @BindView(R.id.tv_name)
        TextView tvName;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}