package com.americavoice.backup.login.model;

/**
 * Created by punke on 23-Aug-17.
 */

public class SpinnerItem {
    private String Id;
    private String Value;

    public SpinnerItem(String id, String value) {
        Id = id;
        Value = value;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }
}
