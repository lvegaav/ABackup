/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2017 Tobias Kaminsky
 * Copyright (C) 2017 Nextcloud GmbH.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.americavoice.backup.calls.ui;

import android.Manifest;
import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.americavoice.backup.R;
import com.americavoice.backup.calls.presenter.CallsListPresenter;
import com.americavoice.backup.calls.service.CallsImportJob;
import com.americavoice.backup.calls.ui.model.Call;
import com.americavoice.backup.datamodel.FileDataStorageManager;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.files.service.FileDownloader;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.event.VCardToggleEvent;
import com.americavoice.backup.main.ui.FileFragment;
import com.americavoice.backup.utils.PermissionUtil;
import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ezvcard.Ezvcard;
import ezvcard.VCard;

/**
 * This fragment shows all contacts from a file and allows to import them.
 */
public class CallListFragment extends FileFragment implements CallsListView {

    /**
     * Interface for listening file list events.
     */
    public interface Listener {
        void onCallsListBackPressed();
    }

    @Inject
    CallsListPresenter mPresenter;

    private Unbinder mUnBind;
    private Listener mListener;

    public CallListFragment() {

    }

    public static final String TAG = CallListFragment.class.getSimpleName();

    public static final String FILE_NAME = "FILE_NAME";
    public static final String ACCOUNT = "ACCOUNT";

    @BindView(R.id.calllist_recyclerview)
    public RecyclerView recyclerView;

    @BindView(R.id.calllist_restore_selected_container)
    public LinearLayout restoreContactsContainer;

    @BindView(R.id.calllist_restore_selected)
    public Button restoreContacts;

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

    public static CallListFragment newInstance(OCFile file, Account account) {
        CallListFragment frag = new CallListFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(FILE_NAME, file);
        arguments.putParcelable(ACCOUNT, account);
        frag.setArguments(arguments);

        return frag;
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
        this.initialize(savedInstanceState);
    }

    private void initialize(Bundle savedInstanceState) {
        this.getComponent(AppComponent.class).inject(this);
        this.mPresenter.setView(this);
        this.mPresenter.initialize(getString(R.string.calls_title));
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.calllist_fragment, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);

        callListAdapter = new CallListAdapter(getContext(), mCalls);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(callListAdapter);
        recyclerView.setLayoutManager(layoutManager);

        ocFile = getArguments().getParcelable(FILE_NAME);
        setFile(ocFile);
        account = getArguments().getParcelable(ACCOUNT);

        if (!ocFile.isDown()) {
            Intent i = new Intent(getContext(), FileDownloader.class);
            i.putExtra(FileDownloader.EXTRA_ACCOUNT, account);
            i.putExtra(FileDownloader.EXTRA_FILE, ocFile);
            getContext().startService(i);

            // Listen for download messages
            IntentFilter downloadIntentFilter = new IntentFilter(FileDownloader.getDownloadAddedMessage());
            downloadIntentFilter.addAction(FileDownloader.getDownloadFinishMessage());
            DownloadFinishReceiver mDownloadFinishReceiver = new DownloadFinishReceiver();
            getContext().registerReceiver(mDownloadFinishReceiver, downloadIntentFilter);
        } else {
            try {
                loadCallsTask.execute();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }

        restoreContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkAndAskForContactsWritePermission()) {
                    importCalls();
                }
            }
        });

        restoreContacts.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));

        return fragmentView;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mPresenter.destroy();
    }

    @Override
    public void onResume() {
        this.mPresenter.resume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mPresenter.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (loadCallsTask != null) {
            loadCallsTask.cancel(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBind.unbind();
    }

    void onButtonBack() {
        if (this.mListener != null) {
            this.mListener.onCallsListBackPressed();
        }
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        onButtonBack();
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean retval;

        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                retval = true;
                break;
            default:
                retval = super.onOptionsItemSelected(item);
                break;
        }
        return retval;
    }

    private void setLoadingMessage() {
        emptyContentHeadline.setText(R.string.common_loading);
        emptyContentMessage.setText("");

        emptyContentIcon.setVisibility(View.GONE);
        emptyContentProgressBar.setVisibility(View.VISIBLE);
    }

    static class CallItemViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView duration;
        private TextView date;

        CallItemViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.calllist_item_name);
            duration = (TextView) itemView.findViewById(R.id.calllist_item_duration);
            date = (TextView) itemView.findViewById(R.id.calllist_item_date);

            itemView.setTag(this);
        }

        public void setListener(View.OnClickListener onClickListener) {
            itemView.setOnClickListener(onClickListener);
        }

        public TextView getName() {
            return name;
        }

        public TextView getDuration() {
            return duration;
        }

        public TextView getDate() {
            return date;
        }
    }

    private void importCalls() {
        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putString(CallsImportJob.CALL_FILE_PATH, getFile().getStoragePath());

        new JobRequest.Builder(CallsImportJob.TAG)
                .setExtras(bundle)
                .setExecutionWindow(3_000L, 10_000L)
                .setRequiresCharging(false)
                .setPersisted(false)
                .setUpdateCurrent(false)
                .build()
                .schedule();

        Snackbar.make(recyclerView, R.string.contacts_preferences_import_scheduled, Snackbar.LENGTH_LONG).show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStack();
                } else {
                    getActivity().finish();
                }
            }
        }, 1750);
    }

    private boolean checkAndAskForContactsWritePermission() {
        // check permissions
        if (!PermissionUtil.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALL_LOG)) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CALL_LOG},
                    PermissionUtil.PERMISSIONS_WRITE_CALLS);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionUtil.PERMISSIONS_WRITE_CALLS) {
            for (int index = 0; index < permissions.length; index++) {
                if (Manifest.permission.WRITE_CALL_LOG.equalsIgnoreCase(permissions[index])) {
                    if (grantResults[index] >= 0) {
                        importCalls();
                    } else {
                        if (getView() != null) {
                            Snackbar.make(getView(), R.string.contacts_list_no_permission, Snackbar.LENGTH_LONG)
                                    .show();
                        } else {
                            Toast.makeText(getContext(), R.string.contacts_list_no_permission, Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                }
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
                restoreContacts.setEnabled(true);
                callListAdapter.replaceList(mCalls);
            }
        }
    };

}
