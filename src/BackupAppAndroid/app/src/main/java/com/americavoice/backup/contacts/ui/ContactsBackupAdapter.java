package com.americavoice.backup.contacts.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.americavoice.backup.R;
import com.crashlytics.android.Crashlytics;

import java.util.List;

import ezvcard.VCard;

import static com.americavoice.backup.contacts.ui.ContactsBackupFragment.getDisplayName;


/**
 * Created by pj on 1/21/18.
 */

public class ContactsBackupAdapter extends RecyclerView.Adapter<ContactsBackupFragment.ContactItemViewHolder> {
    private List<VCard> vCards;

    private Context context;

    ContactsBackupAdapter(Context context, List<VCard> vCards) {
        this.vCards = vCards;
        this.context = context;
    }

    void replaceVCards(List<VCard> vCards) {
        this.vCards = vCards;
        notifyDataSetChanged();
    }

    @Override
    public ContactsBackupFragment.ContactItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.last_backup_contacts_item, parent, false);

        return new ContactsBackupFragment.ContactItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ContactsBackupFragment.ContactItemViewHolder holder, final int position) {

        try {
            final int verifiedPosition = holder.getAdapterPosition();
            final VCard vcard = vCards.get(verifiedPosition);

            if (vcard != null) {
                holder.getName().setText(getDisplayName(vcard));
            }

        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    public int getItemCount() {
        return vCards.size();
    }
}