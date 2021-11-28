package com.mayurkakade.beingvaidya.data.models;

public class PdfModel extends DocId {
    String title;
    String downloadUrl;
    boolean isPurchased;
    String thumbnail;

    public PdfModel() {
    }

    public PdfModel(String downloadUrl, String title, boolean isPurchased,String thumbnail) {
        this.title = title;
        this.downloadUrl = downloadUrl;
        this.isPurchased = isPurchased;
        this.thumbnail = thumbnail;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isPurchased() {
        return isPurchased;
    }

    public void setPurchased(boolean purchased) {
        isPurchased = purchased;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
