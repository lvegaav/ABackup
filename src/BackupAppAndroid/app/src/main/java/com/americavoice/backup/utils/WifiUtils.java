package com.americavoice.backup.utils;

import android.content.Context;

import com.americavoice.backup.db.UploadResult;
import com.americavoice.backup.files.service.FileUploader;
import com.owncloud.android.lib.common.utils.Log_OC;

/**
 * Created by javier on 9/21/17.
 * Wifi utils used by Connectivity Action Receiver and WifiRetryJob
 */

public class WifiUtils {
    private final static String TAG = "WifiUtils";


    public static void wifiConnected(Context context) {
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
