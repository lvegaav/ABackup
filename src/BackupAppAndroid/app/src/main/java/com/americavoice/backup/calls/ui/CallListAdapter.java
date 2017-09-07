package com.americavoice.backup.calls.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.americavoice.backup.R;
import com.americavoice.backup.calls.ui.model.Call;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by angelchanquin on 8/18/17.
 */

public class CallListAdapter extends RecyclerView.Adapter<CallListFragment.CallItemViewHolder> {
    private List<Call> mList;

    private Context context;

    CallListAdapter(Context context, List<Call> list) {
        this.mList = list;
        this.context = context;
    }


    public void replaceList(List<Call> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @Override
    public CallListFragment.CallItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.calllist_list_item, parent, false);

        return new CallListFragment.CallItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CallListFragment.CallItemViewHolder holder, final int position) {
        final int verifiedPosition = holder.getAdapterPosition();
        final Call item = mList.get(verifiedPosition);

        if (item != null) {
            holder.getName().setText(getContactDisplayNameByNumber(item.getPhoneNumber()));
            long millis = Long.parseLong(item.getCallDuration()) * 1000;
            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            df.setTimeZone(tz);
            String time = df.format(new Date(millis));


            millis = Long.parseLong(item.getCallDate());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getDefault());
            String date = sdf.format(new Date(millis));

            holder.getDuration().setText(time);
            holder.getDate().setText(date);
            switch (Integer.parseInt(item.getCallType())) {
                case CallLog.Calls.OUTGOING_TYPE:
                    holder.getDate().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_made, 0, 0, 0);
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    holder.getDate().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_received, 0, 0, 0);
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    holder.getDate().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_missed, 0, 0, 0);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public String getContactDisplayNameByNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = number;

        ContentResolver contentResolver = this.context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return name;
    }
}
