
package com.americavoice.backup.login.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.americavoice.backup.AndroidApplication;
import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AuthenticatorAsyncTask;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.login.model.SpinnerItem;
import com.americavoice.backup.login.presenter.LoginPresenter;
import com.americavoice.backup.login.presenter.LoginRegisterPresenter;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.data.SharedPrefsUtils_Factory;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.network.NetworkProvider;
import com.americavoice.backup.main.ui.BaseAuthenticatorFragment;
import com.americavoice.backup.main.ui.activity.LoginActivity;
import com.americavoice.backup.utils.ConnectivityUtils;
import com.americavoice.backup.utils.FirebaseUtils;
import com.crashlytics.android.Crashlytics;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Fragment that shows details of a certain political party.
 */
public class LoginRegisterFragment extends BaseAuthenticatorFragment implements LoginRegisterView {



    /**
     * Interface for listening submit button.
     */
    public interface Listener {
        void viewValidation(String username, String device);
        void onBackLoginRegisterClicked();
    }


    @Inject
    LoginRegisterPresenter mPresenter;
    private Listener mListener;
    private Unbinder mUnBind;
    @BindView(R.id.et_phone_number)
    public EditText etPhoneNumber;
    @BindView(R.id.sp_country)
    public Spinner spCountry;
    @BindView(R.id.et_username)
    public EditText etUsername;
    @BindView(R.id.et_password)
    public EditText etPassword;
    @BindView(R.id.et_confirm_password)
    public EditText etConfirmPassword;
    @BindView(R.id.terms_of_service)
    TextView mTermsOfService;


    public LoginRegisterFragment() {
        super();
    }

    public static LoginRegisterFragment newInstance() {
        return new LoginRegisterFragment();
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

        View fragmentView = inflater.inflate(R.layout.fragment_login_register, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);
        initializeTermsOfService();
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

    private void initializeTermsOfService() {
        mTermsOfService.setText(Html.fromHtml(getString(R.string.login_register_termsOfService)));
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
        if (this.mListener != null) this.mListener.onBackLoginRegisterClicked();
    }

    @Override
    public void viewValidation(String username, String device) {
        if (mListener != null) mListener.viewValidation(username, device);
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
    public void showPhoneNumberInvalidRange() {
        etPhoneNumber.requestFocus();
        etPhoneNumber.setError(getString(R.string.login_validationPhoneNumberInvalidRange));
    }

    @Override
    public void showCountryCodeRequired() {
        ((TextView) spCountry.getSelectedView()).setError(getString(R.string.error_country_code_required));
    }

    @Override
    public void showUsernameRequired() {
        etUsername.requestFocus();
        etUsername.setError(getString(R.string.login_validationUsernameRequired));
    }

    @Override
    public void showUsernameInvalid() {
        etUsername.requestFocus();
        etUsername.setError(getString(R.string.login_validationUsernameInvalid));
    }

    @Override
    public void showUsernameExists() {
        etUsername.requestFocus();
        etUsername.setError(getString(R.string.login_validationUsernameExists));
    }

    @Override
    public void showPhoneNumberExists() {
        etPhoneNumber.requestFocus();
        etPhoneNumber.setError(getString(R.string.login_validationPhoneNumberExists));
    }

    @Override
    public void showNewPasswordRequired() {
        etPassword.requestFocus();
        etPassword.setError(getString(R.string.login_validationNewPasswordInvalid));
    }

    @Override
    public void showConfirmPasswordRequired() {
        etConfirmPassword.requestFocus();
        etConfirmPassword.setError(getString(R.string.login_validationConfirmPasswordInvalid));
    }

    @Override
    public void showConfirmPasswordInvalid() {
        etConfirmPassword.requestFocus();
        etConfirmPassword.setError(getString(R.string.login_validationConfirmPasswordInvalid));
    }

    @Override
    public void showNewPasswordInvalid() {
        etPassword.requestFocus();
        etPassword.setError(getString(R.string.login_validationNewPasswordMatches));
    }

    @OnClick(R.id.btn_register)
    public void onRegister(View v) {
        String countryCode = "";
        try {
            countryCode = ((SpinnerItem) spCountry.getSelectedItem()).getId();
        } finally {
            mPresenter.submit(countryCode,
                    etPhoneNumber.getText().toString(),
                    etUsername.getText().toString(),
                    etPassword.getText().toString(),
                    etConfirmPassword.getText().toString());
        }
   }

    @OnClick(R.id.terms_of_service)
    public void onShowTerms() {
        SharedPrefsUtils prefsUtils = new SharedPrefsUtils(getContext());
        String language = Locale.getDefault().getLanguage();
        String termsOfServiceUrl = "";
        switch (language) {
            case "es":
                termsOfServiceUrl = prefsUtils.getStringPreference("esTermsOfService", "");
                break;
            default:
                termsOfServiceUrl = prefsUtils.getStringPreference("enTermsOfService", "");
                break;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(termsOfServiceUrl));
            startActivity(intent);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
   }
}

