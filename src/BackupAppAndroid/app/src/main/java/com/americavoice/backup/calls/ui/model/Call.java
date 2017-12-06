package com.americavoice.backup.calls.ui.model;

import com.google.gson.Gson;

/**
 * Created by punke on 30-Aug-17.
 */

public class Call {
    String phoneNumber;
    String callType;
    String callDate;
    String callDuration;

    public Call(String phoneNumber, String callType, String callDate, String callDuration) {
        this.phoneNumber = phoneNumber;
        this.callType = callType;
        this.callDate = callDate;
        this.callDuration = callDuration;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCallType() {
        return callType;
    }

    public String getCallDate() {
        return callDate;
    }

    public String getCallDuration() {
        return callDuration;
    }

    public String ToJson()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Call FromJson(String json)
    {
        Gson gson = new Gson();
        return gson.fromJson(json, Call.class);
    }
}
