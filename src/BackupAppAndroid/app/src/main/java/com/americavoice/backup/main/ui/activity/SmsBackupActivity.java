package com.americavoice.backup.main.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.americavoice.backup.R;
import com.americavoice.backup.calls.ui.CallListFragment;
import com.americavoice.backup.calls.ui.CallsBackupFragment;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.di.components.DaggerAppComponent;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.FileFragment;
import com.americavoice.backup.sms.ui.SmsBackupFragment;
import com.americavoice.backup.sms.ui.SmsListFragment;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by angelchanquin on 8/21/17.
 */

public class SmsBackupActivity extends FileActivity implements HasComponent<AppComponent>,
        FileFragment.ContainerActivity,
        SmsBackupFragment.Listener,
        SmsListFragment.Listener{


    private AppComponent mAppComponent;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;


    public static Intent getCallingIntent(Context context) {
        return new Intent(context, SmsBackupActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        this.initializeActivity(savedInstanceState);
        this.initializeInjector();
        this.initializeView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Initializes this activity.
     */
    private void initializeActivity(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            addFragment(R.id.fl_fragment, SmsBackupFragment.newInstance());
        }
    }

    private void initializeView() {
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
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
    public void showDetails(OCFile file) {

    }

    @Override
    public void onBrowsedDownTo(OCFile folder) {

    }

    @Override
    public void onTransferStateChanged(OCFile file, boolean downloading, boolean uploading) {

    }

    @Override
    public void onSmsBackPressed() {
        finish();
    }

    @Override
    public void onSmsListBackPressed() {
        replaceFragment(R.id.fl_fragment, SmsBackupFragment.newInstance(), false, false);
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().post(new OnBackPress());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
