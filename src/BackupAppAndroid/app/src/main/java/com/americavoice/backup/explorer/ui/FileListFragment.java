
package com.americavoice.backup.explorer.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.explorer.Const;
import com.americavoice.backup.explorer.helper.FilesHelper;
import com.americavoice.backup.explorer.presenter.FileListPresenter;
import com.americavoice.backup.explorer.ui.adapter.FileAdapter;
import com.americavoice.backup.explorer.ui.adapter.FileLayoutManager;
import com.americavoice.backup.explorer.ui.adapter.SimpleDividerItemDecoration;
import com.americavoice.backup.files.service.FileDownloader;
import com.americavoice.backup.files.service.FileUploader;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.main.ui.activity.BaseOwncloudActivity;
import com.americavoice.backup.main.ui.activity.FileActivity;
import com.owncloud.android.lib.resources.files.RemoteFile;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class FileListFragment extends BaseFragment implements FileListView {
    private static final String ARGUMENT_KEY_PATH = "com.americavoice.backup.ARGUMENT_KEY_PATH";
    private static final int SELECT_VIDEO = 1000;
    private static final int SELECT_PHOTO = 1001;
    private static final int SELECT_DOCUMENT = 1002;


    private UploadFinishReceiver mUploadFinishReceiver;
    private DownloadFinishReceiver mDownloadFinishReceiver;

    /**
     * Interface for listening file list events.
     */
    public interface Listener {
        void onFileClicked(final RemoteFile remoteFile);
        void onFolderClicked(final String path);
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
        showKeyboard(false);
        this.initialize();
        this.loadList();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Listen for upload messages
        IntentFilter uploadIntentFilter = new IntentFilter(FileUploader.getUploadFinishMessage());
        mUploadFinishReceiver = new UploadFinishReceiver();
        getContext().registerReceiver(mUploadFinishReceiver, uploadIntentFilter);

        IntentFilter downloadIntentFilter = new IntentFilter(FileDownloader.getDownloadFinishMessage());
        mDownloadFinishReceiver = new DownloadFinishReceiver();
        getContext().registerReceiver(mDownloadFinishReceiver, downloadIntentFilter);

        this.mPresenter.resume();
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
    }

    private void setupUI() {
        FileLayoutManager layoutManager = new FileLayoutManager(getContext());
        if (this.rvFiles != null) {
            this.rvFiles.setLayoutManager(layoutManager);
            this.rvFiles.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
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
    public void renderList(List<RemoteFile> transactionModelCollection) {
        if (rvFiles != null) {
            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
            this.mAdapter = new FileAdapter(getContext(), new ArrayList<RemoteFile>());
            this.mAdapter.setOnItemClickListener(onItemClickListener);
            this.rvFiles.setAdapter(mAdapter);
            if (transactionModelCollection != null) {
                this.mAdapter.setTransactionCollection(transactionModelCollection);
            }
        }

    }

    @Override
    public void viewDetail(RemoteFile transactionModel) {
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
        hideLoading();
        mProgress = ProgressDialog.show(getActivity(),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.common_uploading),
                true,
                false);
    }
    @Override
    public void showDownloading() {
        hideLoading();
        mProgress = ProgressDialog.show(getActivity(),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.common_downloading),
                true,
                false);
    }

    @Override
    public void hideDLoading() {
        if (mProgress != null) {
            mProgress.hide();
            mProgress.dismiss();
            mProgress = null;
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


    private void loadList() {
        mPath = getArguments().getString(ARGUMENT_KEY_PATH, "/");
        switch (mPath) {
            case Const.Contacts:
                setTitle(getString(R.string.contacts_title));
                break;
            case Const.Documents:
                setTitle(getString(R.string.main_documents));
                break;
            case Const.Photos:
                setTitle(getString(R.string.main_photos));
                break;
            case Const.Videos:
                setTitle(getString(R.string.main_videos));
                break;
            default:
                break;
        }
        if (mPresenter != null ) this.mPresenter.initialize(getContext(), mPath);
    }

    @OnClick(R.id.bt_retry)
    void onButtonRetryClick() {
        hideRetry();
        loadList();
    }

    void onButtonBack() {
        String path = null;
        String subPath = mPath.substring(1, mPath.length() -1);
        String[] splits = subPath.split("/");
        if (splits.length > 1) {
            path = splits[splits.length - 2];
        }

        if (this.mListener != null) this.mListener.onFolderClicked(path);
    }

    @OnClick(R.id.fab_upload)
    void onFabUpload() {
        if (mPath.startsWith(Const.Photos)) {
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, SELECT_PHOTO);
        } else if (mPath.startsWith(Const.Videos)) {
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, SELECT_VIDEO);
        } else if (mPath.startsWith(Const.Documents)) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/*|text/*");
            startActivityForResult(intent, SELECT_DOCUMENT);
        }
    }
    @ Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String selectedPath = null;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PHOTO || requestCode == SELECT_VIDEO || requestCode == SELECT_DOCUMENT)
                selectedPath = FilesHelper.getPath(getContext(), data.getData());
            if(selectedPath != null)
                if (mPresenter != null) mPresenter.onFileUpload(selectedPath);
        }
    }

    private FileAdapter.OnItemClickListener onItemClickListener =
            new FileAdapter.OnItemClickListener() {
                @Override
                public void onItemClicked(RemoteFile file) {
                    if (FileListFragment.this.mPresenter != null && file != null) {
                        FileListFragment.this.mPresenter.onFileClicked(getContext(), file);
                    }
                }
            };



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
                    loadList();
                } else {
                    showToastMessage(getString(R.string.exception_message_generic));
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

                String downloadedRemotePath = intent.getStringExtra(FileDownloader.EXTRA_REMOTE_PATH);
                //TODO: openFile.

            } finally {
                if (intent != null) {
                    getContext().removeStickyBroadcast(intent);
                }
            }
        }
    }
}

