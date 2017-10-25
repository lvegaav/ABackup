package com.americavoice.backup.payment.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.payment.data.PaymentMethodDummy;
import com.americavoice.backup.payment.data.SubscriptionDummy;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by javier on 10/24/17.
 */

public class PaymentMethodFragment extends BaseFragment {

    public final static String SELECTED_SUBSCRIPTION = "selected subscription";


    public interface Listener {
        void setPaymentMethod(PaymentMethodDummy paymentMethod);
    }

    private Listener mListener;

    private Unbinder mUnbinder;

    @BindView(R.id.selected_subscription)
    View mSelectedSubscription;

    @BindView(R.id.subscription_amount)
    TextView mSubscriptionAmount;

    @BindView(R.id.subscription_detail)
    TextView mSubscriptionDetail;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() instanceof Listener) {
            mListener = (Listener) getActivity();
        }
        View view = inflater.inflate(R.layout.fragment_payment_method, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mSelectedSubscription.setSelected(true);
        initializeSelectedSubscription();
        return view;
    }

    private void initializeSelectedSubscription() {
        Bundle arguments = getArguments();
        SubscriptionDummy subscription = arguments.getParcelable(SELECTED_SUBSCRIPTION);
        mSubscriptionAmount.setText(subscription.amount);
        mSubscriptionDetail.setText(subscription.description);
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }

}
