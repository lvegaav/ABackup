package com.americavoice.backup.music.presenter;

import android.support.annotation.NonNull;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.music.ui.MusicBackupView;

import javax.inject.Inject;

/**
 * Created by pj on 2/13/18.
 */


/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class MusicBackupPresenter extends BasePresenter implements IPresenter {

    private MusicBackupView mView;

    public void setView(@NonNull MusicBackupView view) {
        this.mView = view;
    }

    @Inject
    public MusicBackupPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
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


    /**
     * Initializes the presenter
     *
     * @param title title of the activity.
     */
    public void initialize(String title) {
        if (this.mView != null) {
            mView.setTitle(title);
        }

    }

    
}
