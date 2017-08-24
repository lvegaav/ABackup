package com.americavoice.backup.login.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.login.model.SpinnerItem;

import java.util.List;

/**
 * Created by punke on 23-Aug-17.
 */

public class SpinnerItemAdapter extends ArrayAdapter<SpinnerItem> {

    public SpinnerItemAdapter(Context context, int resource, List<SpinnerItem> items) {
        super(context, resource, items);
    }

    // Affects default (closed) state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setText(getItem(position).getValue());
        view.setTextColor(Color.WHITE);
        view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_glove, 0, R.drawable.ic_arrow_down, 0);
        return view;
    }

    // Affects opened state of the spinner
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        view.setText(getItem(position).getValue());
        view.setTextColor(Color.BLACK);
        return view;
    }
}