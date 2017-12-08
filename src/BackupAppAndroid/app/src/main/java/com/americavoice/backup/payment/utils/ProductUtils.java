package com.americavoice.backup.payment.utils;

import com.americavoice.backup.main.network.dtos;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by javier on 10/30/17.
 */

public class ProductUtils {

    public static String detailsFromProduct(dtos.Product item) {

        NumberFormat oneDecimal = NumberFormat.getInstance();
        oneDecimal.setMinimumFractionDigits(0);
        oneDecimal.setMaximumFractionDigits(2);

        return item.getName() + " / " + oneDecimal.format(item.getStorageSize()) +
                item.getStorageUnit() + " / " + item.getPeriodicity();

    }

    public static String amountFromProduct(dtos.Product product) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
        return nf.format(product.getPrice());
    }
}
