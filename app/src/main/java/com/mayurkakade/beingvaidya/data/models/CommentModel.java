package com.mayurkakade.beingvaidya.data.models;

import com.google.firebase.Timestamp;

public class CommentModel {
    String docId;
    String commentText;
    Timestamp timestamp;
    String doctorName;

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public CommentModel() {
    }

    public CommentModel(String docId, String commentText, Timestamp timestamp) {
        this.docId = docId;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
