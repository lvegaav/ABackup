package com.americavoice.backup.main.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.americavoice.backup.R;
import com.americavoice.backup.contacts.ui.ContactListFragment;
import com.americavoice.backup.contacts.ui.ContactsBackupFragment;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.di.components.DaggerAppComponent;
import com.americavoice.backup.explorer.Const;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.MainFragment;
import com.americavoice.backup.settings.ui.SettingsFragment;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;


public class MainActivity extends BaseOwncloudActivity implements HasComponent<AppComponent>,
        MainFragment.Listener,
        SettingsFragment.Listener {

    private AppComponent mAppComponent;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initializeActivity(savedInstanceState);
        this.initializeInjector();
        this.initializeView();
    }

    /**
     * Initializes this activity.
     */
    private void initializeActivity(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            addFragment(R.id.fl_fragment, MainFragment.newInstance());
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initializeView() {
    }

    private void initializeInjector() {
        ButterKnife.bind(this);
        this.mAppComponent = DaggerAppComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build();
    }

    @Override
    public AppComponent getComponent() {
        return mAppComponent;
    }

    @Override
    public void viewPhotos() {
        navigator.navigateToFileListActivity(this, Const.Photos);
    }

    @Override
    public void viewVideos() {
        navigator.navigateToFileListActivity(this, Const.Videos);
    }

    @Override
    public void viewContacts() {
        navigator.navigateToContactsBackupActivity(this);
    }

    @Override
    public void viewCalls() {
        navigator.navigateToCallsBackupActivity(this);
    }

    @Override
    public void viewSms() {
        navigator.navigateToSmsBackupActivity(this);
    }

    @Override
    public void viewDocuments() {
        navigator.navigateToFileListActivity(this, Const.Documents);
    }

    @Override
    public void viewSettings() {
        replaceFragment(R.id.fl_fragment, SettingsFragment.newInstance(), true, true);
    }

    @Override
    public void onMainBackPressed() {
        finish();
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().post(new OnBackPress());
    }

    @Override
    public void onBackSettingsClicked() {
        replaceFragment(R.id.fl_fragment, MainFragment.newInstance(), false, false);
    }

}
