package com.americavoice.backup.payment.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by javier on 10/24/17.
 */

public class SubscriptionDummy implements Parcelable {
    public String amount;
    public String description;

    public SubscriptionDummy(String amount, String description) {
        this.amount = amount;
        this.description = description;
    }

    public SubscriptionDummy(Parcel in) {
        amount = in.readString();
        description = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(amount);
        parcel.writeString(description);
    }

    public static final Parcelable.Creator<SubscriptionDummy> CREATOR =
            new Parcelable.Creator<SubscriptionDummy>() {

                @Override
                public SubscriptionDummy createFromParcel(Parcel parcel) {
                    return new SubscriptionDummy(parcel);
                }

                @Override
                public SubscriptionDummy[] newArray(int i) {
                    return new SubscriptionDummy[i];
                }
            };


}
