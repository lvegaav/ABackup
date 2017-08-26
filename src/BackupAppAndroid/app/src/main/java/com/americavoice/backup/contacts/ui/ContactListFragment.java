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

package com.americavoice.backup.contacts.ui;

import android.Manifest;
import android.accounts.Account;
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
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import com.americavoice.backup.contacts.presenter.ContactsListPresenter;
import com.americavoice.backup.contacts.service.ContactsImportJob;
import com.americavoice.backup.datamodel.FileDataStorageManager;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.files.service.FileDownloader;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.event.VCardToggleEvent;
import com.americavoice.backup.main.ui.FileFragment;
import com.americavoice.backup.utils.PermissionUtil;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ezvcard.Ezvcard;
import ezvcard.VCard;

/**
 * This fragment shows all contacts from a file and allows to import them.
 */
public class ContactListFragment extends FileFragment implements ContactsListView {

    /**
     * Interface for listening file list events.
     */
    public interface Listener {
        void onContactsListBackPressed();
    }

    @Inject
    ContactsListPresenter mPresenter;

    private Unbinder mUnBind;
    private Listener mListener;

    private Menu mMenu;

    public ContactListFragment() {

    }

    public static final String TAG = ContactListFragment.class.getSimpleName();

    public static final String FILE_NAME = "FILE_NAME";
    public static final String ACCOUNT = "ACCOUNT";

    public static final String CHECKED_ITEMS_ARRAY_KEY = "CHECKED_ITEMS";

    @BindView(R.id.contactlist_recyclerview)
    public RecyclerView recyclerView;

    @BindView(R.id.contactlist_restore_selected_container)
    public LinearLayout restoreContactsContainer;

    @BindView(R.id.contactlist_restore_selected)
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


    private ContactListAdapter contactListAdapter;
    private Account account;
    private ArrayList<VCard> vCards = new ArrayList<>();
    private OCFile ocFile;

    public static ContactListFragment newInstance(OCFile file, Account account) {
        ContactListFragment frag = new ContactListFragment();
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
        showKeyboard(false);
        this.initialize(savedInstanceState);
    }

