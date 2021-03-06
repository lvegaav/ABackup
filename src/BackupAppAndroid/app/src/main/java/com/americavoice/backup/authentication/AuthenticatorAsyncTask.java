/**
 *   ownCloud Android client application
 *
 *   @author masensio on 09/02/2015.
 *   Copyright (C) 2015 ownCloud Inc.
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
package com.americavoice.backup.authentication;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.network.CertificateCombinedException;
import com.owncloud.android.lib.common.network.NetworkUtils;
import com.owncloud.android.lib.common.network.RedirectionPath;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.files.ExistenceCheckRemoteOperation;
import java.lang.ref.WeakReference;
import java.security.cert.X509Certificate;

/**
 * Async Task to verify the credentials of a user
 */
public class AuthenticatorAsyncTask extends AsyncTask<Object, Void, RemoteOperationResult> {

    private static final String REMOTE_PATH = "/";
    private static final boolean SUCCESS_IF_ABSENT = false;

    private WeakReference<Context> mContextReference;
    private final WeakReference<OnAuthenticatorTaskListener> mListener;

    public AuthenticatorAsyncTask(Fragment fragment) {
        mContextReference = new WeakReference<>(fragment.getContext());
        mListener = new WeakReference<>((OnAuthenticatorTaskListener) fragment);
    }

    @Override
    protected RemoteOperationResult doInBackground(Object... params) {

        RemoteOperationResult result = new RemoteOperationResult(RemoteOperationResult.ResultCode.UNKNOWN_ERROR);
        if (params!= null && params.length==2) {
            Context context = mContextReference.get();
            String url = (String)params[0];
            OwnCloudCredentials credentials = (OwnCloudCredentials)params[1];

            // Client
            Uri uri = Uri.parse(url);
            OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(uri, context, true);
            client.setCredentials(credentials);

            // Operation - try credentials
            ExistenceCheckRemoteOperation operation = new ExistenceCheckRemoteOperation(
                    REMOTE_PATH,
                    SUCCESS_IF_ABSENT
            );

            result = operation.execute(client);
            if (result.getException() != null && result.getException() instanceof CertificateCombinedException)
            {
                try {
                    X509Certificate m509Certificate = ((CertificateCombinedException)result.getException()).getServerCertificate();
                    NetworkUtils.addCertToKnownServersStore(m509Certificate, context);
                } catch (Exception e) {
                    Log_OC.e("", "Server certificate could not be saved in the known-servers trust store ", e);
                } finally {
                    //Try again
                    result = operation.execute(client);
                }
            }

            if (operation.wasRedirected()) {
                RedirectionPath redirectionPath = operation.getRedirectionPath();
                String permanentLocation = redirectionPath.getLastPermanentLocation();
                result.setLastPermanentLocation(permanentLocation);
            }

        }

        return result;
    }

    @Override
    protected void onPostExecute(RemoteOperationResult result) {
        if (result!= null) {
            OnAuthenticatorTaskListener listener = mListener.get();
            if (listener!= null) {
                listener.onAuthenticatorTaskCallback(result);
            }
        }
    }
    /*
     * Interface to retrieve data from recognition task
     */
    public interface OnAuthenticatorTaskListener{
        void onAuthenticatorTaskCallback(RemoteOperationResult result);
    }
}
