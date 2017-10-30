package com.americavoice.backup.payment.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.payment.data.SubscriptionDummy;

import java.util.List;

/**
 * Created by javier on 10/24/17.
 */

public class SubscriptionListAdapter extends BaseAdapter {

    private List<SubscriptionDummy> list;

    public SubscriptionListAdapter(List<SubscriptionDummy> list) {
        this.list = list;
    }

    public void updateList(List<SubscriptionDummy> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.subscription_list_item, viewGroup, false);
            view.setTag(R.id.subscription_amount, view.findViewById(R.id.subscription_amount));
            view.setTag(R.id.subscription_detail, view.findViewById(R.id.subscription_detail));
        }
        SubscriptionDummy item = (SubscriptionDummy) getItem(i);
        TextView amount = (TextView) view.getTag(R.id.subscription_amount);
        TextView detail = (TextView) view.getTag(R.id.subscription_detail);
        amount.setText(item.amount);
        detail.setText(item.description);
        return view;
    }
}
