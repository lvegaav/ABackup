
package com.americavoice.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;
import android.util.Base64;

import com.americavoice.backup.datamodel.ThumbnailsCacheManager;
import com.americavoice.backup.db.PreferenceManager;
import com.americavoice.backup.di.components.ApplicationComponent;
import com.americavoice.backup.di.components.DaggerApplicationComponent;
import com.americavoice.backup.di.modules.ApplicationModule;
import com.americavoice.backup.service.NCJobCreator;
import com.americavoice.backup.utils.BaseConstants;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.evernote.android.job.JobManager;

import java.io.UnsupportedEncodingException;

import io.fabric.sdk.android.Fabric;

/**
 * Android Main Application
 */
public class AndroidApplication extends MultiDexApplication {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private ApplicationComponent applicationComponent;
    private static Context mContext;
    private static String mStoragePath;
    private static Resources.Theme appContext;


    private String serialB1;
    private String serialB2;

    public String getSerialB1() {
        byte[] data = Base64.decode(serialB1, Base64.DEFAULT);
        String text = "";
        try {
            text = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Crashlytics.logException(e);
        }
        return text;
    }

    public void setSerialB1(String serialB1) {
        this.serialB1 = serialB1;
    }

    public String getSerialB2() {
        byte[] data = Base64.decode(serialB2, Base64.DEFAULT);
        String text = "";
        try {
            text = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Crashlytics.logException(e);
        }
        return text;
    }

    public void setSerialB2(String serialB2) {
        this.serialB2 = serialB2;
    }

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

        Fabric.with(this, new Crashlytics());
//        Debug
        Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build());


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
