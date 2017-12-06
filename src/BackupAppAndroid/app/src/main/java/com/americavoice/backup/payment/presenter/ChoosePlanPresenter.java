package com.americavoice.backup.payment.presenter;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.payment.ui.ChoosePlanView;
import com.crashlytics.android.Crashlytics;

import net.servicestack.client.AsyncResult;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by javier on 10/24/17.
 */
@PerActivity
public class ChoosePlanPresenter extends BasePresenter implements IPresenter{

    private ChoosePlanView<dtos.Product> mView;

    @Inject
    ChoosePlanPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(ChoosePlanView<dtos.Product> view) {
        this.mView = view;
        mView.showLoading();
        mNetworkProvider.getProducts(new AsyncResult<dtos.GetProductsResponse>() {
            @Override
            public void success(dtos.GetProductsResponse response) {
                List<dtos.Product> productList = response.getProducts();
                mView.showPlans(productList);
            }

            @Override
            public void error(Exception ex) {
                Crashlytics.logException(ex);
            }

            @Override
            public void complete() {
                mView.hideLoading();
            }
        });
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