    private void initialize(Bundle savedInstanceState) {
        this.getComponent(AppComponent.class).inject(this);
        this.mPresenter.setView(this);
        this.mPresenter.initialize(getString(R.string.contacts_title));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.contactlist_menu, menu);
        mMenu = menu;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.contactlist_fragment, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);

        setHasOptionsMenu(true);

        if (savedInstanceState == null) {
            contactListAdapter = new ContactListAdapter(getContext(), vCards);
        } else {
            Set<Integer> checkedItems = new HashSet<>();
            int[] itemsArray = savedInstanceState.getIntArray(CHECKED_ITEMS_ARRAY_KEY);
            for (int i = 0; i < itemsArray.length; i++) {
                checkedItems.add(itemsArray[i]);
            }
            if (checkedItems.size() > 0) {
                onMessageEvent(new VCardToggleEvent(true));
            }
            contactListAdapter = new ContactListAdapter(getContext(), vCards, checkedItems);
        }
        recyclerView.setAdapter(contactListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
            loadContactsTask.execute();
        }

        restoreContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkAndAskForContactsWritePermission()) {
                    getAccountForImport();
                }
            }
        });

        restoreContacts.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(CHECKED_ITEMS_ARRAY_KEY, contactListAdapter.getCheckedIntArray());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VCardToggleEvent event) {
        if (event.showRestoreButton) {
            restoreContactsContainer.setVisibility(View.VISIBLE);
        } else {
            restoreContactsContainer.setVisibility(View.GONE);
        }
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
        if (loadContactsTask != null) {
            loadContactsTask.cancel(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBind.unbind();
    }

    void onButtonBack() {
        if (this.mListener != null) {
            this.mListener.onContactsListBackPressed();
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
            case R.id.action_select_all:
                item.setChecked(!item.isChecked());
                setSelectAllMenuItem(item, item.isChecked());
                contactListAdapter.selectAllFiles(item.isChecked());
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

    private void setSelectAllMenuItem(MenuItem selectAll, boolean checked) {
        selectAll.setChecked(checked);
        if (checked) {
            selectAll.setTitle(R.string.contacts_select_all);
        } else {
            selectAll.setTitle(R.string.contacts_select_none);
        }
    }

    static class ContactItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView badge;
        private CheckedTextView name;

        ContactItemViewHolder(View itemView) {
            super(itemView);

            badge = (ImageView) itemView.findViewById(R.id.contactlist_item_icon);
            name = (CheckedTextView) itemView.findViewById(R.id.contactlist_item_name);


            itemView.setTag(this);
        }

        public void setVCardListener(View.OnClickListener onClickListener) {
            itemView.setOnClickListener(onClickListener);
        }

        public ImageView getBadge() {
            return badge;
        }

        public void setBadge(ImageView badge) {
            this.badge = badge;
        }

        public CheckedTextView getName() {
            return name;
        }

        public void setName(CheckedTextView name) {
            this.name = name;
        }
    }

    private void importContacts(ContactAccount account) {
        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putString(ContactsImportJob.ACCOUNT_NAME, account.name);
        bundle.putString(ContactsImportJob.ACCOUNT_TYPE, account.type);
        bundle.putString(ContactsImportJob.VCARD_FILE_PATH, getFile().getStoragePath());
        bundle.putIntArray(ContactsImportJob.CHECKED_ITEMS_ARRAY, contactListAdapter.getCheckedIntArray());

        new JobRequest.Builder(ContactsImportJob.TAG)
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

    private void getAccountForImport() {
        final ArrayList<ContactAccount> accounts = new ArrayList<>();

        // add local one
        accounts.add(new ContactAccount("Local contacts", null, null));

        Cursor cursor = null;
        try {
            cursor = getContext().getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                    new String[]{ContactsContract.RawContacts.ACCOUNT_NAME, ContactsContract.RawContacts.ACCOUNT_TYPE},
                    null,
                    null,
                    null);

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
                    String type = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));

                    ContactAccount account = new ContactAccount(name, name, type);

                    if (!accounts.contains(account)) {
                        accounts.add(account);
                    }
                }

                cursor.close();
            }
        } catch (Exception e) {
            Log_OC.d(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (accounts.size() == 1) {
            importContacts(accounts.get(0));
        } else {
            ArrayAdapter adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, accounts);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.contacts_list_account_chooser_title)
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            importContacts(accounts.get(which));
                        }
                    }).show();
        }
    }

    private boolean checkAndAskForContactsWritePermission() {
        // check permissions
        if (!PermissionUtil.checkSelfPermission(getContext(), Manifest.permission.WRITE_CONTACTS)) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS},
                    PermissionUtil.PERMISSIONS_WRITE_CONTACTS);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionUtil.PERMISSIONS_WRITE_CONTACTS) {
            for (int index = 0; index < permissions.length; index++) {
                if (Manifest.permission.WRITE_CONTACTS.equalsIgnoreCase(permissions[index])) {
                    if (grantResults[index] >= 0) {
                        getAccountForImport();
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

    private class ContactAccount {
        private String displayName;
        private String name;
        private String type;

        ContactAccount(String displayName, String name, String type) {
            this.displayName = displayName;
            this.name = name;
            this.type = type;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ContactAccount) {
                ContactAccount other = (ContactAccount) obj;
                return this.name.equalsIgnoreCase(other.name) && this.type.equalsIgnoreCase(other.type);
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private class DownloadFinishReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(FileDownloader.getDownloadFinishMessage())) {
                String downloadedRemotePath = intent.getStringExtra(FileDownloader.EXTRA_REMOTE_PATH);

                FileDataStorageManager storageManager = new FileDataStorageManager(account,
                        getContext());
                ocFile = storageManager.getFileByPath(downloadedRemotePath);
                loadContactsTask.execute();
            }
        }
    }

    public static class VCardComparator implements Comparator<VCard> {
        @Override
        public int compare(VCard o1, VCard o2) {
            String contac1 = getDisplayName(o1);
            String contac2 = getDisplayName(o2);

            return contac1.compareToIgnoreCase(contac2);
        }


    }

    private AsyncTask loadContactsTask = new AsyncTask() {

        @Override
        protected void onPreExecute() {
            setLoadingMessage();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            if (!isCancelled()) {
                File file = new File(ocFile.getStoragePath());
                try {
                    vCards.addAll(Ezvcard.parse(file).all());
                    Collections.sort(vCards, new VCardComparator());
                } catch (IOException e) {
                    Log_OC.e(TAG, "IO Exception: " + file.getAbsolutePath());
                    return false;
                }
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (!isCancelled()) {
                emptyListContainer.setVisibility(View.GONE);
                contactListAdapter.replaceVCards(vCards);
                if (mMenu != null) {
                    onOptionsItemSelected(mMenu.findItem(R.id.action_select_all));
                }
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
