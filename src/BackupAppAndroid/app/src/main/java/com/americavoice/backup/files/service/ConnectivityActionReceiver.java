/**
 *   ownCloud Android client application
 *
 *   @author LukeOwncloud
 *   Copyright (C) 2016 ownCloud Inc.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2,
 *   as published by the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.americavoice.backup.files.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.americavoice.backup.db.PreferenceManager;
import com.americavoice.backup.db.UploadResult;
import com.owncloud.android.lib.common.utils.Log_OC;

/**
 * Receives all connectivity action from Android OS at all times and performs
 * required OC actions. For now that are: - Signal connectivity to
 * {@link FileUploader}.
 * 
 * Later can be added: - Signal connectivity to download service, deletion
 * service, ... - Handle offline mode (cf.
 * https://github.com/owncloud/android/issues/162)
 *
 * Have fun with the comments :S
 */
public class ConnectivityActionReceiver extends BroadcastReceiver {

    private static final String TAG = ConnectivityActionReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        // LOG ALL EVENTS:
        Log_OC.v(TAG, "action: " + intent.getAction());
        Log_OC.v(TAG, "component: " + intent.getComponent());
        if (isOnline(context)) wifiConnected(context);
    }

    private boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private void wifiConnected(Context context) {
        // for the moment, only recovery of instant uploads, similar to behaviour in release 1.9.1
        Log_OC.d(TAG, "Requesting retry of instant uploads");
        FileUploader.UploadRequester requester = new FileUploader.UploadRequester();
        requester.retryFailedUploads(
                context,
                null,
                UploadResult.NETWORK_CONNECTION     // for the interrupted when Wifi fell, if any
                // (side effect: any upload failed due to network error will be retried too, instant or not)
        );
        requester.retryFailedUploads(
                context,
                null,
                UploadResult.DELAYED_FOR_WIFI       // for the rest of enqueued when Wifi fell
        );
    }
}