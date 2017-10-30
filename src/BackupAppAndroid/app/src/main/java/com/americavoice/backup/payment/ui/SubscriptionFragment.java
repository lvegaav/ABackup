package com.americavoice.backup.payment.ui;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.payment.data.SubscriptionDummy;

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

    @BindView(R.id.credit_card_background)
    View mCreditCardBackground;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscription, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        initializeListener();
        initializeSubscription();
        return view;
    }

    private void initializeListener() {
        if (getActivity() instanceof Listener) {
            mListener = (Listener) getActivity();
        }
    }

    private void initializeSubscription() {
        Bundle arguments = getArguments();
        SubscriptionDummy subscription = arguments.getParcelable(SUBSCRIPTION);
        mSubscriptionAmount.setText(subscription.amount);
        mSubscriptionDetail.setText(subscription.description);
        mSubscriptionStart.setVisibility(View.VISIBLE);
        mSubscriptionStart.setText(getString(R.string.subscription_start_date, subscription.startDate));
        mSubscriptionNextPayment.setVisibility(View.VISIBLE);
        mSubscriptionNextPayment.setText(getString(R.string.subscription_next_payment_date, subscription.nextPaymentDate));
    }

//    private void initializePaymentMethod() {
//        ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
//            @Override
//            public Shader resize(int i, int i1) {
//                float rel = mCreditCardBackground.getHeight() == 0 ? 0 :
//                        mCreditCardBackground.getWidth() / mCreditCardBackground.getHeight();
//                LinearGradient lg = new LinearGradient(0, 0, mCreditCardBackground.getWidth(),
//                        mCreditCardBackground.getHeight() * rel * 3, new int[] {
//                        ContextCompat.getColor(getContext(), R.color.bt_very_light_gray),
//                        ContextCompat.getColor(getContext(), R.color.bt_very_light_gray),
//                        ContextCompat.getColor(getContext(), R.color.white),
//                        ContextCompat.getColor(getContext(), R.color.white)},
//                        new float[] {0, 0.1f, 0.1f, 1},
//                        Shader.TileMode.REPEAT);
//
//                return lg;
//            }
//        };
//        PaintDrawable paintDrawable = new PaintDrawable();
//        paintDrawable.setShape(new RoundRectShape(new float[]{20,20,20,20,20,20,20,20}, null, null));
//        paintDrawable.setShaderFactory(sf);
//        mCreditCardBackground.setBackground(paintDrawable);
//    }

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
