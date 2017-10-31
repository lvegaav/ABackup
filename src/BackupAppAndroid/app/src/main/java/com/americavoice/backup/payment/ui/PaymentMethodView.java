package com.americavoice.backup.payment.ui;

import com.braintreepayments.api.BraintreeFragment;

/**
 * Created by javier on 10/25/17.
 */

public interface PaymentMethodView {
    void setAuthorization(String authorization);
    BraintreeFragment getBraintreeFragment();
    void showPayPalError(Exception e);
    void showCreditCardError(Exception e);
    void onPaymentMethodUpdated();
}
