package com.americavoice.backup.payment.presenter;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.payment.data.SubscriptionDummy;
import com.americavoice.backup.payment.ui.ChoosePlanView;

import java.util.Arrays;

import javax.inject.Inject;

/**
 * Created by javier on 10/24/17.
 */
@PerActivity
public class ChoosePlanPresenter extends BasePresenter implements IPresenter{

    private ChoosePlanView<SubscriptionDummy> mView;

    @Inject
    public ChoosePlanPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(ChoosePlanView<SubscriptionDummy> view) {
        this.mView = view;
    }

    @Override
    public void resume() {
        mView.showPlans(Arrays.asList(
                new SubscriptionDummy() {{
                    description = "5GB / 3 months";
                    amount = "$10";
                }},
                new SubscriptionDummy() {{
                    description = "10GB / 3 months";
                    amount = "$15";
                }}
        ));
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }
}
