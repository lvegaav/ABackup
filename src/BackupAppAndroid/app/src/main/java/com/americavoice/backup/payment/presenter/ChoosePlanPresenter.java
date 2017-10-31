package com.americavoice.backup.payment.presenter;

import com.americavoice.backup.di.PerActivity;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.payment.ui.ChoosePlanView;

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
    public ChoosePlanPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(ChoosePlanView<dtos.Product> view) {
        this.mView = view;
        mNetworkProvider.getProducts(new AsyncResult<dtos.GetProductsResponse>() {
            @Override
            public void success(dtos.GetProductsResponse response) {
                List<dtos.Product> productList = response.getProducts();
                mView.showPlans(productList);
            }

            @Override
            public void error(Exception ex) {
                super.error(ex);
            }
        });
    }

    @Override
    public void resume() {
//        mView.showPlans(Arrays.asList(
//                new Subscription("$10", "5GB / 3 months", "2017-01-01", "2018-01-01"),
//                new Subscription("$15", "10GB / 3 months", "2017-01-01", "2018-01-01")
//        ));
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }
}
