package com.americavoice.backup.login.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.AppCompatDrawableManager;
import android.text.TextUtils;
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

    private  Context mContext;
    public SpinnerItemAdapter(Context context, int resource, List<SpinnerItem> items) {
        super(context, resource, items);
        mContext = context;
    }

    // Affects default (closed) state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setText((!TextUtils.isEmpty(getItem(position).getId())? "+":"") + getItem(position).getId());
        view.setTextColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_glove, 0, R.drawable.ic_arrow_down, 0);
        } else {
            Drawable globe = AppCompatDrawableManager.get().getDrawable(mContext, R.drawable.ic_glove);
            Drawable arrow = AppCompatDrawableManager.get().getDrawable(mContext, R.drawable.ic_arrow_down);
            view.setCompoundDrawablesWithIntrinsicBounds(globe, null, arrow, null);
        }

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