package com.americavoice.backup.news.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.americavoice.backup.R;

import java.util.List;

/**
 * Created by javier on 9/26/17.
 * Adapter for fake news
 */

public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.ViewHolder> {

    private List<FakeNews> mFakeNewsList;
    public NewsRecyclerAdapter(List<FakeNews> fakeNewsList) {
        mFakeNewsList = fakeNewsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_news_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FakeNews fakeNews = mFakeNewsList.get(position);
        holder.mTextView.setText(fakeNews.content);
        holder.mTitle.setText(fakeNews.title);
        holder.mDate.setText(fakeNews.date);
        holder.itemView.setTag(fakeNews);
    }

    @Override
    public int getItemCount() {
        return mFakeNewsList.size();
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
