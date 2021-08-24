package com.mayurkakade.beingvaidya.notification;

public interface OnUpdateToken {
    public void onStart();
    public void onSuccess(String token);
    public void onFailure(String message);
}
