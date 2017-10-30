package com.americavoice.backup.payment.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.americavoice.backup.R;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.payment.data.PaymentMethodDummy;
import com.americavoice.backup.payment.data.SubscriptionDummy;
import com.americavoice.backup.payment.presenter.PaymentMethodPresenter;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.PaymentMethodNonce;

import net.servicestack.client.Utils;
import net.servicestack.client.WebServiceException;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by javier on 10/24/17.
 */

public class PaymentMethodFragment extends BaseFragment implements TabLayout.OnTabSelectedListener,
        PaymentMethodView, PaymentMethodNonceCreatedListener {

    public final static String SELECTED_SUBSCRIPTION = "selected subscription";
    public final static int REQUEST_CODE = 0;


    public interface Listener {
        void setPaymentMethod();
        void paymentMethodBackButton();
        void changeSubscriptionOption();
        void onPayPalError();
    }

    private Listener mListener;
    private Unbinder mUnbinder;

    @Inject
    PaymentMethodPresenter mPresenter;

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

    // credit card fields
    @BindView(R.id.first_name)
    EditText mFirstName;
    @BindView(R.id.last_name)
    EditText mLastName;
    @BindView(R.id.phone_number)
    EditText mPhoneNumber;
    @BindView(R.id.address)
    EditText mAddress;
    @BindView(R.id.city)
    EditText mCity;
    @BindView(R.id.state_region)
    EditText mStateRegion;
    @BindView(R.id.postal_code)
    EditText mPostalCode;
    @BindView(R.id.country)
    EditText mCountry;
    @BindView(R.id.credit_card_number)
    EditText mCardNumber;
    @BindView(R.id.expiration_month)
    EditText mExpirationMonth;
    @BindView(R.id.expiration_year)
    EditText mExpirationYear;
    @BindView(R.id.ccv_code)
    EditText mCcvCode;



    BraintreeFragment mBrainTreeFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() instanceof Listener) {
            mListener = (Listener) getActivity();
        }
        initializePresenter();
        View view = inflater.inflate(R.layout.fragment_payment_method, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        initializeSelectedSubscription();
        initializeTabListener();

        return view;
    }

    private void initializePresenter() {
        this.getComponent(AppComponent.class).inject(this);
        mPresenter.setView(this);
    }

    private void initializeSelectedSubscription() {
        mExpirationMonth.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (mExpirationMonth.getText().toString().length() == 2) {
                    mExpirationYear.requestFocus();
                }
                return false;
            }
        });
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
                mPayPalSection.setVisibility(View.VISIBLE);
                mCreditCardSection.setVisibility(View.GONE);
                mPresenter.requestAuthorization();
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void setAuthorization(String authorization) {
        try {
            mBrainTreeFragment = BraintreeFragment.newInstance(getActivity(), authorization);
            mBrainTreeFragment.addListener(this);
            PayPal.authorizeAccount(mBrainTreeFragment);
        } catch (InvalidArgumentException e) {
            Toast.makeText(getContext(), "There was an error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public BraintreeFragment getBraintreeFragment() {
        return mBrainTreeFragment;
    }

    @Override
    public void showPayPalError(Exception e) {
        if (e instanceof WebServiceException) {
            Log.e("Paypal", "Status: " + ((WebServiceException) e).getStatusCode() + "," + ((WebServiceException) e).getErrorMessage());
        }
        Log.e("Paypal", "error", e);
        mListener.onPayPalError();
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        mPresenter.onNonceCreated(paymentMethodNonce);
    }

    @OnClick(R.id.credit_card_save_button)
    public void onCreditCardButtonClick() {
        String
                firstName = mFirstName.getText().toString(),
                lastName = mLastName.getText().toString(),
                phoneNumber = mPhoneNumber.getText().toString(),
                address = mAddress.getText().toString(),
                city = mCity.getText().toString(),
                stateRegion = mStateRegion.getText().toString(),
                postalCode = mPostalCode.getText().toString(),
                country = mCountry.getText().toString(),
                ccvCode = mCcvCode.getText().toString(),
                cardNumber = mCardNumber.getText().toString(),
                expirationMonth = mExpirationMonth.getText().toString(),
                expirationYear = mExpirationYear.getText().toString();
        if (Utils.isEmpty(firstName) || Utils.isEmpty(lastName) || Utils.isEmpty(phoneNumber) ||
                Utils.isEmpty(address) || Utils.isEmpty(city) || Utils.isEmpty(stateRegion) ||
                Utils.isEmpty(postalCode) || Utils.isEmpty(country) || Utils.isEmpty(ccvCode) ||
                Utils.isEmpty(cardNumber) || Utils.isEmpty(expirationMonth) ||
                Utils.isEmpty(expirationYear)) {
            new AlertDialog.Builder(getActivity(), R.style.WhiteDialog)
                    .setTitle("Missing fields")
                    .setMessage("Please fill the missing fields")
                    .setPositiveButton("Ok", null)
                    .show();
        } else {
            String cardExpiry = "" + expirationMonth + expirationYear;
            mPresenter.onCreditCardCreate(firstName, lastName, phoneNumber, address, city,
                    stateRegion, postalCode, country, cardNumber, cardExpiry, ccvCode);
        }

    }

    @Override
    public void onPaymentMethodUpdated() {
        mListener.setPaymentMethod();
    }
}
