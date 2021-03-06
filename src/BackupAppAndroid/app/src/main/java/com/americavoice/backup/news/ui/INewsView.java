package com.americavoice.backup.news.ui;

import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.ui.ILoadDataView;

import java.util.List;

/**
 * Created by javier on 10/27/17.
 */

public interface INewsView extends ILoadDataView {
    void setNews(List<dtos.NewsFeed> newsList);
    void showErrorAndClose(Exception ex);

}
