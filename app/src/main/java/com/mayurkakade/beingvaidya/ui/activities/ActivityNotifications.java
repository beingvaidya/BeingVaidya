package com.mayurkakade.beingvaidya.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.NotificationAdapter;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;
import com.mayurkakade.beingvaidya.data.models.NotificationModel;
import com.mayurkakade.beingvaidya.data.models.PatientModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivityNotifications extends AppCompatActivity {

    List<NotificationModel> notificationList;
    NotificationAdapter adapter;
    RecyclerView recyclerView;
    public static final String TAG = "NotificationsTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(this,notificationList);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        getAllNotifications();

    }


    String collectionAddress = "Doctors/";
    private void getAllNotifications() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("LOCAL_AUTH",MODE_PRIVATE);
        String role = sharedPreferences.getString("role","doctor");
        if (role.equals("doctor")) {
            collectionAddress = "Doctors/";
        } else {
            collectionAddress = "Patients/";
        }


        firebaseFirestore.collection(collectionAddress+ FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/notifications").orderBy("currentTime", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        if (!task.getResult().isEmpty()) {
                            for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                Log.d(TAG, "onComplete: " + "Model Added");
                                try {
                                    NotificationModel notificationModel = new NotificationModel(
                                            doc.getDocument().getString("docId"),
                                            doc.getDocument().getString("msg"),
                                            doc.getDocument().getString("sender"),
                                            doc.getDocument().getLong("notificationType")
                                    ).withId(doc.getDocument().getId());
                                    notificationList.add(notificationModel);
                                } catch (NullPointerException e) {
                                    Log.e(TAG, "onComplete: " + e.getMessage() );
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }

    public void checkIfUserExists(CheckUserRole checkUserRole, String userPhoneNumber) {
        Log.d(TAG, "checkIfUserExists: " + userPhoneNumber);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors").document(userPhoneNumber).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Log.d(TAG, "onComplete: getdoctor doc" );
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: " + "task successful");
                            if (Objects.requireNonNull(task.getResult()).exists()) {
                                DoctorModel doctorModel = task.getResult().toObject(DoctorModel.class);
                                checkUserRole.isDoctor();
                            } else {
                                firebaseFirestore.collection("Patients").document(userPhoneNumber).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    PatientModel patientModel = task.getResult().toObject(PatientModel.class);
                                                    if (patientModel == null) {
                                                        Log.d(TAG, "onComplete: " + "patient null");
                                                        checkUserRole.onFailure();
                                                    } else {
                                                        Log.d(TAG, "onComplete: " + "patient exist");
                                                        checkUserRole.isPatient();
                                                    }
                                                } else {
                                                    Log.d(TAG, "exception : " + task.getException().getMessage());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d(TAG, "exception: " + task.getException().getMessage() );
                        }
                    }
                });

    }

}