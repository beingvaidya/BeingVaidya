package com.mayurkakade.beingvaidya.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.ui.activities.SplashScreenActivity;

import java.util.Objects;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    MessagingUtils messagingUtils;
    public static final String TAG = "MFIS";
    OnUpdateToken onUpdateToken;
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        updateToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG,"in on received" + "FMS");
        int notificationType = Integer.parseInt(Objects.requireNonNull(remoteMessage.getData().get("notificationType")));
        String docId = remoteMessage.getData().get("docId");
        messagingUtils = MessagingUtils.getInstance();
        messagingUtils.createNotification(remoteMessage.getData().get("body"),remoteMessage.getData().get("title"),this,true ,notificationType, docId);
        
//        }

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

    private void sendNotification(RemoteMessage remoteMessage) {
        Log.d("cloud_msg","in send notification");

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]",""));

        Log.d("check_remote_msg", "user : " + user + "\n");
        Log.d("check_remote_msg", "icon : " + icon + "\n" );
        Log.d("check_remote_msg", "title : " + title + "\n");
        Log.d("check_remote_msg", "body : " + body + "\n" );

        Intent intent;
        Bundle bundle = new Bundle();
        intent = new Intent(this, SplashScreenActivity.class);
//        bundle.putString("userId",FirebaseAuth.getInstance().getCurrentUser().getUid());
/*
        if (body.contains("you have new order!")) {
            intent = new Intent(this, ShopOrdersActivity.class);
            bundle.putString("userId",FirebaseAuth.getInstance().getCurrentUser().getUid());
        }else {
            intent = new Intent(this, ActivityChatRoom.class);
            bundle.putString("userId",user);
        }*/
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,j,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"CHANNEL_ID")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int i = 0;
        if (j > 0) {
            i = j;
        }

        notificationManager.notify(i,builder.build());


    }
}
