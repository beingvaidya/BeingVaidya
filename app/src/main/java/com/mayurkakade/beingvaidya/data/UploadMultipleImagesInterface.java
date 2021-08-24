package com.mayurkakade.beingvaidya.data;

import android.net.Uri;

import java.util.List;

public interface UploadMultipleImagesInterface {
    public void onStart();
    public void onProgress(int progress);
    public void onSuccess(List<String> downloadUrl, String field);
    public void addUrl(String url, int size);
    public void onFailure();
}
