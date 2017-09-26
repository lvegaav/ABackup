package com.americavoice.backup.news.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.americavoice.backup.R;

/**
 * Created by javier on 9/26/17.
 * News activity
 */

public class NewsActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.news_title));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        viewPager.setAdapter(new NewsTabsAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(this);

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        String text = "null";
        if (tab.getText() != null) {
            text = tab.getText().toString();
        }
        Log.d("Tabs", text + tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        String text = "null";
        if (tab.getText() != null) {
            text = tab.getText().toString();
        }
        Log.d("Tabs unselected", text);
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
