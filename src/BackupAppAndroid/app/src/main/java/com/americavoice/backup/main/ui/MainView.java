package com.americavoice.backup.main.ui;

/**
 * Interface representing a View in a model view presenter (MVP) pattern.
 */
public interface MainView extends ILoadDataView, AppCompatFragmentView {
    void render();
}
