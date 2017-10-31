package com.americavoice.backup.payment.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.americavoice.backup.main.network.dtos;

/**
 * Created by javier on 10/24/17.
 */

public class PaymentMethod implements Parcelable {
    public enum PaymentMethodType {
        CREDIT_CARD,
        PAY_PAL;

        private final static String PAY_PAL_STRING = "PayPal";
        private final static String CREDIT_CARD_STRING = "Card";

        public static PaymentMethodType fromString(String string) {
            if (string.equals(PAY_PAL_STRING)) {
                return PAY_PAL;
            } else if (string.equals(CREDIT_CARD_STRING)) {
                return CREDIT_CARD;
            }
            throw new RuntimeException("No payment method found for " + string);
        }

        public String toString() {
            if (this == CREDIT_CARD) {
                return CREDIT_CARD_STRING;
            } else {
                return PAY_PAL_STRING;
            }
        }
    }

    public PaymentMethodType paymentMethodType;
    public String creditCardNumber;
    public String expirationDate;


    public PaymentMethod(String paymentMethodType, String creditCardNumber, String expirationDate) {
        this.paymentMethodType = PaymentMethodType.fromString(paymentMethodType);
        this.creditCardNumber = creditCardNumber;
        if (expirationDate != null) {
            this.expirationDate = expirationDate.replace("-", " / ");
        }
    }

    public PaymentMethod(Parcel in) {
        this(in.readString(), in.readString(), in.readString());
    }

    public PaymentMethod(dtos.GetPaymentMethodResponse dtoResponse) {
        this(dtoResponse.getPaymentType(), dtoResponse.getCardNumber(), dtoResponse.getExpirationDate());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(paymentMethodType.toString());
        parcel.writeString(creditCardNumber);
        parcel.writeString(expirationDate);
    }

    public static final Parcelable.Creator<PaymentMethod> CREATOR =
            new Parcelable.Creator<PaymentMethod>() {

                @Override
                public PaymentMethod createFromParcel(Parcel parcel) {
                    return new PaymentMethod(parcel);
                }

                @Override
                public PaymentMethod[] newArray(int i) {
                    return new PaymentMethod[i];
                }
            };

}
