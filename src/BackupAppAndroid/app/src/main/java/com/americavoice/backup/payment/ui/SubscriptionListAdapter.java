package com.americavoice.backup.payment.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.main.network.dtos;

import java.text.NumberFormat;
import java.util.List;

/**
 * Created by javier on 10/24/17.
 */

public class SubscriptionListAdapter extends BaseAdapter {

    private List<dtos.Product> list;

    public SubscriptionListAdapter(List<dtos.Product> list) {
        this.list = list;
    }

    public void updateList(List<dtos.Product> list) {
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
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        NumberFormat oneDecimal = NumberFormat.getInstance();
        oneDecimal.setMinimumFractionDigits(0);
        oneDecimal.setMaximumFractionDigits(2);
        dtos.Product item = (dtos.Product) getItem(i);

        TextView amount = (TextView) view.getTag(R.id.subscription_amount);
        TextView detail = (TextView) view.getTag(R.id.subscription_detail);

        String description = item.getName() + " / " + oneDecimal.format(item.getStorageSize()) +
                item.getStorageUnit() + " / " + item.getPeriodicity();
        amount.setText(nf.format(item.getPrice()));
        detail.setText(description);
        return view;
    }
}
