package com.americavoice.backup.payment.presenter;

import android.util.Log;

import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.payment.data.PaymentMethod;
import com.americavoice.backup.payment.data.SubscriptionDummy;
import com.americavoice.backup.payment.ui.PaymentView;

import net.servicestack.client.AsyncResult;
import net.servicestack.client.WebServiceException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by javier on 10/24/17.
 */

@Singleton
public class PaymentPresenter extends BasePresenter implements IPresenter{


    private PaymentView mPaymentView;

    @Inject
    public PaymentPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(PaymentView view) {
        mPaymentView = view;
        initialize();
    }

    public void initialize() {
        mNetworkProvider.getPaymentMethod(new AsyncResult<dtos.GetPaymentMethodResponse>() {
            @Override
            public void success(dtos.GetPaymentMethodResponse response) {
                //TODO:
                // Existing payment method. Check subscription
                Log.d("Payment", response.getPaymentId());
                checkSubscriptionAndShow();
//                mPaymentView.showSubscriptionDetails(
//                        new SubscriptionDummy("$10", "15 GB / month", "2017-01-01", "2018-01-01"),
//                        new PaymentMethod("credit card", "1111", "01/10"));
            }

            @Override
            public void error(Exception ex) {
                if (ex instanceof WebServiceException) {
                    WebServiceException webServiceException = (WebServiceException) ex;
                    if (webServiceException.getStatusCode() == 404) {
                        // no payment method. Show subscription list
                        mPaymentView.showPlanChoose();
                        return;
                    }
                    Log.e("Payment", webServiceException.getErrorCode() + ":" + webServiceException.getErrorMessage());
                }
                Log.e("Payment", "Error getting payment method");
                ex.printStackTrace();
            }
        });
    }

    private void checkSubscriptionAndShow() {

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
}
