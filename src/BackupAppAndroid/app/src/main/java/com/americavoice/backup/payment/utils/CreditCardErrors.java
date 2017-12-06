package com.americavoice.backup.payment.utils;

import android.content.Context;

import com.americavoice.backup.R;

import net.servicestack.client.ResponseError;

/**
 * Created by javier on 11/2/17.
 */

public class CreditCardErrors {

    public static String errorMessage(Context context, ResponseError error) {
        int resId;
        switch (error.getErrorCode()) {
            case "PGE0039":
                resId = R.string.credit_card_error_PGE0039;
                break;
            case "PGE0042":
                resId = R.string.credit_card_error_PGE0042;
                break;
            case "PGE0002":
                resId = R.string.credit_card_error_PGE0002;
                break;
            case "PGE0006":
                resId = R.string.credit_card_error_PGE0006;
                break;
            case "PGE0007":
                resId = R.string.credit_card_error_PGE0007;
                break;
            case "PGE0008":
                resId = R.string.credit_card_error_PGE0008;
                break;
            case "PGE0017":
                resId = R.string.credit_card_error_PGE0017;
                break;
            case "PGE0019":
                resId = R.string.credit_card_error_PGE0017;
                break;
            case "PGE0044":
                resId = R.string.credit_card_error_PGE0044;
                break;
            case "PGE0045":
                resId = R.string.credit_card_error_PGE0045;
                break;
            case "PGE0127":
                resId = R.string.credit_card_error_PGE0127;
                break;
            default:
                resId = R.string.payment_error_createCreditCard;
        }
        return context.getString(resId);
    }
}
