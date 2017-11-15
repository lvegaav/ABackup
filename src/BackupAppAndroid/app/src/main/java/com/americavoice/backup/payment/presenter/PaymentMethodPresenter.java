package com.americavoice.backup.payment.presenter;

import android.util.Log;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.payment.ui.PaymentMethodView;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.crashlytics.android.Crashlytics;

import net.servicestack.client.AsyncResult;
import net.servicestack.client.WebServiceException;

import javax.inject.Inject;

/**
 * Created by javier on 10/25/17.
 */

@PerActivity
public class PaymentMethodPresenter extends BasePresenter implements IPresenter {


    private PaymentMethodView mView;

    @Inject
    PaymentMethodPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
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
        mView.showLoading();
        mNetworkProvider.getPaypalToken(new AsyncResult<dtos.GetPayPalTokenResponse>() {
            @Override
            public void success(dtos.GetPayPalTokenResponse response) {
                mView.setAuthorization(response.getToken());
            }

            @Override
            public void error(Exception ex) {
                Crashlytics.logException(ex);
                mView.hideLoading();
                mView.showPayPalError(ex);
            }

        });

    }

    public void onNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        // Send this nonce to your server
        String nonce = paymentMethodNonce.getNonce();
        mNetworkProvider.sendPayPalNonce(nonce, new AsyncResult<dtos.CreatePayPalPaymentMethodResponse>() {
            @Override
            public void success(dtos.CreatePayPalPaymentMethodResponse response) {
                mView.onPaymentMethodUpdated();
            }

            @Override
            public void error(Exception ex) {
                Crashlytics.logException(ex);
                mView.showPayPalError(ex);
            }

            @Override
            public void complete() {
                mView.hideLoading();
            }
        });
    }

    public void onCreditCardCreate(String firstName, String lastName, String phoneNumber,
                                   String address, String city, String stateRegion, String postalCode,
                                   String country, String cardNumber, String cardExpiry,
                                   String ccvCode) {

        mView.showLoading();
        dtos.CreateCreditCardPaymentMethod request = new dtos.CreateCreditCardPaymentMethod()
                .setFirstName(firstName.trim())
                .setLastName(lastName.trim())
                .setPhoneNumber(phoneNumber.trim())
                .setAddress(address.trim())
                .setCity(city.trim())
                .setStateRegion(stateRegion.trim())
                .setPostalCode(postalCode.trim())
                .setCountry(country.trim())
                .setCardNumber(cardNumber.trim())
                .setCardExpiry(cardExpiry.trim())
                .setCcvCode(ccvCode.trim());

        mNetworkProvider.createCreditCardPaymentMethod(request,
                new AsyncResult<dtos.CreateCreditCardPaymentMethodResponse>() {

                    @Override
                    public void success(dtos.CreateCreditCardPaymentMethodResponse response) {
                        mView.hideLoading();
                        mView.onPaymentMethodUpdated();
                        Log.d("Credit card", "Success creating credit card: " + response.getPaymentId());
                    }


                    @Override
                    public void error(Exception ex) {
                        Crashlytics.logException(ex);
                        mView.hideLoading();
                        if (ex instanceof WebServiceException) {
                            WebServiceException exception = (WebServiceException) ex;
                            Log.e("Credit card", "Status: " + exception.getStatusCode() +
                                    " " + exception.getErrorMessage());
                        }
                        Log.e("Credit card", "Error creating credit card", ex);
                        mView.showCreditCardError(ex);
                    }
                });
    }
}
