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
import com.americavoice.backup.utils.ConnectivityUtils;
import com.americavoice.backup.utils.WifiUtils;
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
        if (ConnectivityUtils.isAppConnected(context)) WifiUtils.wifiConnected(context);
    }


}