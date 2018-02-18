
package com.americavoice.backup.explorer.ui;

import static com.americavoice.backup.main.ui.activity.MusicBackupActivity.SELECT_MUSIC;

import android.accounts.Account;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.datamodel.ArbitraryDataProvider;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.db.PreferenceManager;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.explorer.helper.ExplorerHelper;
import com.americavoice.backup.explorer.presenter.FileListPresenter;
import com.americavoice.backup.explorer.ui.adapter.FileAdapter;
import com.americavoice.backup.explorer.ui.adapter.FileLayoutManager;
import com.americavoice.backup.explorer.ui.adapter.SimpleDividerItemDecoration;
import com.americavoice.backup.explorer.ui.adapter.SpacesItemDecoration;
import com.americavoice.backup.files.service.FileDownloader;
import com.americavoice.backup.files.service.FileUploader;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.main.ui.activity.BaseOwncloudActivity;
import com.americavoice.backup.main.ui.activity.MusicBackupActivity;
import com.americavoice.backup.operations.RemoveFileOperation;
import com.americavoice.backup.payment.ui.PaymentActivity;
import com.americavoice.backup.service.OperationsService;
import com.americavoice.backup.utils.BaseConstants;
import com.americavoice.backup.utils.ConnectivityUtils;
import com.americavoice.backup.utils.RecyclerItemClickListener;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class FileListFragment extends BaseFragment implements FileListView, OnRemoteOperationListener {

    public static final String PREFERENCE_PHOTOS_LAST_TOTAL = "PREFERENCE_PHOTOS_LAST_TOTAL";
    public static final String PREFERENCE_VIDEOS_LAST_TOTAL = "PREFERENCE_VIDEOS_LAST_TOTAL";
    public static final String PREFERENCE_MUSIC_LAST_TOTAL = "PREFERENCE_MUSIC_LAST_TOTAL";
    public static final String PREFERENCE_PHOTOS_AUTOMATIC_BACKUP = "PREFERENCE_PHOTOS_AUTOMATIC_BACKUP";
    public static final String PREFERENCE_VIDEOS_AUTOMATIC_BACKUP = "PREFERENCE_VIDEOS_AUTOMATIC_BACKUP";
    public static final String PREFERENCE_MUSIC_AUTOMATIC_BACKUP = "PREFERENCE_MUSIC_AUTOMATIC_BACKUP";
    public static final String PREFERENCE_STORAGE_ALMOST_FULL = "PREFERENCE_STORAGE_ALMOST_FULL";

    public static final String PREFERENCE_DOCUMENTS_LAST_TOTAL = "PREFERENCE_DOCUMENTS_LAST_TOTAL";
    private static final String ARGUMENT_KEY_PATH = "com.americavoice.backup.ARGUMENT_KEY_PATH";
    private static final int SELECT_VIDEO = 1000;
    private static final int SELECT_PHOTO = 1001;
    private static final int SELECT_DOCUMENT = 1002;


    private UploadFinishReceiver mUploadFinishReceiver;
    private DownloadFinishReceiver mDownloadFinishReceiver;

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;
    private ArbitraryDataProvider arbitraryDataProvider;
    private boolean mShowingTour;

    /**
     * Interface for listening file list events.
     */
    public interface Listener {
        void onFileClicked(final OCFile remoteFile);

        void onFolderClicked(final String path);

        ActionMode startActivityActionMode(ActionMode.Callback actionMode);

        void finishActivityActionMode();
    }

    @Inject
    FileListPresenter mPresenter;

    @BindView(R.id.rv_files)
    RecyclerView rvFiles;
    @BindView(R.id.rl_progress)
    RelativeLayout rlProgress;
    @BindView(R.id.rl_retry)
    RelativeLayout rlRetry;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.fab_upload)
    FloatingActionButton fabUpload;
    @BindView(R.id.ll_automatic_backup)
    RelativeLayout llAutomaticBackup;
    @BindView(R.id.files_automatic_backup)
    public SwitchCompat backupSwitch;

    private Handler mHandler;
    private BaseOwncloudActivity mContainerActivity;
    private FileListFragment.OperationsServiceConnection operationsServiceConnection;
    private OperationsService.OperationsServiceBinder operationsServiceBinder;
    private ActionMode mActionMode;
    boolean isMultiSelect = false;
    private FileAdapter mAdapter;
    private Unbinder mUnBind;
    private Listener mListener;
    private String mPath;

    public FileListFragment() {
        super();
    }

    public static FileListFragment newInstance(String path) {
        FileListFragment fragment = new FileListFragment();
        Bundle argumentsBundle = new Bundle();
        argumentsBundle.putString(ARGUMENT_KEY_PATH, path);
        fragment.setArguments(argumentsBundle);
        return fragment;
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

        View fragmentView = inflater.inflate(R.layout.fragment_file_list, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);
        setupUI();
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new Handler();
        if (getActivity() instanceof BaseOwncloudActivity)
            mContainerActivity = ((BaseOwncloudActivity) getActivity());
        this.initialize();
    }

    @Override
    public void onResume() {
        super.onResume();

        this.mPresenter.resume();
        // Listen for upload messages
        IntentFilter uploadIntentFilter = new IntentFilter(FileUploader.getUploadFinishMessage());
        mUploadFinishReceiver = new UploadFinishReceiver();
        getContext().registerReceiver(mUploadFinishReceiver, uploadIntentFilter);

        IntentFilter downloadIntentFilter = new IntentFilter(FileDownloader.getDownloadFinishMessage());
        mDownloadFinishReceiver = new DownloadFinishReceiver();
        getContext().registerReceiver(mDownloadFinishReceiver, downloadIntentFilter);

    }

    @Override
    public void onPause() {
        if (mUploadFinishReceiver != null) {
            getContext().unregisterReceiver(mUploadFinishReceiver);
            mUploadFinishReceiver = null;
        }
        if (mDownloadFinishReceiver != null) {
            getContext().unregisterReceiver(mDownloadFinishReceiver);
            mDownloadFinishReceiver = null;
        }
        if (operationsServiceBinder != null) {
            operationsServiceBinder.removeOperationListener(this);
        }
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
        if (mPresenter != null) this.mPresenter.setView(this);
        this.loadList();
        if (! ConnectivityUtils.isAppConnected(getContext())) {
            showToastMessage(getString(R.string.common_connectivity_error));
            return;
        }

        this.arbitraryDataProvider = new ArbitraryDataProvider(getContext().getContentResolver());

        final Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());
        boolean backupEnabled = false;
        switch (mPath) {
            case BaseConstants.PHOTOS_REMOTE_FOLDER:
                backupEnabled = arbitraryDataProvider.getBooleanValue(account, PREFERENCE_PHOTOS_AUTOMATIC_BACKUP);
                break;
            case BaseConstants.VIDEOS_REMOTE_FOLDER:
                backupEnabled = arbitraryDataProvider.getBooleanValue(account, PREFERENCE_VIDEOS_AUTOMATIC_BACKUP);
                break;
            case BaseConstants.MUSIC_REMOTE_FOLDER:
                backupEnabled = arbitraryDataProvider.getBooleanValue(account, PREFERENCE_MUSIC_AUTOMATIC_BACKUP);
                break;
            default:
                break;
        }
        backupSwitch.setChecked(backupEnabled);
        onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setAutomaticBackup(true);
                } else {
                    setAutomaticBackup(false);
                }
            }
        };

        backupSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    @Override
    public void showPersistenceUpgrade(int message) {
        Snackbar snackbar = Snackbar.make(
          getActivity().findViewById(R.id.main_content),
          getString(message),
          Snackbar.LENGTH_INDEFINITE)
          .setAction(R.string.storage_upgrade_plan, new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent intent = new Intent(getContext(), PaymentActivity.class);
                  startActivity(intent);
              }
          });
        snackbar.show();
    }

    private void setAutomaticBackup(final boolean bool) {

        final Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());
        switch (mPath) {
            case BaseConstants.PHOTOS_REMOTE_FOLDER:
                if (bool) {
                    startPhotosBackupJob(account);
                } else {
                    cancelPhotosBackupJobForAccount(getContext(), account);
                }
                arbitraryDataProvider.storeOrUpdateKeyValue(account, PREFERENCE_PHOTOS_AUTOMATIC_BACKUP, String.valueOf(bool));
                break;
            case BaseConstants.VIDEOS_REMOTE_FOLDER:
                if (bool) {
                    startVideosBackupJob(account);
                } else {
                    cancelVideosBackupJobForAccount(getContext(), account);
                }
                arbitraryDataProvider.storeOrUpdateKeyValue(account, PREFERENCE_VIDEOS_AUTOMATIC_BACKUP, String.valueOf(bool));
                break;
            case BaseConstants.MUSIC_REMOTE_FOLDER:
                if (bool) {
                    startMusicBackupJob(account);
                } else {
                    cancelMusicBackupJobForAccount(getContext(), account);
                }
                arbitraryDataProvider.storeOrUpdateKeyValue(account, PREFERENCE_MUSIC_AUTOMATIC_BACKUP, String.valueOf(bool));
                break;
            default:
                break;
        }
    }


    private void startMusicBackupJob(Account account) {
    }

    private void cancelMusicBackupJobForAccount(Context context, Account account) {
    }

    private void startPhotosBackupJob(Account account) {
    }

    private void cancelPhotosBackupJobForAccount(Context context, Account account) {
    }

    private void startVideosBackupJob(Account account) {
    }

    private void cancelVideosBackupJobForAccount(Context context, Account account) {
    }

    private void setupUI() {
        if (this.rvFiles != null) {
            mPath = getArguments().getString(ARGUMENT_KEY_PATH, "/");
            switch (mPath) {
                case BaseConstants.DOCUMENTS_REMOTE_FOLDER:
                    this.rvFiles.setLayoutManager(new FileLayoutManager(getContext()));
                    this.rvFiles.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
                    this.llAutomaticBackup.setVisibility(View.GONE);
                    break;
                default:
                    this.rvFiles.setLayoutManager(new GridLayoutManager(getActivity(), 4));
                    int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
                    this.rvFiles.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
                    break;
            }
            rvFiles.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), rvFiles, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (isMultiSelect)
                        multiSelect(position);
                    else {
                        OCFile file = mAdapter.getCollection().get(position);
                        if (FileListFragment.this.mPresenter != null && file != null) {
                            //Download file
                            FileListFragment.this.mPresenter.onFileClicked(mAdapter.getCollection().get(position));
                        }
                    }
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    startActionMode();
                    multiSelect(position);

                }
            }));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.file_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select_files:
                startActionMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startActionMode() {
        if (! isMultiSelect) {
            mAdapter.resetSelectedCollection();
            isMultiSelect = true;
            if (mActionMode == null) {
                mActionMode = mListener.startActivityActionMode(mActionModeCallback);
            }
        }
    }

    @Override
    public void showLoading() {
        if (this.rlProgress != null) {
            this.rlProgress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLoading() {
        if (this.rlProgress != null) {
            this.rlProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void showRetry() {
        if (this.rlRetry != null) {
            this.rlRetry.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideRetry() {
        if (this.rlRetry != null) {
            this.rlRetry.setVisibility(View.GONE);
        }
    }

    @Override
    public void renderList(List<OCFile> transactionModelCollection) {
        if (rvFiles != null) {
            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
            this.mAdapter = new FileAdapter(
              getContext(),
              transactionModelCollection,
              new ArrayList<OCFile>(),
              ((BaseOwncloudActivity) getActivity()).getStorageManager()
            );
            this.rvFiles.setAdapter(mAdapter);
            setHasOptionsMenu(true);
        }

    }

    @Override
    public void showGuidedTour() {
        List<TapTarget> tapTargets = new ArrayList<>();
        if (! mPath.equals(BaseConstants.DOCUMENTS_REMOTE_FOLDER)) {
            tapTargets.add(TapTarget.forView(backupSwitch, getString(R.string.tour_uploads), getString(R.string.tour_files_switch))
              .dimColor(android.R.color.black)
              .outerCircleColor(R.color.blackOpacity80)
              .targetCircleColor(R.color.colorAccent)
              .transparentTarget(true)
              .textColor(android.R.color.white)
              .cancelable(false));
        }
        tapTargets.add(TapTarget.forView(fabUpload, getString(R.string.tour_files), getString(R.string.tour_files_upload))
          .dimColor(android.R.color.black)
          .outerCircleColor(R.color.blackOpacity80)
          .targetCircleColor(R.color.colorAccent)
          .transparentTarget(true)
          .textColor(android.R.color.white)
          .cancelable(false));
        TapTargetSequence sequence = new TapTargetSequence(getActivity())
          .targets((tapTargets.toArray(new TapTarget[0])))
          .listener(new TapTargetSequence.Listener() {
              // This listener will tell us when interesting(tm) events happen in regards
              // to the sequence
              @Override
              public void onSequenceFinish() {
                  // Yay
                  mShowingTour = false;
                  mPresenter.showCaseFinished();
              }

              @Override
              public void onSequenceStep(TapTarget tapTarget, boolean b) {

              }

              @Override
              public void onSequenceCanceled(TapTarget lastTarget) {
                  // Boo
                  mShowingTour = false;
                  mPresenter.showCaseFinished();
              }
          });
        mShowingTour = true;
        sequence.start();
    }

    @Override
    public void downloadFile(OCFile file) {
        showToastMessage(getString(R.string.common_file_will_download));
        List<OCFile> files = new ArrayList<>();
        files.add(file);
        operationsServiceConnection = new FileListFragment.OperationsServiceConnection(files, R.id.action_download);
        getContext().bindService(new Intent(getContext(), OperationsService.class), operationsServiceConnection,
          OperationsService.BIND_AUTO_CREATE);
    }

    @Override
    public void viewDetail(OCFile transactionModel) {
        if (this.mListener != null) {
            this.mListener.onFileClicked(transactionModel);
        }
    }

    @Override
    public void viewFolder(String path) {
        if (this.mListener != null) {
            this.mListener.onFolderClicked(path);
        }
    }

    @Override
    public void renderEmpty() {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText(getString(R.string.files_no_files));
        }
    }

    @Override
    public void showUploading() {
        if (! PreferenceManager.getInstantUploadUsingMobileData(getContext()) && ! ConnectivityUtils.isAppConnectedViaUnmeteredWiFi(getContext())) {
            new AlertDialog.Builder(getActivity(), R.style.WhiteDialog)
              .setTitle(R.string.app_name)
              .setMessage(R.string.common_file_will_upload_when_wifi_or)
              .setPositiveButton(R.string.common_ok, null)
              .setCancelable(false)
              .show();
        } else {
            showToastMessage(getString(R.string.common_uploading));
        }
    }

    @Override
    public void showError(String message) {
        this.showToastMessage(message);
    }

    @Override
    public Context getContext() {
        return this.getActivity().getApplicationContext();
    }

    @Override
    public void notifyDataSetChanged() {
        if (mAdapter != null) {
            this.mAdapter.notifyDataSetChanged();
        }
    }

    private void loadList() {
        showLoading();
        mPath = getArguments().getString(ARGUMENT_KEY_PATH, "/");
        switch (mPath) {
            case BaseConstants.CONTACTS_REMOTE_FOLDER:
                setTitle(getString(R.string.contacts_title));
                break;
            case BaseConstants.DOCUMENTS_REMOTE_FOLDER:
                setTitle(getString(R.string.main_documents));
                break;
            case BaseConstants.PHOTOS_REMOTE_FOLDER:
                setTitle(getString(R.string.main_photos));
                break;
            case BaseConstants.VIDEOS_REMOTE_FOLDER:
                setTitle(getString(R.string.main_videos));
                break;
            case BaseConstants.MUSIC_REMOTE_FOLDER:
                setTitle(getString(R.string.main_music));
                break;
            default:
                break;
        }
        if (mPresenter != null)
            this.mPresenter.initialize(getContext(), mPath, mContainerActivity.getAccount());
    }

    @OnClick(R.id.bt_retry)
    void onButtonRetryClick() {
        if (! ConnectivityUtils.isAppConnected(getContext())) {
            showToastMessage(getString(R.string.common_connectivity_error));
            return;
        }
        hideRetry();
        loadList();
    }

    void onButtonBack() {
        if (mShowingTour) {
            return;
        }
        String path = null;
        String subPath = mPath.substring(1, mPath.length() - 1);
        String[] splits = subPath.split("/");
        if (splits.length > 1) {
            path = splits[splits.length - 2];
        }

        if (this.mListener != null) this.mListener.onFolderClicked(path);
    }

    @OnClick(R.id.fab_upload)
    void onFabUpload() {
        mPresenter.updateRefreshFlag();
        if (mPath.startsWith(BaseConstants.PHOTOS_REMOTE_FOLDER)) {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_PICK);
            startActivityForResult(i, SELECT_PHOTO);
        } else if (mPath.startsWith(BaseConstants.VIDEOS_REMOTE_FOLDER)) {
            Intent i = new Intent();
            i.setType("video/*");
            i.setAction(Intent.ACTION_PICK);
            startActivityForResult(i, SELECT_VIDEO);
        } else if (mPath.startsWith(BaseConstants.MUSIC_REMOTE_FOLDER)) {
            Intent i = new Intent(getActivity(), MusicBackupActivity.class);
            startActivityForResult(i, SELECT_MUSIC);
        } else if (mPath.startsWith(BaseConstants.DOCUMENTS_REMOTE_FOLDER)) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/*|text/*");
            startActivityForResult(intent, SELECT_DOCUMENT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String selectedPath = null;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PHOTO || requestCode == SELECT_VIDEO || requestCode == SELECT_DOCUMENT)
                selectedPath = ExplorerHelper.getPath(getContext(), data.getData());
            if (selectedPath != null && mPresenter != null) {
                mPresenter.onFileUpload(selectedPath);
            }
            if (requestCode == SELECT_MUSIC) {
                ArrayList<String> selectedPaths = data.getStringArrayListExtra("songPaths");
                ArrayList<String> selectedNames = data.getStringArrayListExtra("songNames");
                if (selectedNames != null && selectedPaths != null) {
                    mPresenter.onFileListUpload(selectedPaths, selectedNames);
                }
            }

        }
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        onButtonBack();
    }

    /**
     * Once the file upload has finished -> update view
     */
    private class UploadFinishReceiver extends BroadcastReceiver {
        /**
         * Once the file upload has finished -> update view
         *
         * {@link BroadcastReceiver} to enable upload feedback in UI
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                boolean uploadWasFine = intent.getBooleanExtra(
                  FileUploader.EXTRA_UPLOAD_RESULT,
                  false);
                if (uploadWasFine) {
                    if (mPresenter != null) mPresenter.readRemoteFiles(mPath);
                }

            } finally {
                if (intent != null) {
                    context.removeStickyBroadcast(intent);
                }
            }

        }
    }

    /**
     * Class waiting for broadcast events from the {@link FileDownloader} service.
     * <p/>
     * Updates the UI when a download is started or finished, provided that it is relevant for the
     * current folder.
     */
    private class DownloadFinishReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (mPresenter != null) {
                    mPresenter.readRemoteFiles(mPath);
                    if (intent.hasExtra(FileDownloader.EXTRA_FILE_PATH)) {
                        String remorePath = intent.getStringExtra(FileDownloader.EXTRA_FILE_PATH);
                        mPresenter.onSuccessfulDownload(remorePath);
                    }
                }
            } finally {
                if (intent != null) {
                    getContext().removeStickyBroadcast(intent);
                }
            }
        }
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_multi_select, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                case R.id.action_download:
                    // bind to Operations Service
                    operationsServiceConnection = new FileListFragment.OperationsServiceConnection(mAdapter.getSelectedCollection(), item.getItemId());
                    getContext().bindService(new Intent(getContext(), OperationsService.class), operationsServiceConnection,
                      OperationsService.BIND_AUTO_CREATE);

                    if (mActionMode != null) {
                        mActionMode.finish();
                    }

                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mListener.finishActivityActionMode();
            mActionMode = null;
            mAdapter.resetSelectedCollection();
            isMultiSelect = false;
        }
    };


    public void multiSelect(int position) {
        if (mActionMode != null) {
            mAdapter.addOrRemoveSelectedItem(position);

            if (mAdapter.getSelectedCollection().size() > 0) {
                int size = mAdapter.getSelectedCollection().size();
                mActionMode.setTitle(String.valueOf(size));
                Toast.makeText(getActivity(), (size + " selected"), Toast.LENGTH_SHORT).show();
            } else
                mActionMode.setTitle("");
        }
    }

    /**
     * s
     * Implements callback methods for service binding.
     */
    private class OperationsServiceConnection implements ServiceConnection {
        int mAction = 0;
        List<OCFile> mFiles = null;

        OperationsServiceConnection(List<OCFile> files, int action) {
            mFiles = files;
            mAction = action;
        }

        @Override
        public void onServiceConnected(ComponentName component, IBinder service) {
            if (component.equals(new ComponentName(getContext(), OperationsService.class))) {
                operationsServiceBinder = (OperationsService.OperationsServiceBinder) service;
                operationsServiceBinder.addOperationListener(FileListFragment.this, mHandler);
                Account account = mContainerActivity.getAccount();
                for (OCFile file : mFiles) {
                    switch (mAction) {
                        case R.id.action_delete:
                            delete(file, account);
                            break;
                        case R.id.action_download:
                            download(file, account);
                            break;
                    }
                }
                getContext().unbindService(operationsServiceConnection);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName component) {
            if (component.equals(new ComponentName(getContext(), OperationsService.class))) {
                operationsServiceBinder = null;
            }
        }
    }

    private void delete(OCFile file, Account account) {
        mPresenter.updateRefreshFlag();
        showToastMessage(getString(R.string.common_deleting));
        Intent service = new Intent(getContext(), OperationsService.class);
        service.setAction(OperationsService.ACTION_REMOVE);
        service.putExtra(OperationsService.EXTRA_ACCOUNT, account);
        service.putExtra(OperationsService.EXTRA_REMOTE_PATH, file.getRemotePath());
        service.putExtra(OperationsService.EXTRA_REMOVE_ONLY_LOCAL, false);
        operationsServiceBinder.queueNewOperation(service);
    }

    private void download(OCFile file, Account account) {
        if (! file.isFolder()) {
            Intent intent = new Intent(getContext(), OperationsService.class);
            intent.setAction(OperationsService.ACTION_SYNC_FILE);
            intent.putExtra(OperationsService.EXTRA_ACCOUNT, account);
            intent.putExtra(OperationsService.EXTRA_REMOTE_PATH, file.getRemotePath());
            intent.putExtra(OperationsService.EXTRA_SYNC_FILE_CONTENTS, true);
            operationsServiceBinder.queueNewOperation(intent);
        } else {
            Intent intent = new Intent(getContext(), OperationsService.class);
            intent.setAction(OperationsService.ACTION_SYNC_FOLDER);
            intent.putExtra(OperationsService.EXTRA_ACCOUNT, account);
            intent.putExtra(OperationsService.EXTRA_REMOTE_PATH, file.getRemotePath());
            getContext().startService(intent);
        }
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation remoteOperation, RemoteOperationResult remoteOperationResult) {
        if (remoteOperation instanceof RemoveFileOperation) {
            if (mAdapter != null) {
                mAdapter.removeItem(((RemoveFileOperation) remoteOperation).getFile());
                mAdapter.notifyDataSetChanged();
                if (mAdapter.getItemCount() <= 0) {
                    renderEmpty();
                }
                if (mPresenter != null) {
                    mPresenter.refreshTotal(mAdapter.getItemCount());
                }
            }
        }
    }

}

