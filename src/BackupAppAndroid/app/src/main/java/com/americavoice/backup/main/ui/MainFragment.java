
package com.americavoice.backup.main.ui;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.americavoice.backup.Const;
import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.calls.ui.CallsBackupFragment;
import com.americavoice.backup.contacts.ui.ContactsBackupFragment;
import com.americavoice.backup.datamodel.ArbitraryDataProvider;
import com.americavoice.backup.db.PreferenceManager;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.presenter.MainPresenter;
import com.americavoice.backup.main.ui.activity.MainActivity;
import com.americavoice.backup.service.MediaContentJob;
import com.americavoice.backup.service.WifiRetryJob;
import com.americavoice.backup.settings.presenter.SettingsPresenter;
import com.americavoice.backup.settings.ui.SettingsView;
import com.americavoice.backup.sms.ui.SmsBackupFragment;
import com.americavoice.backup.sync.service.SyncBackupJob;
import com.americavoice.backup.utils.ConnectivityUtils;
import com.americavoice.backup.utils.PermissionUtil;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import org.greenrobot.eventbus.Subscribe;
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Fragment that shows details of a certain political party.
 */
public class MainFragment extends BaseFragment implements MainView, SettingsView {

    public interface Listener {
        void viewPhotos();
        void viewVideos();
        void viewContacts();
        void viewDocuments();
        void viewCalls();
        void viewSms();
        void viewSettings();
        void viewSync();
        void onMainBackPressed();
    }

    @Inject
    SettingsPresenter mSettingsPresenter;

    @Inject
    MainPresenter mPresenter;

    @BindView(R.id.badge_photos)
    TextView tvBadgePhotos;
    @BindView(R.id.badge_videos)
    TextView tvBadgeVideos;
    @BindView(R.id.badge_contacts)
    TextView tvBadgeContacts;
    @BindView(R.id.badge_documents)
    TextView tvBadgeDocuments;
    @BindView(R.id.badge_sms)
    TextView tvBadgeSms;
    @BindView(R.id.badge_call_log)
    TextView tvBadgeCallLog;

    private Listener mListener;
    private Unbinder mUnBind;

    private RecyclerView.LayoutManager mLayoutManager;

