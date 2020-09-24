package com.example.socio.models;

public class ModelNotification {
    String pId,timestamp,pUid,notificcation,sUid,sName,sEmail,sImage;

    public ModelNotification() {
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getpUid() {
        return pUid;
    }

    public void setpUid(String pUid) {
        this.pUid = pUid;
    }

    public String getNotificcation() {
        return notificcation;
    }

    public void setNotificcation(String notificcation) {
        this.notificcation = notificcation;
    }

    public String getsUid() {
        return sUid;
    }

    public void setsUid(String sUid) {
        this.sUid = sUid;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getsEmail() {
        return sEmail;
    }

    public void setsEmail(String sEmail) {
        this.sEmail = sEmail;
    }

    public String getsImage() {
        return sImage;
    }

    public void setsImage(String sImage) {
        this.sImage = sImage;
    }

    public ModelNotification(String pId, String timestamp, String pUid, String notificcation, String sUid, String sName, String sEmail, String sImage) {
        this.pId = pId;
        this.timestamp = timestamp;
        this.pUid = pUid;
        this.notificcation = notificcation;
        this.sUid = sUid;
        this.sName = sName;
        this.sEmail = sEmail;
        this.sImage = sImage;
    }
}