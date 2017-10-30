package com.americavoice.backup.payment.ui;

import java.util.List;

/**
 * Created by javier on 10/24/17.
 */

public interface ChoosePlanView<T>  {
    void showPlans(List<T> list);
}
