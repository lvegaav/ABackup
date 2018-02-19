package com.americavoice.backup.main.event;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by pj on 2/16/18.
 */

public abstract class Event {

    public void post() {
        EventBus.getDefault().post(this);
    }
}
