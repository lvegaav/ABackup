
package com.americavoice.backup.login.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.login.model.SpinnerItem;
import com.americavoice.backup.login.presenter.LoginForgotPresenter;
import com.americavoice.backup.login.presenter.LoginRegisterPresenter;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseAuthenticatorFragment;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Fragment that shows details of a certain political party.
 */
public class LoginForgotFragment extends BaseAuthenticatorFragment implements LoginForgotView {

    /**
     * Interface for listening submit button.
     */
    public interface Listener {
        void viewLoginNewPasswordView(String countryCode, String phoneNumber);
        void onBackLoginForgotClicked();
    }


    @Inject
    LoginForgotPresenter mPresenter;
    private Listener mListener;
    private Unbinder mUnBind;
    @BindView(R.id.et_phone_number)
    public EditText etPhoneNumber;
    @BindView(R.id.sp_country)
    public Spinner spCountry;

    public LoginForgotFragment() {
        super();
    }

    public static LoginForgotFragment newInstance() {
        return new LoginForgotFragment();
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            this.mListener = (Listener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_login_forgot, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.initialize(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mPresenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mPresenter.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBind.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mPresenter.destroy();
    }


    private void initialize(Bundle savedInstanceState) {
        this.getComponent(AppComponent.class).inject(this);
        this.mPresenter.setView(this);
        this.mPresenter.initialize();

        super.initialize();

        if (savedInstanceState != null) {
            //TODO:Init Values
        }

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
        showDialog(getString(R.string.common_sending));
    }

    @Override
    public void hideRetry() {
        hideDialog();
    }

    @Override
    public void showError(String message) {
        this.showDialogMessage(message);
    }


    @Override
    public Context getContext() {
        return getActivity().getApplicationContext();
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        if (this.mListener != null) this.mListener.onBackLoginForgotClicked();
    }

    @Override
    public void viewLoginNewPassword(String countryCode, String phoneNumber) {
        if (mListener != null) mListener.viewLoginNewPasswordView(countryCode, phoneNumber);
    }

    @Override
    public void populateCountries(List<SpinnerItem> items) {
        if (spCountry == null) return;

        spCountry.setAdapter(
                new SpinnerItemAdapter(
                        getActivity(),
                        R.layout.spinner_item,
                        items));
    }

    @Override
    public void showPhoneNumberRequired() {
        etPhoneNumber.requestFocus();
        etPhoneNumber.setError(getString(R.string.login_validationPhoneNumberRequired));
    }

    @Override
    public void showPhoneNumberInvalid() {
        etPhoneNumber.requestFocus();
        etPhoneNumber.setError(getString(R.string.login_validationPhoneNumberInvalid));
    }

    @Override
    public void showCountryCodeRequired() {
        ((TextView) spCountry.getSelectedView()).setError(getString(R.string.error_country_code_required));
    }

    @OnClick(R.id.btn_forgot)
    public void onRegister(View v) {
        String countryCode = "";
        try {
            countryCode = ((SpinnerItem) spCountry.getSelectedItem()).getId();
        } finally {
            mPresenter.submit(countryCode, etPhoneNumber.getText().toString());
        }
   }
}

