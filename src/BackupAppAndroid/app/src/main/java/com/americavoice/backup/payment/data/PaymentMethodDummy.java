package com.americavoice.backup.payment.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by javier on 10/24/17.
 */

public class PaymentMethodDummy implements Parcelable {
    public String method;
    public String digits;

    public PaymentMethodDummy() {

    }

    public PaymentMethodDummy(Parcel in) {
        method = in.readString();
        digits = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(method);
        parcel.writeString(digits);
    }

    public static final Parcelable.Creator<PaymentMethodDummy> CREATOR =
            new Parcelable.Creator<PaymentMethodDummy>() {

                @Override
                public PaymentMethodDummy createFromParcel(Parcel parcel) {
                    return new PaymentMethodDummy(parcel);
                }

                @Override
                public PaymentMethodDummy[] newArray(int i) {
                    return new PaymentMethodDummy[i];
                }
            };

}
