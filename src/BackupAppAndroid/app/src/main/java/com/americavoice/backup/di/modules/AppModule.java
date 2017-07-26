
package com.americavoice.backup.di.modules;

import android.content.Context;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module that provides user related collaborators.
 */
@Module
public class AppModule {

    public AppModule() {
    }

    @Provides
    @PerActivity
    NetworkProvider provideNetworkProvider(Context context) {
        return new NetworkProvider(context);
    }

    @Provides
    @PerActivity
    SharedPrefsUtils provideSharedPrefsUtils(Context context) {
        return new SharedPrefsUtils(context);
    }
}