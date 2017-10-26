package com.americavoice.backup.payment.presenter;

import android.util.Log;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.payment.ui.PaymentMethodView;

import net.servicestack.client.AsyncResult;

import javax.inject.Inject;

/**
 * Created by javier on 10/25/17.
 */

@PerActivity
public class PaymentMethodPresenter extends BasePresenter implements IPresenter {


    private PaymentMethodView mView;

    @Inject
    public PaymentMethodPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(PaymentMethodView view) {
        mView = view;
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

    public void requestAuthorization() {
        mNetworkProvider.getPaypalToken(new AsyncResult<dtos.GetPayPalTokenResponse>() {
            @Override
            public void success(dtos.GetPayPalTokenResponse response) {
                mView.setAuthorization(response.getToken());
            }

            @Override
            public void error(Exception ex) {
                ex.printStackTrace();
                Log.e("Paypal", ex.toString());
            }
        });
    }
}
