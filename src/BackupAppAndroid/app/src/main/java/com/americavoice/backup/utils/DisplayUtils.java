/*
 * Nextcloud Android client application
 *
 * @author Andy Scherzinger
 * @author Bartek Przybylski
 * @author David A. Velasco
 * Copyright (C) 2011  Bartek Przybylski
 * Copyright (C) 2015 ownCloud Inc.
 * Copyright (C) 2016 Andy Scherzinger
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU AFFERO GENERAL PUBLIC LICENSE for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.americavoice.backup.utils;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.style.StyleSpan;

import com.americavoice.backup.R;
import com.americavoice.backup.datamodel.OCFile;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.utils.Log_OC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.IDN;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A helper class for UI/display related operations.
 */
public class DisplayUtils {
    private static final String TAG = DisplayUtils.class.getSimpleName();

    private static final String[] sizeSuffixes = {"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
    private static final int[] sizeScales = {0, 0, 1, 1, 1, 2, 2, 2, 2};
    private static final int RELATIVE_THRESHOLD_WARNING = 90;
    private static final int RELATIVE_THRESHOLD_CRITICAL = 95;
    private static final String MIME_TYPE_UNKNOWN = "Unknown type";

    private static final String HTTP_PROTOCOLL = "http://";
    private static final String HTTPS_PROTOCOLL = "https://";
    private static final String TWITTER_HANDLE_PREFIX = "@";

    private static Map<String, String> mimeType2HumanReadable;

    static {
        mimeType2HumanReadable = new HashMap<>();
        // images
        mimeType2HumanReadable.put("image/jpeg", "JPEG image");
        mimeType2HumanReadable.put("image/jpg", "JPEG image");
        mimeType2HumanReadable.put("image/png", "PNG image");
        mimeType2HumanReadable.put("image/bmp", "Bitmap image");
        mimeType2HumanReadable.put("image/gif", "GIF image");
        mimeType2HumanReadable.put("image/svg+xml", "JPEG image");
        mimeType2HumanReadable.put("image/tiff", "TIFF image");
        // music
        mimeType2HumanReadable.put("audio/mpeg", "MP3 music file");
        mimeType2HumanReadable.put("application/ogg", "OGG music file");
    }

    /**
     * Converts the file size in bytes to human readable output.
     * <ul>
     *     <li>appends a size suffix, e.g. B, KB, MB etc.</li>
     *     <li>rounds the size based on the suffix to 0,1 or 2 decimals</li>
     * </ul>
     *
     * @param bytes Input file size
     * @return something readable like "12 MB", Pending for negative
     * byte values
     */
    public static String bytesToHumanReadable(long bytes) {
        if (bytes < 0) {
            return "Pending";
        } else {
            double result = bytes;
            int suffixIndex = 0;
            while (result > 1024 && suffixIndex < sizeSuffixes.length) {
                result /= 1024.;
                suffixIndex++;
            }

            return new BigDecimal(String.valueOf(result)).setScale(
                    sizeScales[suffixIndex], BigDecimal.ROUND_HALF_UP) + " " + sizeSuffixes[suffixIndex];
        }
    }

    /**
     * Converts MIME types like "image/jpg" to more end user friendly output
     * like "JPG image".
     *
     * @param mimetype MIME type to convert
     * @return A human friendly version of the MIME type, {@link #MIME_TYPE_UNKNOWN} if it can't be converted
     */
    public static String convertMIMEtoPrettyPrint(String mimetype) {
        if (mimeType2HumanReadable.containsKey(mimetype)) {
            return mimeType2HumanReadable.get(mimetype);
        }
        if (mimetype.split("/").length >= 2) {
            return mimetype.split("/")[1].toUpperCase() + " file";
        }
        return MIME_TYPE_UNKNOWN;
    }

    /**
     * Converts Unix time to human readable format
     *
     * @param milliseconds that have passed since 01/01/1970
     * @return The human readable time for the users locale
     */
    public static String unixTimeToHumanReadable(long milliseconds) {
        Date date = new Date(milliseconds);
        DateFormat df = DateFormat.getDateTimeInstance();
        return df.format(date);
    }

    /**
     * beautifies a given URL by removing any http/https protocol prefix.
     *
     * @param url to be beautified url
     * @return beautified url
     */
    public static String beautifyURL(String url) {
        if (url == null) {
            return "";
        }

        if (url.length() >= 7 && url.substring(0, 7).equalsIgnoreCase(HTTP_PROTOCOLL)) {
            return url.substring(HTTP_PROTOCOLL.length());
        }

        if (url.length() >= 8 && url.substring(0, 8).equalsIgnoreCase(HTTPS_PROTOCOLL)) {
            return url.substring(HTTPS_PROTOCOLL.length());
        }

        return url;
    }

    /**
     * beautifies a given twitter handle by prefixing it with an @ in case it is missing.
     *
     * @param handle to be beautified twitter handle
     * @return beautified twitter handle
     */
    public static String beautifyTwitterHandle(String handle) {
        if (handle == null) {
            return "";
        }

        if (handle.startsWith(TWITTER_HANDLE_PREFIX)) {
            return handle;
        } else {
            return TWITTER_HANDLE_PREFIX + handle;
        }
    }

    /**
     * Converts an internationalized domain name (IDN) in an URL to and from ASCII/Unicode.
     *
     * @param url the URL where the domain name should be converted
     * @param toASCII if true converts from Unicode to ASCII, if false converts from ASCII to Unicode
     * @return the URL containing the converted domain name
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static String convertIdn(String url, boolean toASCII) {

        String urlNoDots = url;
        String dots = "";
        while (urlNoDots.startsWith(".")) {
            urlNoDots = url.substring(1);
            dots = dots + ".";
        }

        // Find host name after '//' or '@'
        int hostStart = 0;
        if (urlNoDots.contains("//")) {
            hostStart = url.indexOf("//") + "//".length();
        } else if (url.contains("@")) {
            hostStart = url.indexOf('@') + "@".length();
        }

        int hostEnd = url.substring(hostStart).indexOf("/");
        // Handle URL which doesn't have a path (path is implicitly '/')
        hostEnd = (hostEnd == -1 ? urlNoDots.length() : hostStart + hostEnd);

        String host = urlNoDots.substring(hostStart, hostEnd);
        host = (toASCII ? IDN.toASCII(host) : IDN.toUnicode(host));

        return dots + urlNoDots.substring(0, hostStart) + host + urlNoDots.substring(hostEnd);
    }

    /**
     * creates the display string for an account.
     *
     * @param context the actual activity
     * @param savedAccount the actual, saved account
     * @param accountName the account name
     * @param fallbackString String to be used in case of an error
     * @return the display string for the given account data
     */
    public static String getAccountNameDisplayText(Context context, Account savedAccount, String accountName, String
            fallbackString) {
        try {
            return new OwnCloudAccount(savedAccount, context).getDisplayName()
                    + " @ "
                    + convertIdn(accountName.substring(accountName.lastIndexOf('@') + 1), false);
        } catch (Exception e) {
            Log_OC.w(TAG, "Couldn't get display name for account, using old style");
            return fallbackString;
        }
    }

    /**
     * converts an array of accounts into a set of account names.
     *
     * @param accountList the account array
     * @return set of account names
     */
    public static Set<String> toAccountNameSet(Collection<Account> accountList) {
        Set<String> actualAccounts = new HashSet<>(accountList.size());
        for (Account account : accountList) {
            actualAccounts.add(account.name);
        }
        return actualAccounts;
    }

    /**
     * calculates the relative time string based on the given modificaion timestamp.
     *
     * @param context the app's context
     * @param modificationTimestamp the UNIX timestamp of the file modification time.
     * @return a relative time string
     */
    public static CharSequence getRelativeTimestamp(Context context, long modificationTimestamp) {
        return getRelativeDateTimeString(context, modificationTimestamp, DateUtils.SECOND_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS, 0);
    }

    public static CharSequence getRelativeDateTimeString(
            Context c, long time, long minResolution, long transitionResolution, int flags) {

        CharSequence dateString = "";

        // in Future
        if (time > System.currentTimeMillis()) {
            return DisplayUtils.unixTimeToHumanReadable(time);
        }
        // < 60 seconds -> seconds ago
        else if ((System.currentTimeMillis() - time) < 60 * 1000) {
            return c.getString(R.string.file_list_seconds_ago);
        } else {
            dateString = DateUtils.getRelativeDateTimeString(c, time, minResolution, transitionResolution, flags);
        }

        String[] parts = dateString.toString().split(",");
        if (parts.length == 2) {
            if (parts[1].contains(":") && !parts[0].contains(":")) {
                return parts[0];
            } else if (parts[0].contains(":") && !parts[1].contains(":")) {
                return parts[1];
            }
        }
        //dateString contains unexpected format. fallback: use relative date time string from android api as is.
        return dateString.toString();
    }

    /**
     * Update the passed path removing the last "/" if it is not the root folder.
     *
     * @param path the path to be trimmed
     */
    public static String getPathWithoutLastSlash(String path) {

        // Remove last slash from path
        if (path.length() > 1 && path.charAt(path.length() - 1) == OCFile.PATH_SEPARATOR.charAt(0)) {
            return path.substring(0, path.length() - 1);
        }

        return path;
    }

    /**
     * Gets the screen size in pixels.
     *
     * @param caller Activity calling; needed to get access to the {@link android.view.WindowManager}
     * @return Size in pixels of the screen, or default {@link Point} if caller is null
     */
    public static Point getScreenSize(Activity caller) {
        Point size = new Point();
        if (caller != null) {
            caller.getWindowManager().getDefaultDisplay().getSize(size);
        }
        return size;
    }

    /**
     * styling of given spanText within a given text.
     *
     * @param text     the non styled complete text
     * @param spanText the to be styled text
     * @param style    the style to be applied
     */
    public static SpannableStringBuilder createTextWithSpan(String text, String spanText, StyleSpan style) {
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        int start = text.lastIndexOf(spanText);
        int end = start + spanText.length();
        sb.setSpan(style, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return sb;
    }
    /**
     * Get String data from a InputStream
     *
     * @param inputStream        The File InputStream
     */
    public static String getData(InputStream inputStream) {

        BufferedReader buffreader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder text = new StringBuilder();
        try {
            while ((line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            Log_OC.e(TAG, e.getMessage());
        }
        return text.toString();
    }

    /**
     * Show a temporary message in a Snackbar bound to the content view.
     *
     * @param activity Activity to which's content view the Snackbar is bound.
     * @param messageResource Message to show.
     */
    public static void showSnackMessage(Activity activity, @StringRes int messageResource) {
        Snackbar.make(activity.findViewById(android.R.id.content), messageResource, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Show a temporary message in a Snackbar bound to the content view.
     *
     * @param activity        Activity to which's content view the Snackbar is bound.
     * @param messageResource Resource id for the format string - message to show.
     * @param formatArgs      The format arguments that will be used for substitution.
     */
    public static void showSnackMessage(Activity activity, @StringRes int messageResource, Object... formatArgs) {
        Snackbar.make(
                activity.findViewById(android.R.id.content),
                String.format(activity.getString(messageResource, formatArgs)),
                Snackbar.LENGTH_LONG)
                .show();
    }

    /**
     * Show a temporary message in a Snackbar bound to the content view.
     *
     * @param activity Activity to which's content view the Snackbar is bound.
     * @param message Message to show.
     */
    public static void showSnackMessage(Activity activity, String message) {
        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
