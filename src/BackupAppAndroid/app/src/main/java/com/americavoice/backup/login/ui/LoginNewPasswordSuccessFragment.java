
package com.americavoice.backup.login.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import com.americavoice.backup.R;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.login.presenter.LoginNewPasswordPresenter;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseAuthenticatorFragment;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Fragment that shows details of a certain political party.
 */
public class LoginNewPasswordSuccessFragment extends BaseAuthenticatorFragment  {
    /**
     * Interface for listening submit button.
     */
    public interface Listener {
        void viewLogin();
    }


    private Listener mListener;
    private Unbinder mUnBind;

    public LoginNewPasswordSuccessFragment() {
        super();
    }

    public static LoginNewPasswordSuccessFragment newInstance() {
        return new LoginNewPasswordSuccessFragment();
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

        View fragmentView = inflater.inflate(R.layout.fragment_login_new_password_success, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.initialize(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBind.unbind();
    }



    private void initialize(Bundle savedInstanceState) {
        this.getComponent(AppComponent.class).inject(this);
        super.initialize();

        if (savedInstanceState != null) {
            //TODO:Init Values
        }

    }

    @Override
    public Context getContext() {
        return getActivity().getApplicationContext();
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
    }

   @OnClick(R.id.btn_success)
    public void onRegister(View v)
   {
       if (this.mListener != null) this.mListener.viewLogin();
   }
}

