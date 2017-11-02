
package com.americavoice.backup.main.presenter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.support.annotation.NonNull;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.model.TokenModel;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.ui.SplashScreenView;

import net.servicestack.client.AsyncResult;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;


/**
 * {@link IPresenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class SplashScreenPresenter extends BasePresenter implements IPresenter {
    private static final int SPLASH_DISPLAY_LENGTH = 3000;
    private SplashScreenView mView;

    @Inject
    SplashScreenPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(@NonNull SplashScreenView view) {
        this.mView = view;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
    }

    /**
     * Initializes the presenter
     */
    public void initialize() {
        //Set App Token
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                NetworkProvider.getIdentityUrl(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            TokenModel tokenModel = (TokenModel) mNetworkProvider.fromJson(response, TokenModel.class);
                            mNetworkProvider.setAppToken(tokenModel.access_token);
                            doLogin();
                        } catch (Exception e) {
                            Crashlytics.logException(e);
                            mView.showError(mView.getContext().getString(R.string.exception_message_generic));
                            mView.finish();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mView.showError(mView.getContext().getString(R.string.network_error_socket_timeout_exception));
                        mView.finish();
                    }
                }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("client_id", "my-test-client");
                params.put("client_secret", "my-test-client");
                params.put("grant_type", "client_credentials");
                params.put("scope", "backup-api");
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                SPLASH_DISPLAY_LENGTH,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(mView.getContext());
        queue.add(stringRequest);
    }

    private void doLogin() {
        Account account = AccountUtils.getCurrentOwnCloudAccount(mView.getContext());
        if (account != null) {
            AccountManager accountManager = AccountManager.get(mView.getContext());
            String password = accountManager.getPassword(account);
            String name = AccountUtils.getAccountUsername(account.name);
            mNetworkProvider.login(name, password, new AsyncResult<dtos.AuthenticateResponse>() {
                @Override
                public void success(dtos.AuthenticateResponse response) {
                    mView.viewHome();
                }

                @Override
                public void error(Exception ex) {
                    mNetworkProvider.logout();
                    mView.viewHome();
                }
            });
        } else{
            mView.viewHome();
        }
    }
}
