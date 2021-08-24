package com.mayurkakade.beingvaidya.data;

import android.net.Uri;

public interface UploadToStorageInterface {
    public void onStart();
    public void onProgress(int progress);
    public void onSuccess(Uri downloadUri,String field);
    public void onFailure();
}
