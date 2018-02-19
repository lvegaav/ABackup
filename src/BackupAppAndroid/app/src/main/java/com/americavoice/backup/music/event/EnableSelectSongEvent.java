package com.americavoice.backup.music.event;

import com.americavoice.backup.main.event.Event;

/**
 * Created by pj on 2/16/18.
 */

public class EnableSelectSongEvent extends Event {

    public boolean enableBtn;


    public EnableSelectSongEvent() {
        // no-op
    }

    public boolean isEnableBtn() {
        return enableBtn;
    }

    public void setEnableBtn(boolean enableBtn) {
        this.enableBtn = enableBtn;
    }
}
