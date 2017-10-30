package com.americavoice.backup.main.network;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;

import com.americavoice.backup.authentication.AccountUtils;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;

import net.servicestack.android.AndroidServiceClient;
import net.servicestack.client.AsyncResult;

import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

@Singleton
public class NetworkProvider {

    public static final String KEY_FIRST_TIME = "com.americavoice.backup.KEY_FIRST_TIME";
    private final SharedPreferences mPref;
    private final AndroidServiceClient mClient;
    private final Context mContext;
    private AccountManager mAccountMgr;
    private HashMap<String, String> mDeviceInfo;
    private OwnCloudClient mCloudClient;

    private static final String baseUrl = "http://core-be.development.americavoice.com:8458";
    private static final String baseUrlOwnCloud = "http://backapp-eng.development.americavoice.com";

    public static String getBaseUrlOwnCloud() {
        return NetworkProvider.baseUrlOwnCloud;
    }

    @Inject
    public NetworkProvider(Context context) {
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
        mClient = new AndroidServiceClient(baseUrl + "/api");
        mContext = context;
        mAccountMgr = AccountManager.get(context);

        
        mDeviceInfo = new HashMap<>();
        mDeviceInfo.put("device:brand", Build.MANUFACTURER);
        mDeviceInfo.put("device:model",Build.MODEL);
        mDeviceInfo.put("device:os","Android");
        mDeviceInfo.put("device:osVersion",Build.VERSION.SDK);

        //TODO Ignore self-signed certificate
        IgnoreSelfSigned();
    }


    private void IgnoreSelfSigned() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new X509TrustManager[]{new NullX509TrustManager()}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public OwnCloudClient getCloudClient() {
        Account account = AccountUtils.getCurrentOwnCloudAccount(mContext);

        if (account != null) {
            int lastIndex = account.name.indexOf("@");
            if (lastIndex == -1) {
                lastIndex = account.name.length();
            }
            String username = account.name.substring(0, lastIndex);
            String password = mAccountMgr.getPassword(account);
            Uri serverUri = Uri.parse(baseUrlOwnCloud);
            mCloudClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, mContext, true);
            mCloudClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(username, password ));
        }
        return mCloudClient;
    }

    public OwnCloudClient getLoginCloudClient(String username, String password) {
        if (mCloudClient == null)
        {
            Uri serverUri = Uri.parse(baseUrlOwnCloud);
            mCloudClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, mContext, true);
            mCloudClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(username, password));
        }
        return mCloudClient;
    }

    public void logout() {
        // Logout from account manager
        Account account = AccountUtils.getCurrentOwnCloudAccount(mContext);
        if (account != null) {
            AccountUtils.removeAccount(mContext, account);
        }
        mClient.clearCookies();    //Logout server
    }

    public void login(String username, String password, AsyncResult<dtos.AuthenticateResponse> result) {

        mClient.postAsync(new dtos.Authenticate()
                .setProvider("credentials")
                .setUserName(username)
                .setPassword(password)
                .setMeta(mDeviceInfo), result);
    }

    public void getUser(AsyncResult<dtos.GetFullUserResponse> result) {
        mClient.getAsync(new dtos.GetFullUser(), result);
    }

    public void CustomRegister(dtos.CustomRegister request, AsyncResult<dtos.CustomRegisterResponse> result) {
        mClient.postAsync(request, result);
    }

    public void SendPhoneVerificationCode(AsyncResult<dtos.SendPhoneVerificationCodeResponse> result) {
        mClient.postAsync(new dtos.SendPhoneVerificationCode(), result);
    }

    public void SendPasswordResetCode(dtos.SendPasswordResetCode request, AsyncResult<dtos.SendPasswordResetCodeResponse> result) {
        mClient.postAsync(request, result);
    }

    public void PerformResetPassword(dtos.PerformResetPassword request, AsyncResult<dtos.PerformResetPasswordResponse> result) {
        mClient.postAsync(request, result);
    }

    public void ValidatePhoneVerificationCode(dtos.ValidatePhoneVerificationCode request, AsyncResult<dtos.ValidatePhoneVerificationCodeResponse> result) {
        mClient.postAsync(request, result);
    }
}
