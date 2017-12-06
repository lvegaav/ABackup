package com.americavoice.backup.news.presenter;

import android.util.Log;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.news.ui.INewsView;
import com.crashlytics.android.Crashlytics;

import net.servicestack.client.AsyncResult;

import javax.inject.Inject;

/**
 * Created by javier on 10/27/17.
 */

@PerActivity
public class NewsPresenter extends BasePresenter implements IPresenter {

    private INewsView mView;

    @Inject
    public NewsPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(INewsView view) {
        mView = view;
        mView.showLoading();
        mNetworkProvider.getNewsFeed(new AsyncResult<dtos.GetNewsFeedResponse>() {
            @Override
            public void success(dtos.GetNewsFeedResponse response) {
                Log.d("News presenter", "Got " + response.getNewsFeeds().size() + " news");
                mView.setNews(response.getNewsFeeds());
            }

            @Override
            public void error(Exception ex) {
                Crashlytics.logException(ex);
                mView.showErrorAndClose(ex);
            }

            @Override
            public void complete() {
                mView.hideLoading();
            }
        });
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }
}
