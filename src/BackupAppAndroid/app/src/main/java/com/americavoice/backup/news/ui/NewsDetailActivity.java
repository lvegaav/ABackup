package com.americavoice.backup.news.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.americavoice.backup.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by javier on 9/26/17.
 */

public class NewsDetailActivity extends AppCompatActivity {

    public static final String TITLE = "title";
    public static final String DATE = "date";
    public static final String CONTENT = "content";
    @BindView(R.id.news_title)
    TextView mTitle;
    @BindView(R.id.news_date)
    TextView mDate;
    @BindView(R.id.news_content)
    TextView mContent;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private Unbinder mUnBind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        mUnBind = ButterKnife.bind(this);

        mToolbar.setTitle(getString(R.string.news_detail_title));
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

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String title = bundle.getString(TITLE, "");
            String content = bundle.getString(CONTENT, "");
            String date = bundle.getString(DATE, "");

            mTitle.setText(title);
            mContent.setText(content);
            mDate.setText(date);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnBind.unbind();
    }
}
