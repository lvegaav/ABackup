package com.americavoice.backup.payment.ui;

import com.americavoice.backup.main.ui.ILoadDataView;

import java.util.List;

/**
 * Created by javier on 10/24/17.
 */

public interface ChoosePlanView<T> extends ILoadDataView{
    void showPlans(List<T> list);
}
