
package com.americavoice.backup.di.modules;

import android.content.Context;

import com.americavoice.backup.AndroidApplication;
import com.americavoice.backup.main.navigation.Navigator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module that provides objects which will live during the application lifecycle.
 */
@Module
public class ApplicationModule {
    private final AndroidApplication mApplication;

    public ApplicationModule(AndroidApplication application) {
        this.mApplication = application;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return this.mApplication;
    }

    @Provides
    @Singleton
    Navigator provideNavigator() {
        return new Navigator();
    }
}
