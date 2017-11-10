package com.americavoice.backup.payment.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.payment.data.PaymentMethod;
import com.americavoice.backup.payment.data.Subscription;

import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by javier on 10/25/17.
 */

public class SubscriptionFragment extends BaseFragment {

    public interface Listener {
        void onSubscriptionBack();
        void onChangePlan();
        void onDeletePaymentMethod();
        void onDeleteSubscription();
        void onUpdatePaymentMethod();
    }

    public final static String SUBSCRIPTION = "Subscription";
    public final static String PAYMENT_METHOD = "Payment method";

    private Unbinder mUnbinder;
    private Listener mListener;

    @BindView(R.id.subscription_amount)
    TextView mSubscriptionAmount;

    @BindView(R.id.subscription_detail)
    TextView mSubscriptionDetail;

    @BindView(R.id.subscription_start)
    TextView mSubscriptionStart;

    @BindView(R.id.subscription_next_payment)
    TextView mSubscriptionNextPayment;

    @BindView(R.id.credit_card_section)
    View mCreditCardSection;

    @BindView(R.id.credit_card_background)
    View mCreditCardBackground;

    @BindView(R.id.credit_card_number)
    TextView mCreditCardNumber;

    @BindView(R.id.credit_card_expiration)
    TextView mCreditCardExpiration;

    @BindView(R.id.ic_wallet)
    ImageView mWalletIcon;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscription, container, false);
        mUnbinder = ButterKnife.bind(this, view);
//        setHasOptionsMenu(true);
        initializeListener();
        initializeSubscription();
        initializePaymentMethod();
        return view;
    }

    private void initializeListener() {
        if (getActivity() instanceof Listener) {
            mListener = (Listener) getActivity();
        }
    }

    private void initializeSubscription() {
        Bundle arguments = getArguments();
        if (arguments == null) return;
        Subscription subscription = arguments.getParcelable(SUBSCRIPTION);
        if (subscription != null) {
            mSubscriptionAmount.setText(subscription.amount);
            mSubscriptionDetail.setText(subscription.description);
            mSubscriptionStart.setVisibility(View.VISIBLE);
            mSubscriptionStart.setText(getString(R.string.subscription_start_date, subscription.startDate));
            mSubscriptionNextPayment.setVisibility(View.VISIBLE);
            mSubscriptionNextPayment.setText(getString(R.string.subscription_next_payment_date, subscription.nextPaymentDate));
        }
    }

    private void initializePaymentMethod() {
        Bundle arguments = getArguments();
        if (arguments == null) return;
        PaymentMethod paymentMethod = arguments.getParcelable(PAYMENT_METHOD);
        if (paymentMethod != null) {
            if (paymentMethod.paymentMethodType == PaymentMethod.PaymentMethodType.PAY_PAL) {
                mWalletIcon.setVisibility(View.VISIBLE);
                mCreditCardSection.setVisibility(View.GONE);
            } else {
                mCreditCardNumber.setText(paymentMethod.creditCardNumber);
                mCreditCardExpiration.setText(paymentMethod.expirationDate);
            }
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.current_subscription_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel_subscription:
                mListener.onDeleteSubscription();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        if (mListener != null) {
            mListener.onSubscriptionBack();
        }
    }

    @OnClick(R.id.change_subscription_button)
    public void onChangePlan() {
        mListener.onChangePlan();
    }

    @OnClick(R.id.update_payment_method_button)
    public void onChangePaymentMethod() {
        mListener.onUpdatePaymentMethod();
    }
}
