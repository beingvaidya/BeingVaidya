package com.mayurkakade.beingvaidya.data.models;

import com.google.firebase.Timestamp;

public class LearningModel extends DocId {
    Timestamp currentTime;
    String title, description,product_id , thumbnail;
    long price;

    public LearningModel() {
    }

    public LearningModel(Timestamp currentTime, String title, String description, String product_id, long price) {
        this.currentTime = currentTime;
        this.title = title;
        this.description = description;
        this.product_id = product_id;
        this.price = price;
    }

    public Timestamp getCurrentTime() {
        return currentTime;
    }

    public String getThumbnail() {
        return thumbnail = "https://images-na.ssl-images-amazon.com/images/I/51Warh2mBVL.png";
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setCurrentTime(Timestamp currentTime) {
        this.currentTime = currentTime;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }
}
