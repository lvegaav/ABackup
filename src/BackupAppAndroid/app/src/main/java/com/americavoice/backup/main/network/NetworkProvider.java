package com.americavoice.backup.main.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

@Singleton
public class NetworkProvider {
    public static final int COMPANY_ID = 1;
    private static final String KEY_PREFS = "com.americavoice.backup.KEY_PREFS";
    public static final String KEY_PHONE_NUMBER = "com.americavoice.backup.KEY_PHONE_NUMBER";
    private final SharedPreferences mPref;
    private final AndroidServiceClient mClient;
    private final Context mContext;

    private OwnCloudClient mCloudClient;

    private final String mDeviceId;
    private static final String baseUrl = "https://backup.secureip.io";
    private static final String baseUrlOwnCloud = "https://cloud.secureip.io";

    public static String getBaseUrlOwnCloud() {
        return NetworkProvider.baseUrlOwnCloud;
    }

    @Inject
    public NetworkProvider(Context context) {
        mPref = context.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE);
        mClient = new AndroidServiceClient(baseUrl + "/api");
        mContext = context;

        //TODO Ignore self-signed certificate
        IgnoreSelfSigned();

        //Device Id
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String str1 = Build.BOARD + Build.BRAND + Build.CPU_ABI + Build.DEVICE +
                Build.DISPLAY + Build.FINGERPRINT + Build.HOST + Build.ID + Build.MANUFACTURER
                +
                Build.MODEL + Build.PRODUCT + Build.TAGS + Build.TYPE + Build.USER;
        mDeviceId = md5(str1 + androidId);
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

    public OwnCloudClient getCloudClient(String phoneNumber)
    {
        if (mCloudClient == null) {
            Uri serverUri = Uri.parse(baseUrlOwnCloud);
            mCloudClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, mContext, true);
            mCloudClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(getUserName(phoneNumber), mDeviceId));
        }
        return mCloudClient;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public String getUserName(String phoneNumber) {
        return COMPANY_ID + "_" + phoneNumber;
    }

    public void logout() {
        mClient.clearCookies();    //Logout server
    }

    public void login(String phoneNumber, AsyncResult<dtos.AuthenticateResponse> result) {
        mClient.postAsync(new dtos.Authenticate()
                .setProvider("credentials")
                .setUserName(getUserName(phoneNumber))
                .setPassword(mDeviceId), result);
    }

    public void getUser(AsyncResult<dtos.GetFullUserResponse> result) {
        mClient.getAsync(new dtos.GetFullUser(), result);
    }

    public void CustomRegister(dtos.CustomRegister request, AsyncResult<dtos.CustomRegisterResponse> result) {
        mClient.postAsync(request, result);
    }

    public void SendResetPasswordSms(dtos.SendResetPasswordSms request, AsyncResult<dtos.SendResetPasswordSmsResponse> result) {
        mClient.postAsync(request, result);
    }

    public void PerformResetPassword(dtos.PerformResetPassword request, AsyncResult<dtos.PerformResetPasswordResponse> result) {
        mClient.postAsync(request, result);
    }


    private String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return s;
    }
}
