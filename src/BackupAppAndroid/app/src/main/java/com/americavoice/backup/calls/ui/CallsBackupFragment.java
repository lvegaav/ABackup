package com.americavoice.backup.calls.ui;


import android.Manifest;
import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.calls.presenter.CallsBackupPresenter;
import com.americavoice.backup.calls.service.CallsBackupJob;
import com.americavoice.backup.calls.ui.model.Call;
import com.americavoice.backup.datamodel.ArbitraryDataProvider;
import com.americavoice.backup.datamodel.FileDataStorageManager;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.files.service.FileDownloader;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.main.ui.activity.BaseOwncloudActivity;
import com.americavoice.backup.operations.RefreshFolderOperation;
import com.americavoice.backup.utils.BackupCalendarUtils;
import com.americavoice.backup.utils.BaseConstants;
import com.americavoice.backup.utils.DisplayUtils;
import com.americavoice.backup.utils.PermissionUtil;
import com.americavoice.backup.utils.ThemeUtils;
import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class CallsBackupFragment extends BaseFragment implements CallsBackupView, DatePickerDialog.OnDateSetListener {


    public static final String PREFERENCE_CALLS_AUTOMATIC_BACKUP = "PREFERENCE_CALLS_AUTOMATIC_BACKUP";
    public static final String PREFERENCE_CALLS_LAST_BACKUP = "PREFERENCE_CALLS_LAST_BACKUP";
    public static final String PREFERENCE_CALLS_LAST_TOTAL = "PREFERENCE_CALLS_LAST_TOTAL";
    public static final String PREFERENCE_CALLS_IS_NOT_FIRST_TIME = "PREFERENCE_CALLS_IS_NOT_FIRST_TIME";

    private static final String KEY_CALENDAR_PICKER_OPEN = "IS_CALENDAR_PICKER_OPEN";
    private static final String KEY_CALENDAR_DAY = "CALENDAR_DAY";
    private static final String KEY_CALENDAR_MONTH = "CALENDAR_MONTH";
    private static final String KEY_CALENDAR_YEAR = "CALENDAR_YEAR";
    private static final String TAG = CallsBackupFragment.class.getSimpleName();
    private ArrayList<String> selectableDays;

    /**
     * Interface for listening file list events.
     */
    public interface Listener {
        void onCallsBackPressed();
    }

    @Inject
    CallsBackupPresenter mPresenter;

    @BindView(R.id.calls_automatic_backup)
    public SwitchCompat backupSwitch;

    @BindView(R.id.calls_last_backup_timestamp)
    public TextView lastBackup;

    @BindView(R.id.calls_datepicker)
    public AppCompatButton callsDatePickerBtn;

    @BindView(R.id.calls_backup_now)
    public AppCompatButton callsBackupNow;

    @BindView(R.id.calllist_recyclerview)
    public RecyclerView recyclerView;

    @BindView(R.id.empty_list_view_text)
    public TextView emptyContentMessage;

    @BindView(R.id.empty_list_view_headline)
    public TextView emptyContentHeadline;

    @BindView(R.id.empty_list_icon)
    public ImageView emptyContentIcon;

    @BindView(R.id.empty_list_progress)
    public ProgressBar emptyContentProgressBar;

    @BindView(R.id.empty_list_container)
    public RelativeLayout emptyListContainer;

    private CallListAdapter callListAdapter;
    private Account account;
    private ArrayList<Call> mCalls = new ArrayList<>();
    private OCFile ocFile;

    private DownloadFinishReceiver mDownloadFinishReceiver;

    private BaseOwncloudActivity mContainerActivity;

    private Date selectedDate = null;
    private boolean calendarPickerOpen;

    private DatePickerDialog datePickerDialog;

    private Unbinder mUnBind;
    private Listener mListener;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;
    private ArbitraryDataProvider arbitraryDataProvider;

    public CallsBackupFragment() {
        // Required empty public constructor
    }

    public static CallsBackupFragment newInstance() {
        return new CallsBackupFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_calls_backup, container, false);
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
        if (getActivity() instanceof BaseOwncloudActivity)
            mContainerActivity = ((BaseOwncloudActivity) getActivity());
        this.initialize(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mPresenter.resume();

        this.mPresenter.resume();
        if (calendarPickerOpen) {
            openDate();
        }

        String backupFolderPath = BaseConstants.CALLS_REMOTE_FOLDER;
        refreshBackupFolder(backupFolderPath);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDownloadFinishReceiver != null)
            getContext().unregisterReceiver(mDownloadFinishReceiver);
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

    private void initialize(Bundle savedInstanceState) {
        this.getComponent(AppComponent.class).inject(this);
        this.mPresenter.setView(this);
        this.mPresenter.initialize(getString(R.string.calls_title));

        this.arbitraryDataProvider = new ArbitraryDataProvider(getContext().getContentResolver());

        final Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());

        backupSwitch.setChecked(arbitraryDataProvider.getBooleanValue(account, PREFERENCE_CALLS_AUTOMATIC_BACKUP));

        onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkAndAskForCallsReadPermission()) {
                    if (isChecked) {
                        setAutomaticBackup(true);
                    } else {
                        setAutomaticBackup(false);
                    }
                }
            }
        };

        backupSwitch.setOnCheckedChangeListener(onCheckedChangeListener);

        // display last backup
        Long lastBackupTimestamp = arbitraryDataProvider.getLongValue(account, PREFERENCE_CALLS_LAST_BACKUP);

        if (lastBackupTimestamp == -1) {
            lastBackup.setText(R.string.contacts_preference_backup_never);
        } else {
            lastBackup.setText(DisplayUtils.getRelativeTimestamp(getActivity(), lastBackupTimestamp));
        }

        if (savedInstanceState != null && savedInstanceState.getBoolean(KEY_CALENDAR_PICKER_OPEN, false)) {
            if (savedInstanceState.getInt(KEY_CALENDAR_YEAR, -1) != -1 &&
                    savedInstanceState.getInt(KEY_CALENDAR_MONTH, -1) != -1 &&
                    savedInstanceState.getInt(KEY_CALENDAR_DAY, -1) != -1) {
                selectedDate = new Date(savedInstanceState.getInt(KEY_CALENDAR_YEAR),
                        savedInstanceState.getInt(KEY_CALENDAR_MONTH), savedInstanceState.getInt(KEY_CALENDAR_DAY));
            }
            calendarPickerOpen = true;
        }
    }

    void onButtonBack() {
        if (this.mListener != null) {
            this.mListener.onCallsBackPressed();
        }
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

    private boolean checkAndAskForCallsReadPermission() {

        // check permissions
        if ((PermissionUtil.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_CALL_LOG))) {
            return true;
        } else {
            // Check if we should show an explanation
            if (PermissionUtil.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_CALL_LOG)) {
                // Show explanation to the user and then request permission
                Snackbar snackbar = Snackbar.make(getView().findViewById(R.id.calls_linear_layout),
                        R.string.calls_read_permission,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.common_ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG},
                                        PermissionUtil.PERMISSIONS_READ_CALLS_AUTOMATIC);
                            }
                        });

                ThemeUtils.colorSnackbar(getActivity(), snackbar);

                snackbar.show();

                return false;
            } else {
                // No explanation needed, request the permission.
                requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG},
                        PermissionUtil.PERMISSIONS_READ_CALLS_AUTOMATIC);
                return false;
            }
        }
    }

    public static void startCallBackupJob(Account account) {
        startCallBackupJob(account, false);
    }

    public static void startCallBackupJob(Account account, boolean isFromSwitch) {

        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putString(CallsBackupJob.ACCOUNT, account.name);
        bundle.putBoolean(CallsBackupJob.IS_FROM_SWITCH, isFromSwitch);

        new JobRequest.Builder(CallsBackupJob.TAG)
                .setExtras(bundle)
                .setRequiresCharging(false)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .setPeriodic(24 * 60 * 60 * 1000)
                .build()
                .schedule();
    }

    public static void startForcedCallBackupJob(Account account) {

        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putString(CallsBackupJob.ACCOUNT, account.name);
        bundle.putBoolean(CallsBackupJob.FORCE, true);

        new JobRequest.Builder(CallsBackupJob.TAG)
                .setExtras(bundle)
                .setRequiresCharging(false)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .setPeriodic(24 * 60 * 60 * 1000)
                .build()
                .schedule();
    }

    private void startCallsBackupJob() {
        final Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());

        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putString(CallsBackupJob.ACCOUNT, account.name);
        bundle.putBoolean(CallsBackupJob.FORCE, true);

        new JobRequest.Builder(CallsBackupJob.TAG)
                .setExtras(bundle)
                .setExecutionWindow(3_000L, 10_000L)
                .setRequiresCharging(false)
                .setPersisted(false)
                .setUpdateCurrent(false)
                .build()
                .schedule();

        Snackbar.make(getView().findViewById(R.id.calls_linear_layout),
                R.string.contacts_preferences_backup_scheduled,
                Snackbar.LENGTH_LONG).show();
    }

    public static void cancelCallBackupJobForAccount(Context context, Account account) {

        JobManager jobManager = JobManager.create(context);
        Set<JobRequest> jobs = jobManager.getAllJobRequestsForTag(CallsBackupJob.TAG);

        for (JobRequest jobRequest : jobs) {
            PersistableBundleCompat extras = jobRequest.getExtras();
            if (extras.getString(CallsBackupJob.ACCOUNT, "").equalsIgnoreCase(account.name)) {
                jobManager.cancel(jobRequest.getJobId());
            }
        }
    }

    private void setAutomaticBackup(final boolean bool) {

        final Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());

        if (bool) {
            startCallBackupJob(account, true);
        } else {
            cancelCallBackupJobForAccount(getContext(), account);
        }

        arbitraryDataProvider.storeOrUpdateKeyValue(account, PREFERENCE_CALLS_AUTOMATIC_BACKUP,
                String.valueOf(bool));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionUtil.PERMISSIONS_READ_CALLS_AUTOMATIC) {
            for (int index = 0; index < permissions.length; index++) {
                if (Manifest.permission.READ_CALL_LOG.equalsIgnoreCase(permissions[index])) {
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

        if (requestCode == PermissionUtil.PERMISSIONS_READ_CALLS_MANUALLY) {
            for (int index = 0; index < permissions.length; index++) {
                if (Manifest.permission.READ_CALL_LOG.equalsIgnoreCase(permissions[index])) {
                    if (grantResults[index] >= 0) {
                        startCallsBackupJob();
                    }

                    break;
                }
            }
        }
    }

    @OnClick(R.id.calls_backup_now)
    public void backupCalls() {
        if (checkAndAskForCallsReadPermission()) {
            startCallsBackupJob();
        }
    }
    @OnClick(R.id.calls_datepicker)
    public void openCleanDate() {
        openDate();
    }

    public void openDate() {

        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                this,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        try {
            Calendar[] selectedDays = BackupCalendarUtils.getSelectableDaysArray(selectableDays);
            if (selectedDays != null) {
                datePickerDialog.setSelectableDays(selectedDays);
            }
            datePickerDialog.show(getActivity().getFragmentManager(), "Datepickerdialog");
        } catch (ParseException e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int month, int dayOfMonth) {

        selectedDate = new Date(year, month, dayOfMonth);

        String backupFolderString = BaseConstants.CALLS_REMOTE_FOLDER;
        OCFile backupFolder = mContainerActivity.getStorageManager().getFileByPath(backupFolderString);
        Vector<OCFile> backupFiles = mContainerActivity.getStorageManager().getFolderContent(
                backupFolder, false);

        // find file with modification with date and time between 00:00 and 23:59
        // if more than one file exists, take oldest
        Calendar date = Calendar.getInstance();
        date.set(year, month, dayOfMonth);

        // start
        date.set(Calendar.HOUR, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 1);
        date.set(Calendar.MILLISECOND, 0);
        date.set(Calendar.AM_PM, Calendar.AM);
        Long start = date.getTimeInMillis();

        // end
        date.set(Calendar.HOUR, 23);
        date.set(Calendar.MINUTE, 59);
        date.set(Calendar.SECOND, 59);
        Long end = date.getTimeInMillis();

        OCFile backupToRestore = null;

        for (OCFile file : backupFiles) {
            if (start < file.getModificationTimestamp() && end > file.getModificationTimestamp()) {
                if (backupToRestore == null) {
                    backupToRestore = file;
                } else if (backupToRestore.getModificationTimestamp() < file.getModificationTimestamp()) {
                    backupToRestore = file;
                }
            }
        }

        if (backupToRestore != null) {
            mContainerActivity.replaceFragment(R.id.fl_fragment,
                    CallListFragment.newInstance(backupToRestore, mContainerActivity.getAccount()), true, true);
        } else {
            Toast.makeText(getContext(), R.string.contacts_preferences_no_file_found,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (datePickerDialog != null) {
            datePickerDialog.dismiss();
        }
    }

    private void refreshBackupFolder(final String backupFolderPath) {
        final Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());
        @SuppressLint("StaticFieldLeak")
        AsyncTask<String, Integer, Boolean> task = new AsyncTask<String, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(String... path) {
                if (getContext() == null) {
                    return false;
                }
                FileDataStorageManager storageManager = new FileDataStorageManager(account, getContext());

                OCFile folder = storageManager.getFileByPath(path[0]);

                if (folder != null) {
                    RefreshFolderOperation operation = new RefreshFolderOperation(folder, System.currentTimeMillis(),
                            false, false, false, storageManager, account, getContext());

                    RemoteOperationResult result = operation.execute(account, getContext());
                    return result.isSuccess();
                } else {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    OCFile backupFolder = mContainerActivity.getStorageManager().getFileByPath(backupFolderPath);

                    Vector<OCFile> backupFiles = mContainerActivity.getStorageManager()
                            .getFolderContent(backupFolder, false);

                    if (backupFiles != null && backupFiles.size() > 0) {
                        setFile(backupFiles.lastElement());
                    }

                    selectableDays = new ArrayList<>();

                    for (OCFile file : backupFiles) {
                        selectableDays.add(file.getFileName());
                    }
                }
            }
        };

        task.execute(backupFolderPath);
    }

    private void setFile(OCFile file) {
        callListAdapter = new CallListAdapter(getContext(), mCalls);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(callListAdapter);
        recyclerView.setLayoutManager(layoutManager);

        ocFile = file;
        account = AccountUtils.getCurrentOwnCloudAccount(getContext());

        if (!ocFile.isDown()) {
            Intent i = new Intent(getContext(), FileDownloader.class);
            i.putExtra(FileDownloader.EXTRA_ACCOUNT, account);
            i.putExtra(FileDownloader.EXTRA_FILE, ocFile);
            getContext().startService(i);

            // Listen for download messages
            IntentFilter downloadIntentFilter = new IntentFilter(FileDownloader.getDownloadAddedMessage());
            downloadIntentFilter.addAction(FileDownloader.getDownloadFinishMessage());
            mDownloadFinishReceiver = new DownloadFinishReceiver();
            getContext().registerReceiver(mDownloadFinishReceiver, downloadIntentFilter);
        } else {
            try {
                loadCallsTask.execute();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
    }

    private class DownloadFinishReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equalsIgnoreCase(FileDownloader.getDownloadFinishMessage())) {
                    String downloadedRemotePath = intent.getStringExtra(FileDownloader.EXTRA_REMOTE_PATH);

                    FileDataStorageManager storageManager = new FileDataStorageManager(account,
                            getContext());
                    ocFile = storageManager.getFileByPath(downloadedRemotePath);
                    loadCallsTask.execute();
                }
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
    }


    private void setLoadingMessage() {
        emptyContentHeadline.setText(R.string.common_loading);
        emptyContentMessage.setText("");

        emptyContentIcon.setVisibility(View.GONE);
        emptyContentProgressBar.setVisibility(View.VISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    private AsyncTask loadCallsTask = new AsyncTask() {

        @Override
        protected void onPreExecute() {
            setLoadingMessage();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            if (!isCancelled()) {
                try {
                    FileInputStream in = new FileInputStream(ocFile.getStoragePath());
                    Scanner br = new Scanner(new InputStreamReader(in));
                    while (br.hasNext()) {
                        String strLine = br.nextLine();
                        JSONArray jsonArr = new JSONArray(strLine);
                        for (int i = 0; i < jsonArr.length(); i++)
                        {
                            mCalls.add(Call.FromJson(jsonArr.getString(i)));
                        }
                    }
                } catch (IOException e) {
                    Log_OC.e(TAG, "IO Exception: " + ocFile.getStoragePath());
                    return false;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (!isCancelled()) {
                emptyListContainer.setVisibility(View.GONE);
                callListAdapter.replaceList(mCalls);
            }
        }
    };

}
