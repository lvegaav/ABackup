package com.americavoice.backup.payment.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.americavoice.backup.R;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.payment.presenter.ChoosePlanPresenter;

import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by javier on 10/24/17.
 */

public class ChoosePlanFragment extends BaseFragment implements ChoosePlanView<dtos.Product>, AdapterView.OnItemClickListener {

    public final static String HAS_PLAN = "has plan";


    public interface Listener {
        void choosePlanBack();
        void selectPlan(dtos.Product dummyPlan);
    }
    private Listener mListener;


    @Inject
    ChoosePlanPresenter mPlanPresenter;

    private Unbinder mUnbinder;

    @BindView(R.id.subscription_list)
    ListView mSubscriptionList;

    @BindView(R.id.no_current_plan)
    View mNoCurrentPlanView;

    private SubscriptionListAdapter mAdapter;

    private dtos.Product selectedItem;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() instanceof Listener) {
            mListener = (Listener) getActivity();
        }
        View view = inflater.inflate(R.layout.fragment_choose_plan, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        initializePresenter();
        initializeCurrentPlanAlert();
        initializeSubscriptionList();
        return view;
    }

    private void initializePresenter() {
        this.getComponent(AppComponent.class).inject(this);
        mPlanPresenter.setView(this);
    }

    private void initializeSubscriptionList() {
        List<dtos.Product> list = Collections.emptyList();
        mAdapter = new SubscriptionListAdapter(list);
        mSubscriptionList.setAdapter(mAdapter);
        mSubscriptionList.setOnItemClickListener(this);
    }


    private void initializeCurrentPlanAlert() {
        Bundle bundle = getArguments();
        boolean hasOptions = bundle.getBoolean(HAS_PLAN);
        if (hasOptions) {
            mNoCurrentPlanView.setVisibility(View.GONE);
        }
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.subscription_options_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_submit:
                if (mListener != null) {
                    mListener.selectPlan(selectedItem);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d("selection", "" + i);
        view.setSelected(true);
        setSelectedItem((dtos.Product) adapterView.getItemAtPosition(i));
    }


    @Override
    public void onResume() {
        super.onResume();
        mPlanPresenter.resume();
    }

    @Override
    public void onPause() {
        mPlanPresenter.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mPlanPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        if (mListener != null) {
            mListener.choosePlanBack();
        }
    }

    @Override
    public void showPlans(List<dtos.Product> list) {
        mAdapter.updateList(list);
    }

    protected void setSelectedItem(dtos.Product subscriptionDummy) {
        selectedItem = subscriptionDummy;
        if (selectedItem == null) {
            Log.d("selection", "not selected");
            setHasOptionsMenu(false);
        } else {
            setHasOptionsMenu(true);
        }
    }


}
