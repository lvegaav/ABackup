package com.americavoice.backup.contacts.ui;


import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatButton;
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
import com.americavoice.backup.contacts.presenter.ContactsBackupPresenter;
import com.americavoice.backup.contacts.service.ContactsBackupJob;
import com.americavoice.backup.datamodel.ArbitraryDataProvider;
import com.americavoice.backup.datamodel.FileDataStorageManager;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.main.ui.activity.BaseOwncloudActivity;
import com.americavoice.backup.operations.RefreshFolderOperation;
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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.Vector;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ezvcard.Ezvcard;
import ezvcard.VCard;


public class ContactsBackupFragment extends BaseFragment implements ContactsBackupView, DatePickerDialog.OnDateSetListener {

  public static final String PREFERENCE_CONTACTS_AUTOMATIC_BACKUP = "PREFERENCE_CONTACTS_AUTOMATIC_BACKUP";
  public static final String PREFERENCE_CONTACTS_LAST_BACKUP = "PREFERENCE_CONTACTS_LAST_BACKUP";
  public static final String PREFERENCE_CONTACTS_LAST_TOTAL = "PREFERENCE_CONTACTS_LAST_TOTAL";

  public static final String TAG = ContactsBackupFragment.class.getSimpleName();

  /**
   * Interface for listening file list events.
   */
  public interface Listener {
    void onContactsBackPressed();
  }

  @Inject
  ContactsBackupPresenter mPresenter;

  @BindView(R.id.contacts_automatic_backup)
  public SwitchCompat backupSwitch;

  @BindView(R.id.contacts_last_backup_timestamp)
  public TextView lastBackup;

  @BindView(R.id.contacts_datepicker)
  public AppCompatButton contactsDatePickerBtn;

  @BindView(R.id.contacts_backup_now)
  public AppCompatButton contactsBackupNow;

  @BindView(R.id.last_contacts_backup_recyclerview)
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

  private BaseOwncloudActivity mContainerActivity;

  private ContactsBackupAdapter contactsBackupAdapter;
  private ArrayList<VCard> vCards = new ArrayList<>();
  private OCFile ocFile;
  private ArrayList<String> selectableDays;
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

    contactsBackupAdapter = new ContactsBackupAdapter(getContext(), vCards);
    recyclerView.setAdapter(contactsBackupAdapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
    this.initialize();
  }

  @Override
  public void onResume() {
    super.onResume();
    this.mPresenter.resume();

    String backupFolderPath = BaseConstants.CONTACTS_REMOTE_FOLDER;
    refreshBackupFolder(backupFolderPath);
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
    selectableDays = null;
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

    // display last backup
    Long lastBackupTimestamp = arbitraryDataProvider.getLongValue(account, PREFERENCE_CONTACTS_LAST_BACKUP);

    if (lastBackupTimestamp == -1) {
      lastBackup.setText(R.string.contacts_preference_backup_never);
    } else {
      lastBackup.setText(DisplayUtils.getRelativeTimestamp(getActivity(), lastBackupTimestamp));
    }

  }

