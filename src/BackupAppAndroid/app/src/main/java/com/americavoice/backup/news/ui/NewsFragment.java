package com.americavoice.backup.news.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.americavoice.backup.R;
import com.americavoice.backup.utils.RecyclerItemClickListener;

import java.util.Arrays;
import java.util.List;

/**
 * Created by javier on 9/26/17.
 * News fragment
 */

public class NewsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new NewsRecyclerAdapter(createFakeNews()));
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                FakeNews fakeNews = (FakeNews) view.getTag();
                Intent intent = new Intent(getContext(), NewsDetailActivity.class);
                Bundle extras = new Bundle();
                extras.putString(NewsDetailActivity.TITLE, fakeNews.title);
                extras.putString(NewsDetailActivity.CONTENT, fakeNews.content);
                extras.putString(NewsDetailActivity.DATE, fakeNews.date);
                intent.putExtras(extras);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
        return view;
    }

    private List<FakeNews> createFakeNews() {
        return Arrays.asList(
                new FakeNews("News 1", "This is an important news", "Today"),
                new FakeNews("News 2", "This is yesterday news", "Yesterday"),
                new FakeNews("Award winner America Voice", "America voice was the winner of the \"App of the year contest\" made by the Google Play Store®", "September 30, 2017")
        );
    }

}
