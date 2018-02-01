package com.americavoice.backup.news.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.news.presenter.NewsPresenter;
import com.americavoice.backup.utils.RecyclerItemClickListener;
import com.americavoice.backup.widgets.RecyclerView;

import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

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

    private Unbinder mUnbinder;
    private NewsRecyclerAdapter mAdapter;
    private Listener mListener;
    @Inject
    NewsPresenter mPresenter;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.empty_news_text)
    TextView mEmptyNewsView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        if (getActivity() instanceof Listener) {
            mListener = (Listener) getActivity();
        }
        initializePresenter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setEmptyView(mEmptyNewsView);
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

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }

    private void initializePresenter() {
        this.getComponent(AppComponent.class).inject(this);
        mPresenter.setView(this);
    }

    @Override
    public void setNews(List<dtos.NewsFeed> newsList) {
        mAdapter.updateList(newsList);
        if (mRecyclerView != null) {
            mRecyclerView.invalidate();
        }
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


    @Override
    public void showLoading() {
        showDialog(getString(R.string.common_loading));
    }

    @Override
    public void hideLoading() {
        hideDialog();
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {

    }

}