  void onButtonBack() {
    if (this.mListener != null) {
      this.mListener.onContactsBackPressed();
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

  private void showLastBackup() {
    String backupFolderString = BaseConstants.CONTACTS_REMOTE_FOLDER;
    OCFile backupFolder = mContainerActivity.getStorageManager().getFileByPath(backupFolderString);
    final Vector<OCFile> backupFiles = mContainerActivity.getStorageManager().getFolderContent(
        backupFolder, false);

    selectableDays = new ArrayList<>();

    for (OCFile file : backupFiles) {
      selectableDays.add(file.getFileName());
    }

    Collections.sort(selectableDays, new Comparator<String>() {
      DateFormat f = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");

      @Override
      public int compare(String o1, String o2) {
        try {
          return f.parse(o1).compareTo(f.parse(o2));
        } catch (ParseException e) {
          throw new IllegalArgumentException(e);
        }
      }
    });

    for (OCFile file : backupFiles) {
      if (file.getFileName().equals(selectableDays.get(selectableDays.size() - 1))) {
        ocFile = file;
      }
    }

    try {
      loadLastBackupTask.execute();
    } catch (Exception e) {
      Crashlytics.logException(e);
    }
  }


  private void setLoadingMessage() {
    emptyContentHeadline.setText(R.string.common_loading);
    emptyContentMessage.setText("");

    emptyContentIcon.setVisibility(View.GONE);
    emptyContentProgressBar.setVisibility(View.VISIBLE);
  }

  @Subscribe
  public void onEvent(OnBackPress onBackPress) {
    onButtonBack();
  }

  public static class ContactItemViewHolder extends RecyclerView.ViewHolder {
    private TextView name;

    ContactItemViewHolder(View itemView) {
      super(itemView);

      name = (TextView) itemView.findViewById(R.id.contact_name);
    }

    public TextView getName() {
      return name;
    }

    public void setName(TextView name) {
      this.name = name;
    }

  }


  private boolean checkAndAskForContactsReadPermission() {

    // check permissions
    if ((PermissionUtil.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS))) {
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
    startContactBackupJob(account, false);
  }

  public static void startContactBackupJob(Account account, Boolean isFromSwitch) {

    PersistableBundleCompat bundle = new PersistableBundleCompat();
    bundle.putString(ContactsBackupJob.ACCOUNT, account.name);
    bundle.putBoolean(ContactsBackupJob.IS_FROM_SWITCH, isFromSwitch);

    new JobRequest.Builder(ContactsBackupJob.TAG)
        .setExtras(bundle)
        .setRequiresCharging(false)
        .setPersisted(true)
        .setUpdateCurrent(true)
        .setPeriodic(24 * 60 * 60 * 1000)
        .build()
        .schedule();
  }

  public static void startForcedContactBackupJob(Account account) {

    PersistableBundleCompat bundle = new PersistableBundleCompat();
    bundle.putString(ContactsBackupJob.ACCOUNT, account.name);
    bundle.putBoolean(ContactsBackupJob.FORCE, true);

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
      startContactBackupJob(account, true);
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
            Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());
            arbitraryDataProvider.storeOrUpdateKeyValue(account, PREFERENCE_CONTACTS_AUTOMATIC_BACKUP, String.valueOf(false));
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

  @OnClick(R.id.contacts_datepicker)
  public void openCleanDate() {
    openDate();
  }

  public void openDate() {
    Calendar cal = Calendar.getInstance();
    DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(ContactsBackupFragment.this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

    try {
      Calendar[] selectedDays = getSelectedDay();
      datePickerDialog.setSelectableDays(selectedDays);
      datePickerDialog.show(getActivity().getFragmentManager(), "Datepickerdialog");
    } catch (ParseException e) {
      Crashlytics.logException(e);
    }
  }

  private Calendar[] getSelectedDay() throws ParseException {
    Calendar[] days = new Calendar[selectableDays.size()];
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    for (int i = 0; i < selectableDays.size(); i++) {
      Date date = formatter.parse(selectableDays.get(i).substring(0, 10));
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      days[i] = calendar;
    }

    return days;
  }

  @Override
  public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
    String backupFolderString = BaseConstants.CONTACTS_REMOTE_FOLDER;
    OCFile backupFolder = mContainerActivity.getStorageManager().getFileByPath(backupFolderString);
    Vector<OCFile> backupFiles = mContainerActivity.getStorageManager().getFolderContent(
        backupFolder, false);

    // find file with modification with date and time between 00:00 and 23:59
    // if more than one file exists, take oldest
    Calendar date = Calendar.getInstance();
    date.set(year, monthOfYear, dayOfMonth);

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
          ContactListFragment.newInstance(backupToRestore, mContainerActivity.getAccount()), true, true);
    } else {
      Toast.makeText(getContext(), R.string.contacts_preferences_no_file_found,
          Toast.LENGTH_SHORT).show();
    }
  }

  private void refreshBackupFolder(final String backupFolderPath) {
    final Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());
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
          showLastBackup();
        }
      }
    };

    task.execute(backupFolderPath);
  }

  private AsyncTask loadLastBackupTask = new AsyncTask() {

    @Override
    protected void onPreExecute() {
      setLoadingMessage();
    }

    @Override
    protected Object doInBackground(Object[] params) {
      if (!isCancelled()) {
        if (ocFile != null) {
          File file = new File(ocFile.getStoragePath());
          try {
            vCards.addAll(Ezvcard.parse(file).all());
            Collections.sort(vCards, new ContactListFragment.VCardComparator());
          } catch (IOException e) {
            Log_OC.e(TAG, "IO Exception: " + file.getAbsolutePath());
            return false;
          }
          return true;
        }
      }
      return false;
    }

    @Override
    protected void onPostExecute(Object o) {
      if (!isCancelled()) {
        if (emptyListContainer != null) {
          emptyListContainer.setVisibility(View.GONE);
        }
        contactsBackupAdapter.replaceVCards(vCards);
      }
    }
  };

  public static String getDisplayName(VCard vCard) {
    if (vCard.getFormattedName() != null) {
      return vCard.getFormattedName().getValue();
    } else if (vCard.getTelephoneNumbers() != null && vCard.getTelephoneNumbers().size() > 0) {
      return vCard.getTelephoneNumbers().get(0).getText();
    } else if (vCard.getEmails() != null && vCard.getEmails().size() > 0) {
      return vCard.getEmails().get(0).getValue();
    }

    return "";
  }
}
