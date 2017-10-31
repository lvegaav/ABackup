package com.americavoice.backup.main.network;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
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
        Log.d("Network", "calling login");
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
        Log.d("Network", "calling register");
        mClient.postAsync(request, result);
    }

    public void SendPhoneVerificationCode(AsyncResult<dtos.SendPhoneVerificationCodeResponse> result) {
        mClient.postAsync(new dtos.SendPhoneVerificationCode(), result);
    }

    public void SendPasswordResetCode(dtos.SendPasswordResetCode request, AsyncResult<dtos.SendPasswordResetCodeResponse> result) {
        Log.d("Network", "calling reset pass");
        mClient.postAsync(request, result);
    }

    public void PerformResetPassword(dtos.PerformResetPassword request, AsyncResult<dtos.PerformResetPasswordResponse> result) {
        mClient.postAsync(request, result);
    }

    public void getPaymentMethod(AsyncResult<dtos.GetPaymentMethodResponse> result) {
        Log.d("Network", "Calling get payment method");
        mClient.getAsync(new dtos.GetPaymentMethod(), result);
    }

    public void getPaypalToken(AsyncResult<dtos.GetPayPalTokenResponse> result) {
        mClient.getAsync(new dtos.GetPayPalToken(), result);
    }

    public void sendPayPalNonce(String nonce, AsyncResult<dtos.CreatePayPalPaymentMethodResponse> response) {
        dtos.CreatePayPalPaymentMethod request = new dtos.CreatePayPalPaymentMethod();
        request.setNonce(nonce);
        mClient.postAsync(request, response);
    }

    public void createCreditCardPaymentMethod(dtos.CreateCreditCardPaymentMethod request,
                                              AsyncResult<dtos.CreateCreditCardPaymentMethodResponse> response) {
        mClient.postAsync(request, response);
    }

    public void getNewsFeed(AsyncResult<dtos.GetNewsFeedResponse> response) {
        Log.d("Network", "calling get news feed");
        dtos.GetNewsFeed request = new dtos.GetNewsFeed();
        request.setTake(25);
        mClient.getAsync(request, response);
    }

    public void ValidatePhoneVerificationCode(dtos.ValidatePhoneVerificationCode request, AsyncResult<dtos.ValidatePhoneVerificationCodeResponse> result) {
        mClient.postAsync(request, result);
    }

    public void getProducts(AsyncResult<dtos.GetProductsResponse> result) {
        mClient.getAsync(new dtos.GetProducts(), result);
    }

    public void getCurrentSubscription(AsyncResult<dtos.GetSubscriptionResponse> result) {
        mClient.getAsync(new dtos.GetSubscription(), result);
    }

    public void createSubscription(dtos.CreateSubscription request, AsyncResult<dtos.CreateSubscriptionResponse> response) {
        mClient.postAsync(request, response);
    }

    public void changeSubscription(dtos.ChangeSubscription request, AsyncResult<dtos.ChangeSubscriptionResponse> response) {
        mClient.postAsync(request, response);
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
                StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
                while (h.length() < 2)
                    h.insert(0, "0");
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return s;
    }
}
