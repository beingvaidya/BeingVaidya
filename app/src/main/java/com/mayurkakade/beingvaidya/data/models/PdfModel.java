package com.mayurkakade.beingvaidya.data.models;

public class PdfModel extends DocId {
    String title;
    String downloadUrl;
    boolean isPurchased;

    public PdfModel() {
    }

    public PdfModel(String downloadUrl, String title, boolean isPurchased) {
        this.title = title;
        this.downloadUrl = downloadUrl;
        this.isPurchased = isPurchased;
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
