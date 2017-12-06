package com.americavoice.backup.news.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.main.network.dtos;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Created by javier on 9/26/17.
 * Adapter for fake news
 */

public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.ViewHolder> {

    private List<dtos.NewsFeed> mNewsFeedList;

    public NewsRecyclerAdapter(List<dtos.NewsFeed> NewsFeedList) {
        mNewsFeedList = NewsFeedList;
    }

    public void updateList(List<dtos.NewsFeed> newsFeed) {
        mNewsFeedList = newsFeed;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_news_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        dtos.NewsFeed newsFeed = mNewsFeedList.get(position);
        holder.mTextView.setText(newsFeed.getShortDescription());

        String date = new SimpleDateFormat("MMM dd yyyy HH:mm a",
                Locale.getDefault()).format(newsFeed.getCreationDate());

        holder.mTitle.setText(newsFeed.getTitle());
        holder.mDate.setText(date);
        holder.itemView.setTag(newsFeed);
    }

    @Override
    public int getItemCount() {
        return mNewsFeedList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView mTextView;
        TextView mTitle;
        TextView mDate;
        ViewHolder(View view) {
            super(view);
            mTextView = view.findViewById(R.id.news_content);
            mTitle = view.findViewById(R.id.news_title);
            mDate = view.findViewById(R.id.news_date);
        }
    }
}
