package com.americavoice.backup.payment.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.americavoice.backup.R;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.di.components.DaggerAppComponent;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.ui.activity.BaseActivity;
import com.americavoice.backup.payment.data.PaymentMethod;
import com.americavoice.backup.payment.data.Subscription;
import com.americavoice.backup.payment.presenter.PaymentPresenter;
import com.americavoice.backup.payment.utils.ProductUtils;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by javier on 10/23/17.
 */

public class PaymentActivity extends BaseActivity implements PaymentView,
        ChoosePlanFragment.Listener, PaymentMethodFragment.Listener, SubscriptionFragment.Listener,
        HasComponent<AppComponent> {


    PaymentPresenter mPaymentPresenter;
    private AppComponent mAppComponent;

    private Unbinder mUnbind;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.content)
    FrameLayout mContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        mUnbind = ButterKnife.bind(this);
        this.mAppComponent = DaggerAppComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build();
        mToolbar.setTitle(getString(R.string.payment_title));
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mPaymentPresenter = new PaymentPresenter(new SharedPrefsUtils(this), new NetworkProvider(this));
        mPaymentPresenter.setView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPaymentPresenter.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaymentPresenter.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPaymentPresenter.destroy();
        mUnbind.unbind();
    }

    @Override
    public void showPlanChoose(boolean hasPlan) {
        Fragment fragment = new ChoosePlanFragment();
        Bundle args = new Bundle();
        args.putBoolean(ChoosePlanFragment.HAS_PLAN, hasPlan);
        fragment.setArguments(args);
        replaceFragment(R.id.content, fragment, true, false);
    }

    @Override
    public void showPaymentChoose(dtos.Product selectedSubscription) {

        Bundle bundle = new Bundle();
        bundle.putString(PaymentMethodFragment.SELECTED_SUBSCRIPTION_AMOUNT,
                ProductUtils.amountFromProduct(selectedSubscription));
        bundle.putString(PaymentMethodFragment.SELECTED_SUBSCRIPTION_DETAIL,
                ProductUtils.detailsFromProduct(selectedSubscription));
        Fragment fragment = new PaymentMethodFragment();
        fragment.setArguments(bundle);
        replaceFragment(R.id.content, fragment, true, false);
    }

    @Override
    public void showSubscriptionDetails(Subscription subscription, PaymentMethod paymentMethod) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SubscriptionFragment.SUBSCRIPTION, subscription);
        bundle.putParcelable(SubscriptionFragment.PAYMENT_METHOD, paymentMethod);
        Fragment fragment = new SubscriptionFragment();
        fragment.setArguments(bundle);
        replaceFragment(R.id.content, fragment, true, false);
    }

    @Override
    public void choosePlanBack() {
        mPaymentPresenter.onChoosePlanBackButton();
    }

    @Override
    public void selectPlan(dtos.Product plan) {
        mPaymentPresenter.onProductChoose(plan);
//        finish();
    }

    @Override
    public AppComponent getComponent() {
        return mAppComponent;
    }

    @Override
    public void paymentMethodUpdated() {
        mPaymentPresenter.onPaymentChosen();
    }

    @Override
    public void paymentMethodBackButton() {
        mPaymentPresenter.onPaymentMethodBackButton();
    }

    @Override
    public void changeSubscriptionOption() {
        mPaymentPresenter.showPlanChoose();
    }


    @Override
    public void onSubscriptionBack() {
        finish();
    }

    @Override
    public void onChangePlan() {
        mPaymentPresenter.showPlanChoose();
    }

    @Override
    public void onDeletePaymentMethod() {
        //TODO:
    }

    @Override
    public void onDeleteSubscription() {
        mPaymentPresenter.onDeleteSubscription();
    }

    @Override
    public void onUpdatePaymentMethod() {
        mPaymentPresenter.onPaymentChoose();
    }

    @Override
    public void onPayPalError() {

       showError(getString(R.string.payment_error_createPaypal), true);
    }

    @Override
    public void onCreditCardError() {
        onCreditCardError(getString(R.string.payment_error_createCreditCard));
    }

    @Override
    public void onCreditCardError(String message) {
        showError(message, false);
    }

    @Override
    public void showError(String message, final boolean finish) {
        new AlertDialog.Builder(this, R.style.WhiteDialog)
                .setTitle("Error")
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (finish) finish();
                    }
                })
                .setMessage(message)
                .create().show();
    }

    @Override
    public void showLoading() {
        showDialog(getString(R.string.common_loading));
    }
    @Override
    public void hideLoading() {
        hideDialog();
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public Context getContext() {
        return getContext();
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().post(new OnBackPress());
    }
}
