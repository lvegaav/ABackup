
package com.americavoice.backup.sync.ui;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.calls.service.CallsBackupJob;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseAuthenticatorFragment;
import com.americavoice.backup.main.ui.activity.BaseOwncloudActivity;
import com.americavoice.backup.sync.presenter.SyncPresenter;
import com.americavoice.backup.sync.service.SyncBackupJob;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Fragment that shows details of a certain political party.
 */
public class SyncFragment extends BaseAuthenticatorFragment implements SyncView {

    /**
     * Interface for listening submit button.
     */
    public interface Listener {
        void onBackConfirmationClicked();
    }


    @Inject
    SyncPresenter mPresenter;

    @BindView(R.id.btn_sync)
    public Button btnSync;
    @BindView(R.id.tv_sync_photos)
    public TextView tvSyncPhotos;
    @BindView(R.id.tv_sync_videos)
    public TextView tvSyncVideos;

    private Listener mListener;
    private Unbinder mUnBind;
    private BaseOwncloudActivity mContainerActivity;

    public SyncFragment() {
        super();
    }

    public static SyncFragment newInstance() {
        return new SyncFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_sync, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof BaseOwncloudActivity)
            mContainerActivity = ((BaseOwncloudActivity) getActivity());
        this.initialize(savedInstanceState);
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

    private void initialize(Bundle savedInstanceState) {
        this.getComponent(AppComponent.class).inject(this);
        this.mPresenter.setView(this);
        this.mPresenter.initialize(getContext(),mContainerActivity.getAccount());
        super.initialize();

        if (savedInstanceState != null) {
            //TODO:Init Values
        }
    }

    @Override
    public void showLoading() {
        showDialog(getString(R.string.common_loading));
    }

    @Override
    public void hideLoading() {
        hideDialog();
    }

    @Override
    public void showRetry() {
        showDialog(getString(R.string.common_sending));
    }

    @Override
    public void hideRetry() {
        hideDialog();
    }

    @Override
    public void showError(String message) {
        this.showDialogMessage(message);
    }


    @Override
    public Context getContext() {
        return getActivity().getApplicationContext();
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        if (this.mListener != null) this.mListener.onBackConfirmationClicked();
    }

    @OnClick(R.id.btn_sync)
    public void Sync(View view) {
        if (this.mPresenter != null) this.mPresenter.sync();
    }

    @OnClick(R.id.btn_back)
    public void Back(View view) {
        if (this.mListener != null) this.mListener.onBackConfirmationClicked();
    }

    @Override
    public void viewHome() {
        Snackbar.make(btnSync, R.string.contacts_preferences_backup_scheduled, Snackbar.LENGTH_LONG).show();
        if (this.mListener != null) this.mListener.onBackConfirmationClicked();
    }

    @Override
    public void syncJob(List<String> pendingPhotos, List<String> pendingVideos) {
        final Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());

        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putString(SyncBackupJob.ACCOUNT, account.name);
        if (pendingPhotos != null) {
            bundle.putStringArray(SyncBackupJob.PENDING_PHOTOS, pendingPhotos.toArray(new String[pendingPhotos.size()]));
        }
        if (pendingVideos != null) {
            bundle.putStringArray(SyncBackupJob.PENDING_VIDEOS, pendingVideos.toArray(new String[pendingVideos.size()]));
        }
        bundle.putBoolean(SyncBackupJob.FORCE, true);

        new JobRequest.Builder(SyncBackupJob.TAG)
                .setExtras(bundle)
                .setExecutionWindow(3_000L, 10_000L)
                .setRequiresCharging(false)
                .setPersisted(false)
                .setUpdateCurrent(false)
                .build()
                .schedule();

        Toast.makeText(getContext(), R.string.contacts_preferences_backup_scheduled, Toast.LENGTH_LONG).show();

        if (this.mListener != null) this.mListener.onBackConfirmationClicked();
    }

    @Override
    public void totalImages(int count) {
        tvSyncPhotos.setText(String.format(getString(R.string.sync_photos), count));
    }

    @Override
    public void totalVideos(int count) {
        tvSyncVideos.setText(String.format(getString(R.string.sync_videos), count));
    }

}

