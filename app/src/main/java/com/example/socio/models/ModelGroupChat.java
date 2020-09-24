package com.example.socio.models;

public class ModelGroupChat {
    String sender,message,timeStamp,type;

    public ModelGroupChat() {
    }

    public ModelGroupChat(String sender, String message, String timeStamp, String type) {
        this.sender = sender;
        this.message = message;
        this.timeStamp = timeStamp;
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
}
