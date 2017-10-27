package com.americavoice.backup.payment.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by javier on 10/24/17.
 */

public class SubscriptionDummy implements Parcelable {
    public String amount;
    public String description;
    public String startDate;
    public String nextPaymentDate;

    public static SubscriptionDummy dummy() {
        return new SubscriptionDummy("$50", "50 GB / month", "2017-01-01", "2018-01-01");
    }

    public SubscriptionDummy(String amount, String description, String startDate,
                             String nextPaymentDate) {

        this.amount = amount;
        this.description = description;
        this.startDate = startDate;
        this.nextPaymentDate = nextPaymentDate;
    }

    public SubscriptionDummy(Parcel in) {
        amount = in.readString();
        description = in.readString();
        startDate = in.readString();
        nextPaymentDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(amount);
        parcel.writeString(description);
        parcel.writeString(startDate);
        parcel.writeString(nextPaymentDate);
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
