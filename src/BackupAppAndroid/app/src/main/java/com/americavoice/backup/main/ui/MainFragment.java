
package com.americavoice.backup.main.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.americavoice.backup.R;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.presenter.MainPresenter;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Fragment that shows details of a certain political party.
 */
public class MainFragment extends BaseFragment implements MainView {

    public interface Listener {
        void viewPhotos();
        void viewVideos();
        void viewContacts();
        void viewDocuments();
        void viewSettings();
        void onMainBackPressed();
    }

    @Inject
    MainPresenter mPresenter;
    private Listener mListener;
    private Unbinder mUnBind;

    private RecyclerView.LayoutManager mLayoutManager;

    public MainFragment() {
        super();
    }

    public static MainFragment newInstance() {
        return new MainFragment();
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

        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showKeyboard(false);
        this.initialize();
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

    private void initialize() {
        this.getComponent(AppComponent.class).inject(this);
        this.mPresenter.setView(this);
        this.mPresenter.initialize(getString(R.string.main_title));


    }

    @Override
    public void showLoading() {
        if (mProgress != null) {
            mProgress.hide();
            mProgress.dismiss();
            mProgress = null;
        }
        mProgress = ProgressDialog.show(getActivity(),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.common_loading),
                true,
                false);
    }

    @Override
    public void hideLoading() {
        if (mProgress != null) {
            mProgress.hide();
            mProgress.dismiss();
            mProgress = null;
        }
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @OnClick(R.id.ll_contacts)
    public void onContacts(View view) {
        if (mListener != null) mListener.viewContacts();
    }

    @OnClick(R.id.ll_documents)
    public void onDocuments(View view)
    {
        if (mListener != null) mListener.viewDocuments();
    }

    @OnClick(R.id.ll_photos)
    public void onPhotos(View view)
    {
        if (mListener != null) mListener.viewPhotos();
    }

    @OnClick(R.id.ll_videos)
    public void onVideos(View view)
    {
        if (mListener != null) mListener.viewVideos();
    }

    @OnClick(R.id.btn_settings)
    public void onSettings(View view)
    {
        if (mListener != null) mListener.viewSettings();
    }

    @Override
    public void showError(String message) {
        this.showToastMessage(message);
    }

    @Override
    public Context getContext() {
        return getActivity().getApplicationContext();
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        if (mListener != null) mListener.onMainBackPressed();
    }


    @Override
    public void render() {

    }
}

