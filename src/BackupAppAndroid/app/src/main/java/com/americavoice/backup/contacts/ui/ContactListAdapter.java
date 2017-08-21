package com.americavoice.backup.contacts.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.americavoice.backup.R;
import com.americavoice.backup.main.event.VCardToggleEvent;
import com.americavoice.backup.main.ui.widget.TextDrawable;
import com.americavoice.backup.utils.BitmapUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ezvcard.VCard;

import static com.americavoice.backup.contacts.ui.ContactListFragment.getDisplayName;

/**
 * Created by angelchanquin on 8/18/17.
 */

public class ContactListAdapter extends RecyclerView.Adapter<ContactListFragment.ContactItemViewHolder> {
    private List<VCard> vCards;
    private Set<Integer> checkedVCards;

    private Context context;

    ContactListAdapter(Context context, List<VCard> vCards) {
        this.vCards = vCards;
        this.context = context;
        this.checkedVCards = new HashSet<>();
    }

    ContactListAdapter(Context context, List<VCard> vCards,
                       Set<Integer> checkedVCards) {
        this.vCards = vCards;
        this.context = context;
        this.checkedVCards = checkedVCards;
    }

    public int getCheckedCount() {
        if (checkedVCards != null) {
            return checkedVCards.size();
        } else {
            return 0;
        }
    }

    public void replaceVCards(List<VCard> vCards) {
        this.vCards = vCards;
        notifyDataSetChanged();
    }

    public int[] getCheckedIntArray() {
        int[] intArray;
        if (checkedVCards != null && checkedVCards.size() > 0) {
            intArray = new int[checkedVCards.size()];
            int i = 0;
            for (int position : checkedVCards) {
                intArray[i] = position;
                i++;
            }
            return intArray;
        } else {
            intArray = new int[0];
            return intArray;
        }
    }

    @Override
    public ContactListFragment.ContactItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contactlist_list_item, parent, false);

        return new ContactListFragment.ContactItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ContactListFragment.ContactItemViewHolder holder, final int position) {
        final int verifiedPosition = holder.getAdapterPosition();
        final VCard vcard = vCards.get(verifiedPosition);

        if (vcard != null) {

            if (checkedVCards.contains(position)) {
                holder.getName().setChecked(true);

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    holder.getName().getCheckMarkDrawable()
//                            .setColorFilter(ThemeUtils.primaryAccentColor(), PorterDuff.Mode.SRC_ATOP);
//                }
            } else {
                holder.getName().setChecked(false);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    holder.getName().getCheckMarkDrawable().clearColorFilter();
                }
            }

            holder.getName().setText(getDisplayName(vcard));

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

            // Checkbox
            holder.setVCardListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.getName().setChecked(!holder.getName().isChecked());

                    if (holder.getName().isChecked()) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                            holder.getName().getCheckMarkDrawable()
//                                    .setColorFilter(ThemeUtils.primaryAccentColor(), PorterDuff.Mode.SRC_ATOP);
//                        }

                        if (!checkedVCards.contains(verifiedPosition)) {
                            checkedVCards.add(verifiedPosition);
                        }
                        if (checkedVCards.size() == 1) {
                            EventBus.getDefault().post(new VCardToggleEvent(true));
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            holder.getName().getCheckMarkDrawable().clearColorFilter();
                        }

                        if (checkedVCards.contains(verifiedPosition)) {
                            checkedVCards.remove(verifiedPosition);
                        }

                        if (checkedVCards.size() == 0) {
                            EventBus.getDefault().post(new VCardToggleEvent(false));
                        }
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return vCards.size();
    }

    public void selectAllFiles(boolean select) {
        checkedVCards = new HashSet<>();
        if (select) {
            for (int i = 0; i < vCards.size(); i++) {
                checkedVCards.add(i);
            }
        }

        if (checkedVCards.size() > 0) {
            EventBus.getDefault().post(new VCardToggleEvent(true));
        } else {
            EventBus.getDefault().post(new VCardToggleEvent(false));
        }

        notifyDataSetChanged();
    }

}
