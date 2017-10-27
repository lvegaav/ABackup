package com.americavoice.backup.news.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.americavoice.backup.R;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.news.presenter.NewsPresenter;
import com.americavoice.backup.utils.RecyclerItemClickListener;

import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by javier on 9/26/17.
 * News fragment
 */

public class NewsFragment extends BaseFragment implements INewsView {


    public interface Listener {
        void onNewsFeedGoBack();
        void onNewsClick(dtos.NewsFeed news);
        void newsFeedError();
    }

    private NewsRecyclerAdapter mAdapter;
    private Listener mListener;
    @Inject
    NewsPresenter mPresenter;

    RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);
        if (getActivity() instanceof Listener) {
            mListener = (Listener) getActivity();
        }
        initializePresenter();
        mRecyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        List<dtos.NewsFeed> news = Collections.emptyList();
        mAdapter = new NewsRecyclerAdapter(news);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                dtos.NewsFeed news = (dtos.NewsFeed) view.getTag();
                mListener.onNewsClick(news);
//                Intent intent = new Intent(getContext(), NewsDetailActivity.class);
//                Bundle extras = new Bundle();
//                extras.putString(NewsDetailActivity.TITLE, fakeNews.getTitle());
//                extras.putString(NewsDetailActivity.CONTENT, fakeNews.getDescription());
//
//                extras.putString(NewsDetailActivity.DATE, );
//                intent.putExtras(extras);
//                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
        return view;
    }

    private void initializePresenter() {
        this.getComponent(AppComponent.class).inject(this);
        mPresenter.setView(this);
    }

    @Override
    public void setNews(List<dtos.NewsFeed> newsList) {
        mAdapter.updateList(newsList);
        mRecyclerView.invalidate();

    }

    @Override
    public void showErrorAndClose(Exception ex) {
        Log.e("News feed", "Exception on retrieving news", ex);
        mListener.newsFeedError();
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        if (mListener != null) {
            mListener.onNewsFeedGoBack();
        }
    }
}
