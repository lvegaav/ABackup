package com.americavoice.backup.payment.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.americavoice.backup.R;
import com.americavoice.backup.di.HasComponent;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.di.components.DaggerAppComponent;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.ui.activity.BaseActivity;
import com.americavoice.backup.payment.data.PaymentMethodDummy;
import com.americavoice.backup.payment.data.SubscriptionDummy;
import com.americavoice.backup.payment.presenter.PaymentPresenter;

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
    public void showPlanChoose() {
        replaceFragment(R.id.content, new ChoosePlanFragment(), true, false);
    }

    @Override
    public void showPaymentChoose(SubscriptionDummy selectedSubscription) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(PaymentMethodFragment.SELECTED_SUBSCRIPTION, selectedSubscription);
        Fragment fragment = new PaymentMethodFragment();
        fragment.setArguments(bundle);
        replaceFragment(R.id.content, fragment, true, false);
    }

    @Override
    public void showSubscriptionDetails(SubscriptionDummy subscription, PaymentMethodDummy paymentMethod) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SubscriptionFragment.SUBSCRIPTION, subscription);
        bundle.putParcelable(SubscriptionFragment.PAYMENT_METHOD, paymentMethod);
        Fragment fragment = new SubscriptionFragment();
        fragment.setArguments(bundle);
        replaceFragment(R.id.content, fragment, true, false);
    }

    @Override
    public void choosePlanBack() {
        finish();
    }

    @Override
    public void selectPlan(SubscriptionDummy dummyPlan) {
        showPaymentChoose(dummyPlan);
//        finish();
    }

    @Override
    public AppComponent getComponent() {
        return mAppComponent;
    }

    @Override
    public void setPaymentMethod(PaymentMethodDummy paymentMethod) {
        //TODO:
        finish();
    }

    @Override
    public void paymentMethodBackButton() {
        finish();
    }

    @Override
    public void changeSubscriptionOption() {
        showPlanChoose();
    }


    @Override
    public void onSubscriptionBack() {
        finish();
    }

    @Override
    public void onChangePlan() {
        showPlanChoose();
    }

    @Override
    public void onDeletePaymentMethod() {
        //TODO:
    }

    @Override
    public void onUpdatePaymentMethod() {
        showPaymentChoose(SubscriptionDummy.dummy());
    }
}
