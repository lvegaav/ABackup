package com.americavoice.backup.settings.ui;

import static com.americavoice.backup.calls.ui.CallsBackupFragment.PREFERENCE_CALLS_AUTOMATIC_BACKUP;
import static com.americavoice.backup.contacts.ui.ContactsBackupFragment.PREFERENCE_CONTACTS_AUTOMATIC_BACKUP;
import static com.americavoice.backup.explorer.ui.FileListFragment.PREFERENCE_MUSIC_AUTOMATIC_BACKUP;
import static com.americavoice.backup.explorer.ui.FileListFragment.PREFERENCE_PHOTOS_AUTOMATIC_BACKUP;
import static com.americavoice.backup.explorer.ui.FileListFragment.PREFERENCE_VIDEOS_AUTOMATIC_BACKUP;
import static com.americavoice.backup.sms.ui.SmsBackupFragment.PREFERENCE_SMS_AUTOMATIC_BACKUP;

import android.accounts.Account;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.datamodel.ArbitraryDataProvider;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.settings.presenter.BackupOptionsPresenter;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by angelchanquin on 5/12/17.
 */

public class BackupOptionsFragment extends BaseFragment implements BackupOptionsView {

    public static final String PHOTOS_BACKUP_ENABLED_PREFERENCE = "PHOTOS_BACKUP_ENABLED_PREFERENCE";
    public static final String VIDEOS_BACKUP_ENABLED_PREFERENCE = "VIDEOS_BACKUP_ENABLED_PREFERENCE";
    public static final String MUSIC_BACKUP_ENABLED_PREFERENCE = "MUSIC_BACKUP_ENABLED_PREFERENCE";
    public static final String CONTACTS_BACKUP_ENABLED_PREFERENCE = "CONTACTS_BACKUP_ENABLED_PREFERENCE";
    public static final String SMS_BACKUP_ENABLED_PREFERENCE = "SMS_BACKUP_ENABLED_PREFERENCE";
    public static final String CALLS_BACKUP_ENABLED_PREFERENCE = "CALLS_BACKUP_ENABLED_PREFERENCE";

    public interface Listener {
        void onBackBackupOptionsClicked();
    }

    private Listener mListener;
    private Unbinder mUnBind;

    private ArbitraryDataProvider arbitraryDataProvider;
    private Account account;

    @Inject
    BackupOptionsPresenter mPresenter;

    @BindView(R.id.tv_title)
    TextView tvTitle;

    @BindView(R.id.backup_photos)
    SwitchCompat backupPhotos;
    @BindView(R.id.backup_videos)
    SwitchCompat backupVideos;
    @BindView(R.id.backup_music)
    SwitchCompat backupMusic;
    @BindView(R.id.backup_contacts)
    SwitchCompat backupContacts;
    @BindView(R.id.backup_sms)
    SwitchCompat backupSms;
    @BindView(R.id.backup_call_log)
    SwitchCompat backupCalls;

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

        this.account = AccountUtils.getCurrentOwnCloudAccount(getContext());
        this.arbitraryDataProvider = new ArbitraryDataProvider(getContext().getContentResolver());

        backupPhotos.setChecked(arbitraryDataProvider.getBooleanValue(account, PREFERENCE_PHOTOS_AUTOMATIC_BACKUP));
        backupVideos.setChecked(arbitraryDataProvider.getBooleanValue(account, PREFERENCE_VIDEOS_AUTOMATIC_BACKUP));
        backupContacts.setChecked(arbitraryDataProvider.getBooleanValue(account, PREFERENCE_CONTACTS_AUTOMATIC_BACKUP));
        backupMusic.setChecked(arbitraryDataProvider.getBooleanValue(account, PREFERENCE_MUSIC_AUTOMATIC_BACKUP));
        backupSms.setChecked(arbitraryDataProvider.getBooleanValue(account, PREFERENCE_SMS_AUTOMATIC_BACKUP));
        backupCalls.setChecked(arbitraryDataProvider.getBooleanValue(account, PREFERENCE_CALLS_AUTOMATIC_BACKUP));
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

    @OnCheckedChanged(R.id.backup_photos)
    public void onBackupPhotosChange() {
        arbitraryDataProvider.storeOrUpdateKeyValue(account, PREFERENCE_PHOTOS_AUTOMATIC_BACKUP, String.valueOf(backupPhotos.isChecked()));
//        mPresenter.updateBackupOption(R.id.backup_photos, backupPhotos.isChecked());
    }

    @OnCheckedChanged(R.id.backup_videos)
    public void onBackupVideosChange() {
        arbitraryDataProvider.storeOrUpdateKeyValue(account, PREFERENCE_VIDEOS_AUTOMATIC_BACKUP, String.valueOf(backupVideos.isChecked()));
//        mPresenter.updateBackupOption(R.id.backup_videos, backupVideos.isChecked());
    }

    @OnCheckedChanged(R.id.backup_music)
    public void onBackupMusicChange() {
        arbitraryDataProvider.storeOrUpdateKeyValue(account, PREFERENCE_MUSIC_AUTOMATIC_BACKUP, String.valueOf(backupMusic.isChecked()));
//        mPresenter.updateBackupOption(R.id.backup_videos, backupVideos.isChecked());
    }

    @OnCheckedChanged(R.id.backup_contacts)
    public void onBackupContactsChange() {
        arbitraryDataProvider.storeOrUpdateKeyValue(account, PREFERENCE_CONTACTS_AUTOMATIC_BACKUP, String.valueOf(backupContacts.isChecked()));
//        mPresenter.updateBackupOption(R.id.backup_contacts, backupContacts.isChecked());
    }

    @OnCheckedChanged(R.id.backup_sms)
    public void onBackupSmsChange() {
        arbitraryDataProvider.storeOrUpdateKeyValue(account, PREFERENCE_SMS_AUTOMATIC_BACKUP, String.valueOf(backupSms.isChecked()));
//        mPresenter.updateBackupOption(R.id.backup_sms, backupSms.isChecked());
    }

    @OnCheckedChanged(R.id.backup_call_log)
    public void onBackupCallsChange() {
        arbitraryDataProvider.storeOrUpdateKeyValue(account, PREFERENCE_CALLS_AUTOMATIC_BACKUP, String.valueOf(backupCalls.isChecked()));
//        mPresenter.updateBackupOption(R.id.backup_call_log, backupCalls.isChecked());
    }
}
