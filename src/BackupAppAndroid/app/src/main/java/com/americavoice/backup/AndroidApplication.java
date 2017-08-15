
package com.americavoice.backup;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;

import com.americavoice.backup.db.PreferenceManager;
import com.americavoice.backup.di.components.ApplicationComponent;
import com.americavoice.backup.di.components.DaggerApplicationComponent;
import com.americavoice.backup.di.modules.ApplicationModule;
import com.americavoice.backup.service.NCJobCreator;
import com.americavoice.backup.utils.BaseConstants;
import com.evernote.android.job.JobManager;

/**
 * Android Main Application
 */
public class AndroidApplication extends Application {

    private ApplicationComponent applicationComponent;

    private static String mStoragePath;

    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new NCJobCreator());
//        Fabric.with(this, new Crashlytics());
        //Debug Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build());


        SharedPreferences appPrefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mStoragePath = appPrefs.getString(BaseConstants.PreferenceKeys.STORAGE_PATH, Environment.
                getExternalStorageDirectory().getAbsolutePath());

        this.initializeInjector();
    }

    private void initializeInjector() {
        this.applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return this.applicationComponent;
    }

    public static String getStoragePath() {
        return mStoragePath;
    }
    public static String getDataFolder() {
        return BaseConstants.DATA_FOLDER;
    }
}
