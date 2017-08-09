
package com.americavoice.backup;

import android.app.Application;
import android.content.Context;

import com.americavoice.backup.di.components.ApplicationComponent;
import com.americavoice.backup.di.components.DaggerApplicationComponent;
import com.americavoice.backup.di.modules.ApplicationModule;

/**
 * Android Main Application
 */
public class AndroidApplication extends Application {

    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
//        Fabric.with(this, new Crashlytics());
        //Debug Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build());

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
}
