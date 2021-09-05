package com.mayurkakade.beingvaidya.data.models;

import com.google.firebase.Timestamp;

public class PatientsCommunityImageModel extends DocId {
    String doctor_id;
    String downloadUri;
    String description;
    Timestamp currentTime;
    String doctor_profile_photo;

    public String getDoctor_profile_photo() {
        return doctor_profile_photo;
    }

    public void setDoctor_profile_photo(String doctor_profile_photo) {
        this.doctor_profile_photo = doctor_profile_photo;
    }

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

    String doctorName;
    String doctorImage;

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorImage() {
        return doctorImage;
    }

    public void setDoctorImage(String doctorImage) {
        this.doctorImage = doctorImage;
    }
}
