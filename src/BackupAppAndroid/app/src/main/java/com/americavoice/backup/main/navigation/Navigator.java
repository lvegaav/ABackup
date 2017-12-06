
package com.americavoice.backup.main.navigation;

import android.content.Intent;

import com.americavoice.backup.main.ui.activity.BaseActivity;
import com.americavoice.backup.main.ui.activity.CallsBackupActivity;
import com.americavoice.backup.main.ui.activity.ContactsBackupActivity;
import com.americavoice.backup.main.ui.activity.FileListActivity;
import com.americavoice.backup.main.ui.activity.LoginActivity;
import com.americavoice.backup.main.ui.activity.MainActivity;
import com.americavoice.backup.main.ui.activity.SmsBackupActivity;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Class used to navigate through the application.
 */
@Singleton
public class Navigator {

    @Inject
    public void Navigator() {
        //empty
    }

    /**
     * Goes to the Activity
     *
     * @param context A Context needed to open the destiny activity.
     */
    public void navigateToLoginActivity(BaseActivity context) {
        if (context != null) {
            Intent intentToLaunch = LoginActivity.getCallingIntent(context);
            context.startActivity(intentToLaunch);
        }
    }

    /**
     * Goes to the Activity
     *
     * @param context A Context needed to open the destiny activity.
     */
    public void navigateToMainActivity(BaseActivity context) {
        if (context != null) {
            Intent intentToLaunch = MainActivity.getCallingIntent(context);
            context.startActivity(intentToLaunch);
        }
    }

    public void navigateToFileListActivity(BaseActivity context, String fileType) {
        if (context != null) {
            Intent intentToLaunch = FileListActivity.getCallingIntent(context);
            intentToLaunch.putExtra(FileListActivity.EXTRA_FILE_TYPE, fileType);
            context.startActivity(intentToLaunch);
        }
    }

    public void navigateToContactsBackupActivity(BaseActivity context) {
        if (context != null) {
            Intent intentToLaunch = ContactsBackupActivity.getCallingIntent(context);
            context.startActivity(intentToLaunch);
        }
    }

    public void navigateToCallsBackupActivity(BaseActivity context) {
        if (context != null) {
            Intent intentToLaunch = CallsBackupActivity.getCallingIntent(context);
            context.startActivity(intentToLaunch);
        }
    }

    public void navigateToSmsBackupActivity(BaseActivity context) {
        if (context != null) {
            Intent intentToLaunch = SmsBackupActivity.getCallingIntent(context);
            context.startActivity(intentToLaunch);
        }
    }
}
