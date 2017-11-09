package com.americavoice.backup.main.network;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.americavoice.backup.BuildConfig;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.utils.DisplayUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;

import net.servicestack.android.AndroidServiceClient;
import net.servicestack.client.AsyncResult;
import net.servicestack.client.ConnectionFilter;
import net.servicestack.client.JsonSerializers;
import net.servicestack.client.TimeSpan;

import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

@Singleton
public class NetworkProvider {

    public static final String KEY_FIRST_TIME = "com.americavoice.backup.KEY_FIRST_TIME";
    private final SharedPreferences mPref;
    private final AndroidServiceClient mClient; //Client with User Authentication
    private BearerTokenJsonServiceClient mAppClient; //Client with JWT Authentication
    private final Context mContext;
    private Gson mGson;
    private AccountManager mAccountMgr;
    private HashMap<String, String> mDeviceInfo;
    private OwnCloudClient mCloudClient;

    /*private static final String baseUrl = "http://core-be.development.americavoice.com:8458";
    private static final String baseUrlOwnCloud = "http://backapp-eng.development.americavoice.com";
    private static final String identityUrl = "http://172.22.122.40/connect/token";*/

    private static final String baseUrl = "https://backup.secureip.io";
    private static final String baseUrlOwnCloud = "https://cloud.secureip.io";
    private static final String identityUrl = "https://id.americavoice.com/connect/token";

    public static String getBaseUrlOwnCloud() {
        return baseUrlOwnCloud;
    }

    public static String getIdentityUrl() {
        return identityUrl;
    }

    @Inject
    public NetworkProvider(Context context) {
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
        mClient = new AndroidServiceClient(baseUrl + "/api");

        mContext = context;
        mAccountMgr = AccountManager.get(context);

        mDeviceInfo = new HashMap<>();
        mDeviceInfo.put("device:brand", Build.MANUFACTURER);
        mDeviceInfo.put("device:model", Build.MODEL);
        mDeviceInfo.put("device:os", "Android");
        mDeviceInfo.put("device:osVersion", Build.VERSION.RELEASE);
        mDeviceInfo.put("device:appVersion", BuildConfig.VERSION_NAME);

        //TODO Ignore self-signed certificate
        //IgnoreSelfSigned();
    }

    private BearerTokenJsonServiceClient getAppClient() {
        if (mAppClient == null) {
            mAppClient = new BearerTokenJsonServiceClient(
                    baseUrl + "/api",
                    mContext,
                    identityUrl,
                    "my-test-client",
                    "my-test-client",
                    "backup-api");
        }

        mAppClient.RequestFilter = new ConnectionFilter() {
            @Override
            public void exec(HttpURLConnection conn) {
                String token = mAppClient.getBearerToken();
                conn.setRequestProperty("Authorization",
                        "Bearer " + token);
            }
        };
        return mAppClient;
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

    private GsonBuilder getGsonBuilder() {
        return (new GsonBuilder()).registerTypeAdapter(Date.class, JsonSerializers.getDateSerializer()).registerTypeAdapter(Date.class, JsonSerializers.getDateDeserializer()).registerTypeAdapter(TimeSpan.class, JsonSerializers.getTimeSpanSerializer()).registerTypeAdapter(TimeSpan.class, JsonSerializers.getTimeSpanDeserializer()).registerTypeAdapter(UUID.class, JsonSerializers.getGuidSerializer()).registerTypeAdapter(UUID.class, JsonSerializers.getGuidDeserializer());
    }

    private Gson getGson() {
        if(this.mGson == null) {
            this.mGson = this.getGsonBuilder().create();
        }

        return this.mGson;
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
        if (mCloudClient == null) {
            Uri serverUri = Uri.parse(baseUrlOwnCloud);
            mCloudClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, mContext, true);
            mCloudClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(username, password));
        }
        return mCloudClient;
    }

    public void setAppToken(String appToken) {
        mPref.edit().putString("TOKEN", appToken).apply();
    }

    public String toJson(Object o) {
        return this.getGson().toJson(o);
    }

    public Object fromJson(String json, Class c) {
        return this.getGson().fromJson(json, c);
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
        getAppClient().postAsync(request, result);
    }

    public void SendPhoneVerificationCode(AsyncResult<dtos.SendPhoneVerificationCodeResponse> result) {
        mClient.postAsync(new dtos.SendPhoneVerificationCode(), result);
    }

    public void SendPasswordResetCode(dtos.SendPasswordResetCode request, AsyncResult<dtos.SendPasswordResetCodeResponse> result) {
        Log.d("Network", "calling reset pass");
        getAppClient().postAsync(request, result);
    }

    public void PerformResetPassword(dtos.PerformResetPassword request, AsyncResult<dtos.PerformResetPasswordResponse> result) {
        getAppClient().postAsync(request, result);
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

    public void getAppConfig(AsyncResult<dtos.GetMobileAppConfigResponse> response) {
        dtos.GetMobileAppConfig request = new dtos.GetMobileAppConfig();
        request.setTablet(DisplayUtils.isTablet(mContext));
        request.setType("android");
        mClient.getAsync(request, response);
    }

}
