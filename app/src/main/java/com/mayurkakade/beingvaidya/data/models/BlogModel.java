package com.mayurkakade.beingvaidya.data.models;

import com.google.firebase.Timestamp;

public class BlogModel extends DocId{
    String content;
    Timestamp currentTime;
    String img_url;
    String title;
    String uploader;
    String tag;

    public BlogModel() {
    }

    public BlogModel(String content, Timestamp currentTime, String img_url, String title, String uploader, String tag) {
        this.content = content;
        this.currentTime = currentTime;
        this.img_url = img_url;
        this.title = title;
        this.uploader = uploader;
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Timestamp currentTime) {
        this.currentTime = currentTime;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }
}
