package com.americavoice.backup.utils;

import android.accounts.Account;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.ImageButton;

import com.americavoice.backup.AndroidApplication;
import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.datamodel.FileDataStorageManager;
import com.owncloud.android.lib.resources.status.OCCapability;

/**
 * Created by angelchanquin on 8/9/17.
 */

public class ThemeUtils {
    /**
    * sets the tinting of the given ImageButton's icon to color_accent.
    *
    * @param imageButton the image button who's icon should be colored
    */
    public static void colorImageButton(ImageButton imageButton, @ColorInt int color) {
        if (imageButton != null) {
            imageButton.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static int primaryColor(Account account) {
        OCCapability capability = getCapability(account);

        try {
            return Color.parseColor(capability.getServerColor());
        } catch (Exception e) {
            return AndroidApplication.getAppContext().getResources().getColor(R.color.colorPrimary);
        }
    }

    /**
     * set the Nextcloud standard colors for the snackbar.
     *
     * @param context  the context relevant for setting the color according to the context's theme
     * @param snackbar the snackbar to be colored
     */
    public static void colorSnackbar(Context context, Snackbar snackbar) {
        // Changing action button text color
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.colorAccent));
    }

    public static String colorToHexString(int color) {
        return String.format("#%06X", 0xFFFFFF & color);
    }

    public static Drawable tintDrawable(Drawable drawable, int color) {
        if (drawable != null) {
            Drawable wrap = DrawableCompat.wrap(drawable);
            wrap.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

            return wrap;
        } else {
            return drawable;
        }
    }

    private static OCCapability getCapability() {
        return getCapability(null);
    }

    private static OCCapability getCapability(Account acc) {
        Account account;

        if (acc != null) {
            account = acc;
        } else {
            account = AccountUtils.getCurrentOwnCloudAccount(AndroidApplication.getAppContext());
        }

        if (account != null) {
            Context context = AndroidApplication.getAppContext();

            FileDataStorageManager storageManager = new FileDataStorageManager(account, context);
            return storageManager.getCapability(account.name);
        } else {
            return new OCCapability();
        }
    }

}
