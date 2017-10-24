package com.americavoice.backup.payment.presenter;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.payment.ui.PaymentView;

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
    }

    @Override
    public void resume() {
        mPaymentView.showPlanChoose();
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }
}
