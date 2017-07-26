
package com.americavoice.backup.main.navigation;

import android.content.Intent;

import com.americavoice.backup.main.ui.activity.BaseActivity;
import com.americavoice.backup.main.ui.activity.ConfirmationActivity;
import com.americavoice.backup.main.ui.activity.LoginActivity;
import com.americavoice.backup.main.ui.activity.MainActivity;

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

    /**
     * Goes to the Activity
     *
     * @param context A Context needed to open the destiny activity.
     */
    public void navigateToConfirmationActivity(BaseActivity context) {
        if (context != null) {
            Intent intentToLaunch = ConfirmationActivity.getCallingIntent(context);
            context.startActivity(intentToLaunch);
        }
    }

}
