package com.americavoice.backup.settings.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.db.PreferenceManager;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.settings.presenter.BackupOptionsPresenter;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by angelchanquin on 5/12/17.
 */

public class BackupOptionsFragment extends BaseFragment implements BackupOptionsView {

    public static final String PHOTOS_BACKUP_ENABLED_PREFERENCE = "PHOTOS_BACKUP_ENABLED_PREFERENCE";
    public static final String VIDEOS_BACKUP_ENABLED_PREFERENCE = "VIDEOS_BACKUP_ENABLED_PREFERENCE";
    public static final String CONTACTS_BACKUP_ENABLED_PREFERENCE = "CONTACTS_BACKUP_ENABLED_PREFERENCE";
    public static final String SMS_BACKUP_ENABLED_PREFERENCE = "SMS_BACKUP_ENABLED_PREFERENCE";
    public static final String CALLS_BACKUP_ENABLED_PREFERENCE = "CALLS_BACKUP_ENABLED_PREFERENCE";

    public interface Listener {
        void onBackBackupOptionsClicked();
    }

    private Listener mListener;
    private Unbinder mUnBind;

    @Inject
    BackupOptionsPresenter mPresenter;

    @BindView(R.id.tv_title)
    TextView tvTitle;


    public BackupOptionsFragment() {
        super();
    }

    public static BackupOptionsFragment newInstance() {
        return new BackupOptionsFragment();
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            this.mListener = (Listener) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_backup_options, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);

        tvTitle.setText(getText(R.string.backup_options_title));

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.initialize();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mPresenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mPresenter.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBind.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mPresenter.destroy();
    }

    private void initialize() {
        this.getComponent(AppComponent.class).inject(this);
        this.mPresenter.setView(this);
//        this.mPresenter.initialize();
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

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

    @OnClick(R.id.btn_back)
    void onButtonBack() {
        if (this.mListener != null) this.mListener.onBackBackupOptionsClicked();
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        onButtonBack();
    }
}
