package com.americavoice.backup.sms.ui.model;

import com.google.gson.Gson;

/**
 * Created by punke on 30-Aug-17.
 */

public class Sms {

    private String _address;
    private String _msg;
    private String _readState; //"0" for have not read sms and "1" for have read sms
    private String _time;
    private String _folderName;

    public String getAddress(){
        return _address;
    }
    public String getMsg(){
        return _msg;
    }
    public String getReadState(){
        return _readState;
    }
    public String getTime(){
        return _time;
    }
    public String getFolderName(){
        return _folderName;
    }

    public void setAddress(String address){
        _address = address;
    }
    public void setMsg(String msg){
        _msg = msg;
    }
    public void setReadState(String readState){
        _readState = readState;
    }
    public void setTime(String time){
        _time = time;
    }
    public void setFolderName(String folderName){
        _folderName = folderName;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Sms fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Sms.class);
    }
}
