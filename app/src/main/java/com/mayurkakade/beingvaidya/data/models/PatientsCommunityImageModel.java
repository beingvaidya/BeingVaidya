package com.mayurkakade.beingvaidya.data.models;

import com.google.firebase.Timestamp;

public class PatientsCommunityImageModel extends DocId {
    String doctor_id;
    String downloadUri;
    String description;
    Timestamp currentTime;

    public PatientsCommunityImageModel() {
    }

    public PatientsCommunityImageModel(String doctor_id, String downloadUri, String description, Timestamp currentTime) {
        this.doctor_id = doctor_id;
        this.downloadUri = downloadUri;
        this.description = description;
        this.currentTime = currentTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(String doctor_id) {
        this.doctor_id = doctor_id;
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