    public MainFragment() {
        super();
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            this.mListener = (Listener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showKeyboard(false);
        this.initialize();
        requestPermissions();
    }

    public void requestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();

        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add(getString(R.string.common_write_external_storage));
        if (!addPermission(permissionsList, Manifest.permission.READ_CONTACTS))
            permissionsNeeded.add(getString(R.string.common_read_contacts));
        if (!addPermission(permissionsList, Manifest.permission.READ_SMS))
            permissionsNeeded.add(getString(R.string.common_read_sms));
        if (!addPermission(permissionsList, Manifest.permission.READ_CALL_LOG))
            permissionsNeeded.add(getString(R.string.common_read_call_log));

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                StringBuilder message = new StringBuilder("You need to grant access to " + permissionsNeeded.get(0));
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message.append(", ").append(permissionsNeeded.get(i));

                showMessageOKCancel(message.toString(),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PermissionUtil.requestMultiplePermission(getActivity(), permissionsList.toArray(new String[permissionsList.size()]));
                            }
                        });
                return;
            }
            PermissionUtil.requestMultiplePermission(getActivity(), permissionsList.toArray(new String[permissionsList.size()]));
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton(getString(R.string.common_ok), okListener)
                .setNegativeButton(getString(R.string.common_cancel), null)
                .create()
                .show();
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (!PermissionUtil.checkSelfPermission(getActivity(), permission)) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (PermissionUtil.shouldShowRequestPermissionRationale(getActivity(), permission))
                return false;
        }
        return true;
    }

    @Override
    public void showRequestPermissionDialog() {
        StringBuilder message = new StringBuilder("You need to grant access to " + getString(R.string.common_write_external_storage));
        showMessageOKCancel(message.toString(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionUtil.requestWriteExternalStoragePermission(getActivity());
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mPresenter.resume();
        this.mSettingsPresenter.showSyncAtFirst();
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
        this.mSettingsPresenter.setView(this);
        this.mPresenter.initialize(getContext(), getString(R.string.main_title));
    }

    @Override
    public void showLoading() {
        if (mProgress != null) {
            mProgress.hide();
            mProgress.dismiss();
            mProgress = null;
        }
        mProgress = ProgressDialog.show(getActivity(),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.common_loading),
                true,
                false);
    }

    @Override
    public void hideLoading() {
        if (mProgress != null) {
            mProgress.hide();
            mProgress.dismiss();
            mProgress = null;
        }
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @OnClick(R.id.ll_calls)
    public void onCalls(View view) {
        if (mListener != null) mListener.viewCalls();
    }

    @OnClick(R.id.ll_sms)
    public void onSms(View view) {
        if (mListener != null) mListener.viewSms();
    }

    @OnClick(R.id.ll_contacts)
    public void onContacts(View view) {
        if (mListener != null) mListener.viewContacts();
    }

    @OnClick(R.id.ll_documents)
    public void onDocuments(View view)
    {
        if (mListener != null) mListener.viewDocuments();
    }

    @OnClick(R.id.ll_photos)
    public void onPhotos(View view)
    {
        if (mListener != null) mListener.viewPhotos();
    }

    @OnClick(R.id.ll_videos)
    public void onVideos(View view)
    {
        if (mListener != null) mListener.viewVideos();
    }

    @OnClick(R.id.btn_settings)
    public void onSettings(View view)
    {
        if (mListener != null) mListener.viewSettings();
    }

    @OnClick(R.id.iv_logo)
    public void onLogo() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Const.ICON_URL));
        startActivity(browserIntent);
    }


    @Override
    public void showError(String message) {
        this.showToastMessage(message);
    }

    @Override
    public Context getContext() {
        return getActivity().getApplicationContext();
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        if (mListener != null) mListener.onMainBackPressed();
    }


    @Override
    public void render() {

    }

    @Override
    public void setBadgePhotos(int size) {
        setBadge(tvBadgePhotos, size);
    }

    @Override
    public void setBadgeVideos(int size) {
        setBadge(tvBadgeVideos, size);
    }

    @Override
    public void setBadgeContacts(int size) {
        setBadge(tvBadgeContacts, size);
    }

    @Override
    public void setBadgeFiles(int size) {
        setBadge(tvBadgeDocuments, size);
    }

    @Override
    public void setBadgeSms(int size) {
        setBadge(tvBadgeSms, size);
    }

    @Override
    public void setBadgeCallLog(int size) {
        setBadge(tvBadgeCallLog, size);
    }

    @Override
    public void showSync() {
        if (mListener != null) mListener.viewSync();
    }

    private void setBadge(TextView tv, int size) {
        if (size < 0) return;
        if (size > 999) size = 999;

        tv.setVisibility(View.VISIBLE);
        tv.setText(String.valueOf(size));
    }

    @Override
    public void showPercent(HashMap<String, BigDecimal> sizes, BigDecimal total, BigDecimal totalAvailable) {

    }

    @Override
    public void showDefaultError() {
        showToastMessage(getString(R.string.exception_message_generic));
    }

    @Override
    public void showGettingPending() {
        hideLoading();
        mProgress = ProgressDialog.show(getActivity(),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.sync_getting_pending_files),
                true,
                false);
    }

    @Override
    public void showSyncDialog(int pendingPhotos, int pendingVideos) {
        String textToDisplay = "";
        if (pendingPhotos == 0 && pendingVideos == 0) {
            if (PreferenceManager.getInstantUploadUsingMobileData(getContext()) && !ConnectivityUtils.isAppConnectedViaUnmeteredWiFi(getContext())){
                textToDisplay = getString(R.string.sync_backup_no_files_pending_warning, getString(R.string.sync_warning_mobile_data_on));
            } else {
                textToDisplay = getString(R.string.sync_backup_no_files_pending);
            }
        } else {
            if (PreferenceManager.getInstantUploadUsingMobileData(getContext()) && !ConnectivityUtils.isAppConnectedViaUnmeteredWiFi(getContext())){
                textToDisplay = getString(R.string.sync_backup_photos_and_videos_warning, pendingPhotos, pendingVideos, getString(R.string.sync_warning_mobile_data_on));
            } else {
                textToDisplay = getString(R.string.sync_backup_photos_and_videos, pendingPhotos, pendingVideos);
            }
        }

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (mSettingsPresenter != null) {
                            mSettingsPresenter.scheduleSync();
                        }
                    }
                })
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (mSettingsPresenter != null) {
                            mSettingsPresenter.setFirstTimeFalse();
                        }
                    }
                })
                .title(R.string.app_name)
                .content(textToDisplay)
                .positiveText(R.string.sync_backup_now)
                .negativeText(R.string.common_cancel);

        MaterialDialog dialog = builder.build();
        dialog.show();
    }

    @Override
    public void scheduleSyncJob(List<String> pendingPhotos, List<String> pendingVideos) {
        final Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());
        if (pendingPhotos.size() > 0 || pendingVideos.size() > 0) {
            PersistableBundleCompat bundle = new PersistableBundleCompat();
            bundle.putString(SyncBackupJob.ACCOUNT, account.name);
            bundle.putStringArray(SyncBackupJob.PENDING_PHOTOS, pendingPhotos.toArray(new String[pendingPhotos.size()]));
            bundle.putStringArray(SyncBackupJob.PENDING_VIDEOS, pendingVideos.toArray(new String[pendingVideos.size()]));
            bundle.putBoolean(SyncBackupJob.FORCE, true);

            new JobRequest.Builder(SyncBackupJob.TAG)
                    .setExtras(bundle)
                    .setExecutionWindow(3_000L, 10_000L)
                    .setRequiresCharging(false)
                    .setPersisted(false)
                    .setUpdateCurrent(false)
                    .build()
                    .schedule();
        }
        ContactsBackupFragment.startForcedContactBackupJob(account);
        SmsBackupFragment.startForcedSmsBackupJob(account);
        CallsBackupFragment.startForcedCallBackupJob(account);
        int messageId = R.string.sync_preferences_backup_scheduled;

        if (!PreferenceManager.getInstantUploadUsingMobileData(getContext()) && !ConnectivityUtils.isAppConnectedViaUnmeteredWiFi(getContext())){
            messageId = R.string.sync_preferences_backup_scheduled_on_wifi;
        }

        Toast.makeText(getContext(), messageId, Toast.LENGTH_LONG).show();
    }
}

