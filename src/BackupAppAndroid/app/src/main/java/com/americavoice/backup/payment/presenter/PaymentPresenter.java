package com.americavoice.backup.payment.presenter;

import android.util.Log;

import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.presenter.BasePresenter;
import com.americavoice.backup.main.presenter.IPresenter;
import com.americavoice.backup.payment.data.PaymentMethod;
import com.americavoice.backup.payment.data.Subscription;
import com.americavoice.backup.payment.ui.PaymentView;

import net.servicestack.client.AsyncResult;
import net.servicestack.client.WebServiceException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by javier on 10/24/17.
 */

@Singleton
public class PaymentPresenter extends BasePresenter implements IPresenter{


    private PaymentView mPaymentView;
    private PaymentMethod mPaymentMethod;
    private dtos.Product selectedProduct;
    private Subscription subscription;

    @Inject
    public PaymentPresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        super(sharedPrefsUtils, networkProvider);
    }

    public void setView(PaymentView view) {
        mPaymentView = view;
        checkPaymentMethodAndShow();
    }

    public void checkPaymentMethodAndShow() {
        mPaymentView.showLoading();
        mNetworkProvider.getPaymentMethod(new AsyncResult<dtos.GetPaymentMethodResponse>() {
            @Override
            public void success(dtos.GetPaymentMethodResponse response) {
                //TODO:
                // Existing payment method. Check subscription
                Log.d("Payment", response.getPaymentId());
                mPaymentMethod = new PaymentMethod(response);
                checkSubscriptionAndShow();
            }

            @Override
            public void error(Exception ex) {
                mPaymentView.hideLoading();
                if (ex instanceof WebServiceException) {
                    WebServiceException webServiceException = (WebServiceException) ex;
                    if (webServiceException.getStatusCode() == 404) {
                        // no payment method. Show subscription list
                        mPaymentView.showPlanChoose(subscription != null);
                        return;
                    }
                    Log.e("Payment", webServiceException.getErrorCode() + ":" + webServiceException.getErrorMessage());
                }
                Log.e("Payment", "Error getting payment method");
                ex.printStackTrace();
                mPaymentView.showError("Could not load data, please try again later", true);
            }

        });
    }

    public void checkSubscriptionAndShow() {
        mPaymentView.showLoading();
        mNetworkProvider.getCurrentSubscription(new AsyncResult<dtos.GetSubscriptionResponse>() {
            @Override
            public void success(dtos.GetSubscriptionResponse response) {
                subscription = new Subscription(response);
                selectedProduct = response.product;
                showCurrentSubscription();
            }

            @Override
            public void error(Exception ex) {
                if (ex instanceof WebServiceException) {
                    WebServiceException webServiceException = (WebServiceException) ex;
                    Log.e("Payment", webServiceException.getErrorCode() + ":" + webServiceException.getErrorMessage());
                }
                showPlanChoose();
            }

            @Override
            public void complete() {
                mPaymentView.hideLoading();
            }
        });
    }

    public void showPlanChoose() {
        mPaymentView.showPlanChoose(subscription != null);
    }

    public void showPaymentChoose() {
        if (selectedProduct != null) {
            mPaymentView.showPaymentChoose(selectedProduct);
        } else {
            throw new RuntimeException("Not selected product");
        }
    }

    public void onProductChoose(dtos.Product product) {
        this.selectedProduct = product;
        if (mPaymentMethod != null) {
            createSubscription();
        } else {
            showPaymentChoose();
        }
    }

    public void onPaymentChoose() {
        showPaymentChoose();
    }

    public void onPaymentChosen() {
        if (subscription == null || !subscription.productId.equals(selectedProduct.getProductId())) {
            createSubscription();
        } else {
            // same product just show subscription
            checkPaymentMethodAndShow();
        }
    }

    public void showCurrentSubscription() {
        mPaymentView.showSubscriptionDetails(subscription, mPaymentMethod);

    }

    protected void createSubscription() {
        if (subscription == null) {
            // it's a create
            mPaymentView.showLoading();
            dtos.CreateSubscription request = new dtos.CreateSubscription()
                    .setProductId(selectedProduct.productId);
            mNetworkProvider.createSubscription(request, new AsyncResult<dtos.CreateSubscriptionResponse>() {
                @Override
                public void success(dtos.CreateSubscriptionResponse response) {
                    checkSubscriptionAndShow();
                }

                @Override
                public void error(Exception ex) {

                    if (ex instanceof WebServiceException) {
                        WebServiceException webServiceException = (WebServiceException) ex;
                        Log.e("Payment", webServiceException.getStatusCode() + ":" +
                                webServiceException.getErrorMessage(), ex);
                        if (webServiceException.getStatusCode() == 409) {
                            // current subscription is the same. ignore
                            showCurrentSubscription();
                        } else {
                            mPaymentView.showError("Could not create a subscription, please try again later", true);
                        }

                    }
                }

                @Override
                public void complete() {
                    mPaymentView.hideLoading();
                }
            });
        } else if (!subscription.productId.equals(selectedProduct.productId)) {
            // it's an update
            mPaymentView.showLoading();
            dtos.ChangeSubscription request = new dtos.ChangeSubscription()
                    .setProductId(selectedProduct.productId);
            mNetworkProvider.changeSubscription(request, new AsyncResult<dtos.ChangeSubscriptionResponse>() {
                @Override
                public void success(dtos.ChangeSubscriptionResponse response) {
                    checkSubscriptionAndShow();
                }

                @Override
                public void error(Exception ex) {
                    if (ex instanceof WebServiceException) {
                        WebServiceException webServiceException = (WebServiceException) ex;
                        Log.e("Payment", webServiceException.getStatusCode() + ":" +
                                webServiceException.getErrorMessage(), ex);
                        if (webServiceException.getStatusCode() == 409) {
                            // current subscription is the same. ignore
                            showCurrentSubscription();
                        } else {
                            mPaymentView.showError("Could not create a subscription, please try again later", true);
                        }

                    }
                }

                @Override
                public void complete() {
                    mPaymentView.hideLoading();
                }
            });
        } else {
            // they're the same, showing current subscription
            checkSubscriptionAndShow();
        }
    }


    public void onChoosePlanBackButton() {
        if (subscription != null && mPaymentMethod != null) {
            showCurrentSubscription();
        } else {
            mPaymentView.close();
        }
    }

    public void onPaymentMethodBackButton() {
        if (subscription != null && mPaymentMethod != null) {
            showCurrentSubscription();
        } else {
            mPaymentView.close();
        }
    }

    public void onDeleteSubscription() {
        //TODO: dtos missing method
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
