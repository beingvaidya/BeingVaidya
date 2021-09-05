package com.mayurkakade.beingvaidya.data.models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class FeedModel extends DocId{
    String doctor_id;
    String title;
    String caption;
    String img_url;
    Timestamp currentTime;
    boolean isBanner = false;
    List<SliderItem> mSliderItems = new ArrayList<>() ;

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


    public List<SliderItem> getmSliderItems() {
        return mSliderItems;
    }

    public void setmSliderItems(List<SliderItem> mSliderItems) {
        this.mSliderItems = mSliderItems;
    }


    ///Doctor Details

    String doctorName;
    String comment_show;
    String view_show;
    String doctor_profile_photo;

    public List<SliderItem> getmSliderItemsDoctor() {
        return mSliderItemsDoctor;
    }

    public void setmSliderItemsDoctor(List<SliderItem> mSliderItemsDoctor) {
        this.mSliderItemsDoctor = mSliderItemsDoctor;
    }

    List<SliderItem> mSliderItemsDoctor  = new ArrayList<>();
    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getComment_show() {
        return comment_show;
    }

    public void setComment_show(String comment_show) {
        this.comment_show = comment_show;
    }

    public String getView_show() {
        return view_show;
    }

    public void setView_show(String view_show) {
        this.view_show = view_show;
    }

    public String getDoctor_profile_photo() {
        return doctor_profile_photo;
    }

    public void setDoctor_profile_photo(String doctor_profile_photo) {
        this.doctor_profile_photo = doctor_profile_photo;
    }
}
