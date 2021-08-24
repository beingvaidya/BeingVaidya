package com.mayurkakade.beingvaidya.notification;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;


public class MyFirebaseIdService extends FirebaseMessagingService {
    MessagingUtils messagingUtils;
    public static final String TAG = "MFIS";
    OnUpdateToken onUpdateToken;

    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived: " + remoteMessage.toString());
//        messagingUtils = MessagingUtils.getInstance();
//        messagingUtils.createNotification(remoteMessage.getData().get("body"),remoteMessage.getData().get("title"),this,true );
    }

    @Override
    public void onNewToken(@NotNull String s) {
        super.onNewToken(s);
        Log.d("NEW_TOKEN",s);
        if (FirebaseAuth.getInstance().getCurrentUser()!=null) {
            updateToken(s);
        }
    }

    private void updateToken(String s) {
        onUpdateToken = new OnUpdateToken() {
            @Override
            public void onStart() {
                Log.d(TAG, "onStart: " + " Token Update Started");
            }

            @Override
            public void onSuccess(String token) {
                Log.d(TAG, "onSuccess: " + "Token Updated Successfully : " + token);
            }

            @Override
            public void onFailure(String message) {
                Log.e(TAG, "onFailure: " + "Token Update Failed : " + message );
            }
        };
        messagingUtils = MessagingUtils.getInstance();
        messagingUtils.updateToken(s,true,onUpdateToken);
    }
}
