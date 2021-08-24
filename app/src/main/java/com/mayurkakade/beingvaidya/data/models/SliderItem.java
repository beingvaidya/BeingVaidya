package com.mayurkakade.beingvaidya.data.models;

import android.net.Uri;

public class SliderItem {
    Uri imgUri;
    String url;

    public SliderItem() {
    }

    public SliderItem(String url) {
        this.imgUri = imgUri;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Uri getImgUri() {
        return imgUri;
    }

    public void setImgUri(Uri imgUri) {
        this.imgUri = imgUri;
    }

    public SliderItem(Uri imgUri) {
        this.imgUri = imgUri;
    }
}
