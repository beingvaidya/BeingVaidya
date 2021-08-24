package com.mayurkakade.beingvaidya.notification;

public class Data {
    private String receiver;
    private int icon;
    private String body;
    private String title;
    private String sender;
    private int notificationType;
    private String docId;

    public Data(String receiver, int icon, String body, String title, String sender, int notificationType, String docId) {
        this.receiver = receiver;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sender = sender;
        this.notificationType = notificationType;
        this.docId = docId;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Data() {
    }


}
