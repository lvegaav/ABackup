package com.americavoice.backup.sms.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.americavoice.backup.R;
import com.americavoice.backup.sms.ui.model.Sms;
import com.crashlytics.android.Crashlytics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by angelchanquin on 8/18/17.
 */

public class SmsListAdapter extends RecyclerView.Adapter<SmsListFragment.SmsItemViewHolder> {
    private List<Sms> mList;

    private Context context;

    SmsListAdapter(Context context, List<Sms> list) {
        this.mList = list;
        this.context = context;
    }


    public void replaceList(List<Sms> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @Override
    public SmsListFragment.SmsItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sms_list_item, parent, false);

        return new SmsListFragment.SmsItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SmsListFragment.SmsItemViewHolder holder, final int position) {
        try {
            final int verifiedPosition = holder.getAdapterPosition();
            final Sms item = mList.get(verifiedPosition);

            if (item != null) {
                holder.getName().setText(getContactDisplayNameByNumber(item.getAddress()));
                long millis = Long.parseLong(item.getTime()) * 1000;

                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.US);
                sdf.setTimeZone(TimeZone.getDefault());
                String date = sdf.format(new Date(millis));

                holder.getMessage().setText(item.getMsg());
                holder.getDate().setText(date);
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private String getContactDisplayNameByNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = number;

        ContentResolver contentResolver = this.context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return name;
    }
}
