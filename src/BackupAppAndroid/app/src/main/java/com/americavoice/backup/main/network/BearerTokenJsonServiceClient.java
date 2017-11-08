package com.americavoice.backup.main.network;

import android.content.Context;

import com.americavoice.backup.main.model.TokenModel;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;

import net.servicestack.android.AndroidServiceClient;
import net.servicestack.client.JsonServiceClient;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by angelchanquin on 11/7/2017.
 */

public class BearerTokenJsonServiceClient extends AndroidServiceClient {

    private Map<String, CacheEntry> TokenCache = new HashMap<>();

    private Context mContext;
    private String mOidUrl;
    private String mClientId;
    private String mClientSecret;
    private String mClientScope;
    private String mCacheKey;

    BearerTokenJsonServiceClient(String baseUrl, Context context, String oidUrl, String clientId, String clientSecret, String clientScope) {
        super(baseUrl);
        this.mContext = context;
        this.mOidUrl = oidUrl;
        this.mClientId = clientId;
        this.mClientSecret = clientSecret;
        this.mClientScope = clientScope;
    }

    public TokenModel getToken() {

        TokenModel tokenModel = null;
        Map<String, String> params = new HashMap<>();
        params.put("client_id", mClientId);
        params.put("client_secret", mClientSecret);
        params.put("grant_type", "client_credentials");
        params.put("scope", mClientScope);

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, mOidUrl,  new JSONObject(params), future, future);

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(request);

        try {
            JSONObject response = future.get();
            tokenModel = (TokenModel) fromJson(response.toString(), TokenModel.class);
        } catch (InterruptedException e) {
            Crashlytics.logException(e);
        } catch (ExecutionException e) {
            Crashlytics.logException(e);
        } catch (ClassCastException e) {
            Crashlytics.logException(e);
        }

        return tokenModel;
    }

//    @DataContract
//    private class Token {
//
//        @DataMember(Order = 1, Name = "access_token")
//        String accessToken;
//
//        @DataMember(Order = 2, Name = "expires_in")
//        int expiresIn;
//
//        @DataMember(Order = 3, Name = "token_type")
//        String tokenType;
//
//    }
    private class CacheEntry {
        Date expiresAt;
        boolean hasExpired = expiresAt != null && expiresAt.before(Calendar.getInstance().getTime());
        String value;
    }

}
