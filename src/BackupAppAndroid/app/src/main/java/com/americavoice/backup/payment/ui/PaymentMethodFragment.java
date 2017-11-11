package com.americavoice.backup.payment.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.payment.presenter.PaymentMethodPresenter;
import com.americavoice.backup.payment.utils.CreditCardErrors;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.crashlytics.android.Crashlytics;

import net.servicestack.client.ResponseError;
import net.servicestack.client.Utils;
import net.servicestack.client.WebServiceException;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by javier on 10/24/17.
 */

public class PaymentMethodFragment extends BaseFragment implements TabLayout.OnTabSelectedListener,
        PaymentMethodView, PaymentMethodNonceCreatedListener, BraintreeErrorListener {

    public final static String SELECTED_SUBSCRIPTION_AMOUNT = "selected subscription amount";
    public final static String SELECTED_SUBSCRIPTION_DETAIL = "selected subscription detail";
    public final static int REQUEST_CODE = 0;

    @Override
    public void onError(Exception e) {
        Crashlytics.logException(e);
        showError(getString(R.string.paypal_error_token));
    }


    public interface Listener {
        void paymentMethodUpdated();
        void paymentMethodBackButton();
        void changeSubscriptionOption();
        void onPayPalError();
        void onCreditCardError(String message);
        void onCreditCardError();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_method, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        if (getActivity() instanceof Listener) {
            mListener = (Listener) getActivity();
        }
        initializePresenter();
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
        if (arguments != null && arguments.containsKey(SELECTED_SUBSCRIPTION_AMOUNT) && arguments.containsKey(SELECTED_SUBSCRIPTION_DETAIL)) {
            String amount = arguments.getString(SELECTED_SUBSCRIPTION_AMOUNT);
            String description = arguments.getString(SELECTED_SUBSCRIPTION_DETAIL);
            mSubscriptionAmount.setText(amount);
            mSubscriptionDetail.setText(description);
        }
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
            Crashlytics.logException(e);
            showError(getString(R.string.paypal_error_token));
        }
    }

    @Override
    public BraintreeFragment getBraintreeFragment() {
        return mBrainTreeFragment;
    }

    @Override
    public void showPayPalError(Exception e) {
        Crashlytics.logException(e);
        showError(getString(R.string.payment_error_createPaypal));
        mListener.onPayPalError();
    }

    @Override
    public void showCreditCardError(Exception e) {
        Crashlytics.logException(e);
        if (e instanceof WebServiceException) {
            WebServiceException wsException = (WebServiceException) e;
            if (!TextUtils.isEmpty(wsException.getErrorCode()) && wsException.getErrorCode().equals("PaymentProviderError")) {
                List<ResponseError> errorList = wsException.getFieldErrors();
                if (errorList != null && errorList.size() > 0) {
                    ResponseError responseError = errorList.get(0);
                    String message = CreditCardErrors.errorMessage(getContext(), responseError);
                    mListener.onCreditCardError(message);
                    return;
                }
            }
        }
        mListener.onCreditCardError();
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

    @OnClick({R.id.wallet_button, R.id.paypal_create_button})
    public void onPayPalRequestClick() {
        mPresenter.requestAuthorization();
    }


    @Override
    public void onPaymentMethodUpdated() {
        mListener.paymentMethodUpdated();
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
        new AlertDialog.Builder(getContext(), R.style.WhiteDialog)
                .setTitle(getString(R.string.app_name))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.common_ok), null)
                .setMessage(message)
                .create().show();
    }
}
