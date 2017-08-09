package com.americavoice.backup.contacts;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.americavoice.backup.R;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;

import org.greenrobot.eventbus.Subscribe;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsBackupFragment extends BaseFragment {


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
        return inflater.inflate(R.layout.fragment_contacts_backup, container, false);
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
    }

}
