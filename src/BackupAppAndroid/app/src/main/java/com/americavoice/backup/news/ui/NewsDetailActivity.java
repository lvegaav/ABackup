package com.americavoice.backup.news.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.americavoice.backup.R;

/**
 * Created by javier on 9/26/17.
 */

public class NewsDetailActivity extends AppCompatActivity {

    public static final String TITLE = "title";
    public static final String DATE = "date";
    public static final String CONTENT = "content";

    private TextView mTitle;
    private TextView mDate;
    private TextView mContent;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        mTitle = findViewById(R.id.news_title);
        mDate = findViewById(R.id.news_date);
        mContent = findViewById(R.id.news_content);
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(R.string.news_detail_title));
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_back_white);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString(TITLE, "");
        String content = bundle.getString(CONTENT, "");
        String date = bundle.getString(DATE, "");

        mTitle.setText(title);
        mContent.setText(content);
        mDate.setText(date);
    }
}
