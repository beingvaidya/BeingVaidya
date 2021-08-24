package com.mayurkakade.beingvaidya.data.models;

import com.google.firebase.Timestamp;

public class FeedModel extends DocId{
    String doctor_id;
    String title;
    String caption;
    String img_url;
    Timestamp currentTime;
    boolean isBanner = false;

    public FeedModel() {
    }

    public FeedModel(String img_url, boolean isBanner) {
        this.img_url = img_url;
        this.isBanner = isBanner;
    }

    public FeedModel(String doctor_id, String title, String caption, String img_url, Timestamp currentTime) {
        this.doctor_id = doctor_id;
        this.title = title;
        this.caption = caption;
        this.img_url = img_url;
        this.currentTime = currentTime;
        this.isBanner = false;
    }

    public boolean isBanner() {
        return isBanner;
    }

    public void setBanner(boolean banner) {
        isBanner = banner;
    }

    public Timestamp getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Timestamp currentTime) {
        this.currentTime = currentTime;
    }

    public String getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(String doctor_id) {
        this.doctor_id = doctor_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
}
