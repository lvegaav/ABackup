
package com.americavoice.backup.di.components;


import com.americavoice.backup.confirmation.ui.ConfirmationFragment;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.di.modules.ActivityModule;
import com.americavoice.backup.di.modules.AppModule;
import com.americavoice.backup.explorer.ui.FileListFragment;
import com.americavoice.backup.login.ui.LoginFragment;
import com.americavoice.backup.main.ui.MainFragment;
import com.americavoice.backup.main.ui.SplashScreenFragment;
import com.americavoice.backup.settings.ui.SettingsFragment;

import dagger.Component;
/**
 * A scope {@link PerActivity} component.
 * Injects user specific Fragments.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = {ActivityModule.class, AppModule.class})
public interface AppComponent extends ActivityComponent {
    void inject(SplashScreenFragment fragment);
    void inject(LoginFragment fragment);
    void inject(ConfirmationFragment fragment);
    void inject(MainFragment fragment);
    void inject(FileListFragment fragment);
    void inject(SettingsFragment fragment);
}