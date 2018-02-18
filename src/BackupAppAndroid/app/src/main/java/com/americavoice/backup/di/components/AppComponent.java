
package com.americavoice.backup.di.components;


import com.americavoice.backup.calls.ui.CallListFragment;
import com.americavoice.backup.calls.ui.CallsBackupFragment;
import com.americavoice.backup.contacts.ui.ContactListFragment;
import com.americavoice.backup.contacts.ui.ContactsBackupFragment;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.di.modules.ActivityModule;
import com.americavoice.backup.di.modules.AppModule;
import com.americavoice.backup.explorer.ui.FileListFragment;
import com.americavoice.backup.login.ui.LoginConfirmationFragment;
import com.americavoice.backup.login.ui.LoginConfirmationView;
import com.americavoice.backup.login.ui.LoginForgotFragment;
import com.americavoice.backup.login.ui.LoginFragment;
import com.americavoice.backup.login.ui.LoginNewPasswordFragment;
import com.americavoice.backup.login.ui.LoginNewPasswordSuccessFragment;
import com.americavoice.backup.login.ui.LoginRegisterFragment;
import com.americavoice.backup.main.ui.MainFragment;
import com.americavoice.backup.main.ui.SplashScreenFragment;
import com.americavoice.backup.music.ui.MusicBackupFragment;
import com.americavoice.backup.news.ui.NewsFragment;
import com.americavoice.backup.payment.ui.ChoosePlanFragment;
import com.americavoice.backup.payment.ui.PaymentMethodFragment;
import com.americavoice.backup.settings.ui.BackupOptionsFragment;
import com.americavoice.backup.settings.ui.StorageInfoFragment;
import com.americavoice.backup.settings.ui.SettingsFragment;
import com.americavoice.backup.sms.ui.SmsBackupFragment;
import com.americavoice.backup.sms.ui.SmsListFragment;

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
    void inject(LoginConfirmationFragment fragment);
    void inject(LoginRegisterFragment fragment);
    void inject(LoginForgotFragment fragment);
    void inject(LoginNewPasswordFragment fragment);
    void inject(LoginNewPasswordSuccessFragment fragment);
    void inject(MainFragment fragment);
    void inject(FileListFragment fragment);
    void inject(SettingsFragment fragment);
    void inject(ContactsBackupFragment fragment);
    void inject(ContactListFragment fragment);
    void inject(CallsBackupFragment fragment);
    void inject(CallListFragment fragment);
    void inject(SmsBackupFragment fragment);
    void inject(SmsListFragment fragment);
    void inject(MusicBackupFragment fragment);
    void inject(StorageInfoFragment fragment);
    void inject(ChoosePlanFragment fragment);
    void inject(PaymentMethodFragment fragment);
    void inject(NewsFragment fragment);
    void inject(BackupOptionsFragment fragment);
}