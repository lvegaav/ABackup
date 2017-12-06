package com.americavoice.backup.payment.ui;

import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.ui.ILoadDataView;
import com.americavoice.backup.payment.data.PaymentMethod;
import com.americavoice.backup.payment.data.Subscription;

/**
 * Created by javier on 10/24/17.
 */

public interface PaymentView extends ILoadDataView {

    void showPlanChoose(boolean hasPlan);
    void showPaymentChoose(dtos.Product selectedSubscription);
    void showSubscriptionDetails(Subscription subscription, PaymentMethod paymentMethod);
    void showError(String message, boolean finish);
    void close();

}
