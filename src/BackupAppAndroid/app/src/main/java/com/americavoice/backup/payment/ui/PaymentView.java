package com.americavoice.backup.payment.ui;

import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.payment.data.PaymentMethod;
import com.americavoice.backup.payment.data.Subscription;

/**
 * Created by javier on 10/24/17.
 */

public interface PaymentView {

    void showPlanChoose();
    void showPaymentChoose(dtos.Product selectedSubscription);
    void showSubscriptionDetails(Subscription subscription, PaymentMethod paymentMethod);
    void showError(String message, boolean finish);

}
