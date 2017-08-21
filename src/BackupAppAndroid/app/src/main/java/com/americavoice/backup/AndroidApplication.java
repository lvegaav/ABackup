
package com.americavoice.backup;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.os.IBinder;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.americavoice.backup.datamodel.ThumbnailsCacheManager;
import com.americavoice.backup.db.PreferenceManager;
import com.americavoice.backup.di.components.ApplicationComponent;
import com.americavoice.backup.di.components.DaggerApplicationComponent;
import com.americavoice.backup.di.modules.ApplicationModule;
import com.americavoice.backup.service.NCJobCreator;
import com.americavoice.backup.utils.BaseConstants;
import com.evernote.android.job.JobManager;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

/**
 * Android Main Application
 */
public class AndroidApplication extends MultiDexApplication {

    private ApplicationComponent applicationComponent;
    private static Context mContext;
    private static String mStoragePath;
    private static Resources.Theme appContext;

    public static Context getAppContext() {
        return AndroidApplication.mContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        AndroidApplication.mContext = getApplicationContext();
        JobManager.create(this).addJobCreator(new NCJobCreator());
        // initialise thumbnails cache on background thread
        new ThumbnailsCacheManager.InitDiskCacheTask().execute();

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
