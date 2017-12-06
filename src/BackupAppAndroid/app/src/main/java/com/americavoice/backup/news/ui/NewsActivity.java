package com.americavoice.backup.news.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.americavoice.backup.R;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.di.components.DaggerAppComponent;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.ui.activity.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by javier on 9/26/17.
 * News activity
 */

public class NewsActivity extends BaseActivity implements NewsFragment.Listener, HasComponent<AppComponent> {

    private AppComponent mAppComponent;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
//    @BindView(R.id.tab_layout)
//    TabLayout mTabLayout;
//    @BindView(R.id.pager)
//    ViewPager mViewPager;

    private Unbinder mUnBind;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        mUnBind = ButterKnife.bind(this);
        mAppComponent = DaggerAppComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build();

        mToolbar.setTitle(getString(R.string.news_title));
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        replaceFragment(R.id.content, new NewsFragment(), true, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnBind.unbind();
    }

    @Override
    public void onNewsFeedGoBack() {
        finish();
    }

    @Override
    public void onNewsClick(dtos.NewsFeed news) {
        Intent intent = new Intent(this, NewsDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putString(NewsDetailActivity.TITLE, news.getTitle());
        extras.putString(NewsDetailActivity.CONTENT, news.getDescription());
        String date = new SimpleDateFormat("MMM dd yyyy HH:mm a", Locale.getDefault())
                .format(news.creationDate);
        extras.putString(NewsDetailActivity.DATE, date);
        intent.putExtras(extras);
        startActivity(intent);
    }

    @Override
    public void newsFeedError() {
        new AlertDialog.Builder(this, R.style.WhiteDialog)
                .setTitle("Error")
                .setMessage("There was an error while fetching the news feed, please try again later")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    public AppComponent getComponent() {
        return mAppComponent;
    }

}
