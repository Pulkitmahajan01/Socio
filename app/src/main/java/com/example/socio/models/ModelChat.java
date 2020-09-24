package com.example.socio.models;

import com.google.firebase.database.PropertyName;

public class ModelChat {

    String message,sender,reciever,timeStamp,type;
    boolean isSeen;

    public ModelChat(){};


    public ModelChat(String message, String sender, String reciever, String timeStamp,String type,boolean isSeen) {
        this.message = message;
        this.sender = sender;
        this.reciever = reciever;
        this.timeStamp = timeStamp;
        this.type = type;
        this.isSeen = isSeen;


    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReciever() {
        return reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @PropertyName("isSeen")
    public boolean isSeen() {
        return isSeen;
    }

    @PropertyName("isSeen")
    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}

