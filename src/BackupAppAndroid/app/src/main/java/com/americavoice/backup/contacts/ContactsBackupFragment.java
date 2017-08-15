package com.americavoice.backup.contacts;


import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.contacts.presenter.ContactsBackupPresenter;
import com.americavoice.backup.contacts.service.ContactsBackupJob;
import com.americavoice.backup.contacts.ui.ContactsBackupView;
import com.americavoice.backup.datamodel.ArbitraryDataProvider;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.utils.PermissionUtil;
import com.americavoice.backup.utils.ThemeUtils;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.greenrobot.eventbus.Subscribe;

import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class ContactsBackupFragment extends BaseFragment implements ContactsBackupView {

    public static final String PREFERENCE_CONTACTS_AUTOMATIC_BACKUP = "PREFERENCE_CONTACTS_AUTOMATIC_BACKUP";
    public static final String PREFERENCE_CONTACTS_LAST_BACKUP = "PREFERENCE_CONTACTS_LAST_BACKUP";

    /**
     * Interface for listening file list events.
     */
    public interface Listener {
        void onContactsBackPressed();
    }

    @Inject
    ContactsBackupPresenter mPresenter;

    @BindView(R.id.tv_title)
    TextView tvTitle;

    @BindView(R.id.contacts_automatic_backup)
    public SwitchCompat backupSwitch;

    @BindView(R.id.contacts_last_backup_timestamp)
    public TextView lastBackup;

    private Unbinder mUnBind;
    private Listener mListener;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;
    private ArbitraryDataProvider arbitraryDataProvider;

    public ContactsBackupFragment() {
        // Required empty public constructor
    }

    public static ContactsBackupFragment newInstance() {
        return new ContactsBackupFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_contacts_backup, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);

        return fragmentView;
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            this.mListener = (Listener) context;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showKeyboard(false);
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
    public void onDestroy() {
        super.onDestroy();
        this.mPresenter.destroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBind.unbind();
    }

    private void initialize() {
        this.getComponent(AppComponent.class).inject(this);
        this.mPresenter.setView(this);
        this.mPresenter.initialize(getString(R.string.contacts_title));

        this.arbitraryDataProvider = new ArbitraryDataProvider(getContext().getContentResolver());

        final Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());
        backupSwitch.setChecked(arbitraryDataProvider.getBooleanValue(account, PREFERENCE_CONTACTS_AUTOMATIC_BACKUP));

        onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkAndAskForContactsReadPermission()) {
                    if (isChecked) {
                        setAutomaticBackup(true);
                    } else {
                        setAutomaticBackup(false);
                    }
                }
            }
        };

        backupSwitch.setOnCheckedChangeListener(onCheckedChangeListener);

    }

    @OnClick(R.id.btn_back)
    void onButtonBack() {
        if (this.mListener != null) {
            this.mListener.onContactsBackPressed();
        }
    }

    @Override
    public void setTitle(String title) {
        tvTitle.setText(title);
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

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        onButtonBack();
    }

    private boolean checkAndAskForContactsReadPermission() {

        // check permissions
        if ((PermissionUtil.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_CONTACTS))) {
            return true;
        } else {
            // Check if we should show an explanation
            if (PermissionUtil.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.READ_CONTACTS)) {
                // Show explanation to the user and then request permission
                Snackbar snackbar = Snackbar.make(getView().findViewById(R.id.contacts_linear_layout),
                        R.string.contacts_read_permission,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.common_ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                                        PermissionUtil.PERMISSIONS_READ_CONTACTS_AUTOMATIC);
                            }
                        });

                ThemeUtils.colorSnackbar(getActivity(), snackbar);

                snackbar.show();

                return false;
            } else {
                // No explanation needed, request the permission.
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                        PermissionUtil.PERMISSIONS_READ_CONTACTS_AUTOMATIC);
                return false;
            }
        }
    }

    public static void startContactBackupJob(Account account) {

        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putString(ContactsBackupJob.ACCOUNT, account.name);

        new JobRequest.Builder(ContactsBackupJob.TAG)
                .setExtras(bundle)
                .setRequiresCharging(false)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .setPeriodic(24 * 60 * 60 * 1000)
                .build()
                .schedule();
    }

    private void startContactsBackupJob() {
        final Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());

        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putString(ContactsBackupJob.ACCOUNT, account.name);
        bundle.putBoolean(ContactsBackupJob.FORCE, true);

        new JobRequest.Builder(ContactsBackupJob.TAG)
                .setExtras(bundle)
                .setExecutionWindow(3_000L, 10_000L)
                .setRequiresCharging(false)
                .setPersisted(false)
                .setUpdateCurrent(false)
                .build()
                .schedule();

        Snackbar.make(getView().findViewById(R.id.contacts_linear_layout),
                R.string.contacts_preferences_backup_scheduled,
                Snackbar.LENGTH_LONG).show();
    }

    public static void cancelContactBackupJobForAccount(Context context, Account account) {

        JobManager jobManager = JobManager.create(context);
        Set<JobRequest> jobs = jobManager.getAllJobRequestsForTag(ContactsBackupJob.TAG);

        for (JobRequest jobRequest : jobs) {
            PersistableBundleCompat extras = jobRequest.getExtras();
            if (extras.getString(ContactsBackupJob.ACCOUNT, "").equalsIgnoreCase(account.name)) {
                jobManager.cancel(jobRequest.getJobId());
            }
        }
    }

    private void setAutomaticBackup(final boolean bool) {

        final Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());

        if (bool) {
            startContactBackupJob(account);
        } else {
            cancelContactBackupJobForAccount(getContext(), account);
        }

        arbitraryDataProvider.storeOrUpdateKeyValue(account, PREFERENCE_CONTACTS_AUTOMATIC_BACKUP,
                String.valueOf(bool));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionUtil.PERMISSIONS_READ_CONTACTS_AUTOMATIC) {
            for (int index = 0; index < permissions.length; index++) {
                if (Manifest.permission.READ_CONTACTS.equalsIgnoreCase(permissions[index])) {
                    if (grantResults[index] >= 0) {
                        setAutomaticBackup(true);
                    } else {
                        backupSwitch.setOnCheckedChangeListener(null);
                        backupSwitch.setChecked(false);
                        backupSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
                    }

                    break;
                }
            }
        }

        if (requestCode == PermissionUtil.PERMISSIONS_READ_CONTACTS_MANUALLY) {
            for (int index = 0; index < permissions.length; index++) {
                if (Manifest.permission.READ_CONTACTS.equalsIgnoreCase(permissions[index])) {
                    if (grantResults[index] >= 0) {
                        startContactsBackupJob();
                    }

                    break;
                }
            }
        }
    }

    @OnClick(R.id.contacts_backup_now)
    public void backupContacts() {
        if (checkAndAskForContactsReadPermission()) {
            startContactsBackupJob();
        }
    }

}
