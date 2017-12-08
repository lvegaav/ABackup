package com.americavoice.backup.settings.presenter;

import android.support.annotation.NonNull;

import com.americavoice.backup.R;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.settings.ui.BackupOptionsFragment;
import com.americavoice.backup.settings.ui.BackupOptionsView;

import javax.inject.Inject;

/**
 * Created by angelchanquin on 6/12/17.
 */

@PerActivity
public class BackupOptionsPresenter extends BasePresenter implements IPresenter{

    private BackupOptionsView mView;

    @Inject
    BackupOptionsPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull BackupOptionsView mView) {
        this.mView = mView;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }


    public void updateBackupOption(int id, boolean value) {
        String keyToUpdate = null;
        switch (id) {
            case R.id.backup_photos:
                keyToUpdate = BackupOptionsFragment.PHOTOS_BACKUP_ENABLED_PREFERENCE;
                break;
            case R.id.backup_videos:
                keyToUpdate = BackupOptionsFragment.VIDEOS_BACKUP_ENABLED_PREFERENCE;
                break;
            case R.id.backup_contacts:
                keyToUpdate = BackupOptionsFragment.CONTACTS_BACKUP_ENABLED_PREFERENCE;
                break;
            case R.id.backup_sms:
                keyToUpdate = BackupOptionsFragment.SMS_BACKUP_ENABLED_PREFERENCE;
                break;
            case R.id.backup_call_log:
                keyToUpdate = BackupOptionsFragment.CALLS_BACKUP_ENABLED_PREFERENCE;
                break;
            default:
                keyToUpdate = null;
                break;

        }
        if (keyToUpdate != null) {
            mSharedPrefsUtils.setBooleanPreference(keyToUpdate, value);
        }
    }


}
