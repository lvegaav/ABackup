
package com.americavoice.backup.di.components;

import android.content.Context;

import com.americavoice.backup.di.modules.ApplicationModule;
import com.americavoice.backup.main.ui.activity.BaseActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * A component whose lifetime is the life of the application.
 */
@Singleton // Constraints this component to one-per-application or unscoped bindings.
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    void inject(BaseActivity baseActivity);

    //Exposed to sub-graphs.
    Context context();
/*
    ThreadExecutor threadExecutor();

    PostExecutionThread postExecutionThread();

    AppRepository appRepository();*/
}
