package com.mayurkakade.beingvaidya.data.models;

public class NotificationModel extends DocId{

    String docId;
    String msg;
    String sender;
    long notificationType;

    public NotificationModel() {
    }

    public NotificationModel(String docId, String msg, String sender, long notificationType) {
        this.docId = docId;
        this.msg = msg;
        this.sender = sender;
        this.notificationType = notificationType;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(long notificationType) {
        this.notificationType = notificationType;
    }
}
