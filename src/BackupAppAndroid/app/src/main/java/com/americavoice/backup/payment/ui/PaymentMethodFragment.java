package com.americavoice.backup.payment.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.payment.data.PaymentMethodDummy;
import com.americavoice.backup.payment.data.SubscriptionDummy;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;

import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by javier on 10/24/17.
 */

public class PaymentMethodFragment extends BaseFragment implements TabLayout.OnTabSelectedListener {

    public final static String SELECTED_SUBSCRIPTION = "selected subscription";
    public final static int REQUEST_CODE = 0;


    public interface Listener {
        void setPaymentMethod(PaymentMethodDummy paymentMethod);
        void paymentMethodBackButton();
        void changeSubscriptionOption();
    }

    private Listener mListener;

    private Unbinder mUnbinder;

    @BindView(R.id.selected_subscription)
    View mSelectedSubscription;

    @BindView(R.id.subscription_amount)
    TextView mSubscriptionAmount;

    @BindView(R.id.subscription_detail)
    TextView mSubscriptionDetail;

    @BindView(R.id.change_subscription_button)
    Button mChangeSubscriptionButton;

    @BindView(R.id.payment_type_selector)
    TabLayout mTabLayout;

    @BindView(R.id.credit_card_section)
    View mCreditCardSection;

    @BindView(R.id.paypal_section)
    View mPayPalSection;

    BraintreeFragment mBrainTreeFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() instanceof Listener) {
            mListener = (Listener) getActivity();
        }
        View view = inflater.inflate(R.layout.fragment_payment_method, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        initializeSelectedSubscription();
//        mBrainTreeFragment = BraintreeFragment.newInstance(getActivity(), )
//        initializeTabListener();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                // send to server result
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // canceled
            } else {
                // exception
            }
        }

    }

    private void initializeSelectedSubscription() {
        Bundle arguments = getArguments();
        SubscriptionDummy subscription = arguments.getParcelable(SELECTED_SUBSCRIPTION);
        mSubscriptionAmount.setText(subscription.amount);
        mSubscriptionDetail.setText(subscription.description);
    }

    private void initializeTabListener() {
        mTabLayout.addOnTabSelectedListener(this);
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        if (mListener != null) {
            mListener.paymentMethodBackButton();
        }
    }

    @OnClick(R.id.change_subscription_button)
    public void onChangeSubscription() {
        if (mListener != null) {
            mListener.changeSubscriptionOption();
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        switch (tab.getPosition()) {
            case 0:
                mPayPalSection.setVisibility(View.GONE);
                mCreditCardSection.setVisibility(View.VISIBLE);
                break;
            case 1:
                //TODO: show paypal fragment
                mPayPalSection.setVisibility(View.VISIBLE);
                mCreditCardSection.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


}
