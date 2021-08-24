package com.mayurkakade.beingvaidya.data.models;


import com.google.firebase.Timestamp;

public class FirebaseImageModel{
    String downloadUri;
    Timestamp currentTime;

    public FirebaseImageModel() {
    }

    public FirebaseImageModel(String downloadUri, Timestamp currentTime) {
        this.downloadUri = downloadUri;
        this.currentTime = currentTime;
    }

    public String getDownloadUri() {
        return downloadUri;
    }

    public void setDownloadUri(String downloadUri) {
        this.downloadUri = downloadUri;
    }

    public Timestamp getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Timestamp currentTime) {
        this.currentTime = currentTime;
    }
}
