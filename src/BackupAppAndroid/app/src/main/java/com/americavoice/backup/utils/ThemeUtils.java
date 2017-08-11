package com.americavoice.backup.utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.widget.ImageButton;

import com.americavoice.backup.R;

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

    /**
     * set the Nextcloud standard colors for the snackbar.
     *
     * @param context  the context relevant for setting the color according to the context's theme
     * @param snackbar the snackbar to be colored
     */
    public static void colorSnackbar(Context context, Snackbar snackbar) {
        // Changing action button text color
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.white));
    }

    public static String colorToHexString(int color) {
        return String.format("#%06X", 0xFFFFFF & color);
    }

}
