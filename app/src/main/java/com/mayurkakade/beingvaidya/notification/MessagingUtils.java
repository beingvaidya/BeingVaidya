package com.mayurkakade.beingvaidya.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor;
import com.mayurkakade.beingvaidya.ui.activities.ActivityNotifications;
import com.mayurkakade.beingvaidya.ui.activities.ActivityPatient;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagingUtils {

    public static final String TAG = "MessagingUtils";
    private static APIService apiService;
    private MessagingUtils() {}
    private static MessagingUtils instance;

    public static MessagingUtils getInstance() {
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        if (instance != null) {
            return instance;
        } else {
            instance = new MessagingUtils();
            return instance;
        }
    }

    public void updateToken(String newToken,boolean isDoctor, OnUpdateToken onUpdateToken){
        onUpdateToken.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Map<String ,Object> tokenMap = new HashMap<>();
        tokenMap.put("myToken",newToken);
        String collectionAddress;
        if (isDoctor) {
            collectionAddress = "Doctors/";
        } else {
            collectionAddress = "Patients/";
        }
        if (user != null) {
            firebaseFirestore.collection(collectionAddress+user.getPhoneNumber()+"/token").document("myToken")
                    .set(tokenMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                onUpdateToken.onSuccess(newToken);
                                Log.d(TAG, "onComplete: Token Updated Successfully");
                            } else {
                                onUpdateToken.onFailure(Objects.requireNonNull(task.getException()).getMessage());
                                Log.d(TAG, "onComplete: Error Updating Token : " + Objects.requireNonNull(task.getException()).getMessage());
                            }
                        }
                    });
        } else {
            onUpdateToken.onFailure("User Not Logged In");
        }
    }

    public void sendCloudNotification(String receiver, String sender, String msg, boolean isDoctor, int notificationType, String docId) {
        String collectionAddress;
        if (isDoctor) {
            collectionAddress = "Doctors/";
        } else {
            collectionAddress = "Patients/";
        }

        addUserNotification(receiver,sender,msg,isDoctor,notificationType,docId);

        FirebaseFirestore firebaseFirestore  = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(collectionAddress+receiver + "/token").document("myToken").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        String token = Objects.requireNonNull(task.getResult()).getString("myToken");
                        Token token1 = new Token();
                        token1.setToken(token);

                        firebaseFirestore.collection(collectionAddress).document(receiver).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    String username = task.getResult().getString("name");
                                    Data data =  new Data(receiver, R.drawable.ic_baseline_keyboard_arrow_down_24,username +" : "  + msg, "New Notification", sender,notificationType,docId);
                                    Sender sender = new Sender(data,token);
                                    apiService.sendNotification(sender)
                                            .enqueue(new Callback<MyResponse>() {
                                                @Override
                                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                                    Log.d(TAG,"response : " + response);
                                                }
                                                @Override
                                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                                    Log.e(TAG, "onFailure: " + t.getMessage() );
                                                }
                                            });

                                }
                            }
                        });

                    }
                });




    }

    private void addUserNotification(String receiver, String sender, String msg, boolean isDoctor, int notificationType, String docId) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Map<String,Object> params = new HashMap<>();
        params.put("sender",sender);
        params.put("msg",msg);
        params.put("notificationType", notificationType);
        params.put("docId",docId);
        params.put("currentTime", Calendar.getInstance().getTimeInMillis());

        String collectionAddress;
        if (isDoctor) {
            collectionAddress = "Doctors/";
        } else {
            collectionAddress = "Patients/";
        }

        firebaseFirestore.collection(collectionAddress+receiver+"/notifications").add(params)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {

                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });

    }


    public void createNotification(String message, String title, Context context, boolean isDoctor, int notificationType, String docId) {

        Log.d(TAG, "createNotification: " + "NotificationType : " + notificationType);

        NotificationManager mNotificationManager;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context.getApplicationContext(), "notify_001");
        Intent i1;
        if (isDoctor) {
            i1 = new Intent(context.getApplicationContext(), ActivityNotifications.class);
        } else {
            i1 = new Intent(context.getApplicationContext(), ActivityNotifications.class);
        }
        i1.putExtra("notificationType",notificationType);
        i1.putExtra("docId",docId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i1, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(message);
        bigText.setBigContentTitle(title);
        bigText.setSummaryText(message);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(message);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "notify_001";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = null;
            channel = new NotificationChannel(
                    channelId,
                    "MyChannel",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
        }
        mBuilder.setChannelId(channelId);


        mNotificationManager.notify(0, mBuilder.build());
    }
}
