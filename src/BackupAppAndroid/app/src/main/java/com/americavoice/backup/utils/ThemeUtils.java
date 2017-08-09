package com.americavoice.backup.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;

import com.americavoice.backup.R;

/**
 * Created by angelchanquin on 8/9/17.
 */

public class ThemeUtils {

    /**
     * set the theme standard colors for the snackbar.
     *
     * @param context  the context relevant for setting the color according to the context's theme
     * @param snackbar the snackbar to be colored
     */
    public static void colorSnackbar(Context context, Snackbar snackbar) {
        // Changing action button text color
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.white));
    }
}
