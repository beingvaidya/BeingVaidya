package com.mayurkakade.beingvaidya.data.models;

import com.google.firebase.Timestamp;

public class StoreModel extends DocId{
    Timestamp currentTime;
    String contact_no,description,downloadUrl,title,tags;
    long price;

    public StoreModel(Timestamp currentTime, String contact_no, String description, String downloadUrl, String title, String tags, long price) {
        this.currentTime = currentTime;
        this.contact_no = contact_no;
        this.description = description;
        this.downloadUrl = downloadUrl;
        this.title = title;
        this.tags = tags;
        this.price = price;
    }

    public StoreModel() {
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Timestamp getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Timestamp currentTime) {
        this.currentTime = currentTime;
    }

    public String getContact_no() {
        return contact_no;
    }

    public void setContact_no(String contact_no) {
        this.contact_no = contact_no;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }
}
