package com.americavoice.backup.contacts.ui;

import static com.americavoice.backup.contacts.ui.ContactsBackupFragment.getDisplayName;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.americavoice.backup.R;
import com.americavoice.backup.main.ui.widget.TextDrawable;
import com.americavoice.backup.utils.BitmapUtils;
import com.crashlytics.android.Crashlytics;

import java.util.List;

import ezvcard.VCard;


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

            // photo
            if (vcard.getPhotos().size() > 0) {
                byte[] data = vcard.getPhotos().get(0).getData();

                Bitmap thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length);
                RoundedBitmapDrawable drawable = BitmapUtils.bitmapToCircularBitmapDrawable(context.getResources(),
                  thumbnail);

                holder.getBadge().setImageDrawable(drawable);
            } else {
                try {
                    holder.getBadge().setImageDrawable(
                      TextDrawable.createNamedAvatar(
                        holder.getName().getText().toString(),
                        context.getResources().getDimension(R.dimen.list_item_avatar_icon_radius)
                      )
                    );
                } catch (Exception e) {
                    holder.getBadge().setImageResource(R.mipmap.ic_user);
                }
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