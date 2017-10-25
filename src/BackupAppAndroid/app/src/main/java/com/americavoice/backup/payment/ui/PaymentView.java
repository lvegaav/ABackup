package com.americavoice.backup.payment.ui;

import com.americavoice.backup.payment.data.SubscriptionDummy;

/**
 * Created by javier on 10/24/17.
 */

public interface PaymentView {

    void showPlanChoose();
    void showPaymentChoose(SubscriptionDummy selectedSubscription);
    void showPaymentDetails();

}
