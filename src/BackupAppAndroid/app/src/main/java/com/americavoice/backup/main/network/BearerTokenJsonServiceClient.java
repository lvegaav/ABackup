package com.americavoice.backup.main.network;

import android.content.Context;

import com.americavoice.backup.main.model.TokenModel;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
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

    private Map<String, CacheEntry> mTokenCache = new HashMap<>();

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

        mCacheKey = baseUrl + oidUrl + clientId + clientSecret + clientScope;
    }

    private TokenModel getToken() {

        TokenModel tokenModel = null;

        RequestFuture<String> future = RequestFuture.newFuture();

        StringRequest request = new StringRequest(Request.Method.POST,
                mOidUrl,
                future,
                future) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("client_id", mClientId);
                params.put("client_secret", mClientSecret);
                params.put("grant_type", "client_credentials");
                params.put("scope", mClientScope);
                return params;
            }
        };



        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(request);

        try {
            String response = future.get();
            tokenModel = (TokenModel) fromJson(response, TokenModel.class);
        } catch (InterruptedException e) {
            Crashlytics.logException(e);
        } catch (ExecutionException e) {
            Crashlytics.logException(e);
        } catch (ClassCastException e) {
            Crashlytics.logException(e);
        }

        return tokenModel;
    }

    private String getAndCacheBearerToken() {
        TokenModel token = getToken();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, token.expires_in);
        Date expirationDate = calendar.getTime();
        mTokenCache.put(mCacheKey, new CacheEntry(token, expirationDate));
        return token.access_token;
    }

    String getBearerToken() {
        CacheEntry cacheEntry = null;
        if (mTokenCache.containsKey(mCacheKey)){
            cacheEntry = mTokenCache.get(mCacheKey);
            if (cacheEntry.hasExpired()) {
                mTokenCache.remove(mCacheKey);
                cacheEntry = null;
            }
        }

        return cacheEntry != null ? ((TokenModel) cacheEntry.value).access_token : getAndCacheBearerToken();
    }

    private class CacheEntry {
        Date expiresAt;
        Object value;

        CacheEntry(Object value, Date expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        boolean hasExpired() {
            return expiresAt != null && expiresAt.before(Calendar.getInstance().getTime());
        }
    }


}
