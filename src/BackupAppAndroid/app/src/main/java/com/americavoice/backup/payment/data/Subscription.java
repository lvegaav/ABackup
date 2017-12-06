package com.americavoice.backup.payment.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.americavoice.backup.main.network.dtos;
import com.americavoice.backup.payment.utils.ProductUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by javier on 10/24/17.
 */

public class Subscription implements Parcelable {
    public String productId;
    public String amount;
    public String description;
    public String startDate;
    public String nextPaymentDate;

    
    public Subscription(String productId, String amount, String description, String startDate,
                        String nextPaymentDate) {

        this.productId = productId;
        this.amount = amount;
        this.description = description;
        this.startDate = startDate;
        this.nextPaymentDate = nextPaymentDate;
    }

    public Subscription(Parcel in) {
        productId = in.readString();
        amount = in.readString();
        description = in.readString();
        startDate = in.readString();
        nextPaymentDate = in.readString();
    }

    public Subscription(dtos.GetSubscriptionResponse subscription) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        dtos.Product product = subscription.product;
        productId = product.getProductId();
        amount = ProductUtils.amountFromProduct(product);
        description = ProductUtils.detailsFromProduct(product);
        startDate = dateFormat.format(subscription.getStartDate());
        nextPaymentDate = dateFormat.format(subscription.getNextPaymentDate());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(productId);
        parcel.writeString(amount);
        parcel.writeString(description);
        parcel.writeString(startDate);
        parcel.writeString(nextPaymentDate);
    }

    public static final Parcelable.Creator<Subscription> CREATOR =
            new Parcelable.Creator<Subscription>() {

                @Override
                public Subscription createFromParcel(Parcel parcel) {
                    return new Subscription(parcel);
                }

                @Override
                public Subscription[] newArray(int i) {
                    return new Subscription[i];
                }
            };


}
