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
                new SubscriptionDummy("$10", "5GB / 3 months", "2017-01-01", "2018-01-01"),
                new SubscriptionDummy("$15", "10GB / 3 months", "2017-01-01", "2018-01-01")
        ));
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }
}
