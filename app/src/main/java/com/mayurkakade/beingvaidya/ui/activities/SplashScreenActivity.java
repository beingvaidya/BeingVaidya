package com.mayurkakade.beingvaidya.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.Repository;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;
import com.mayurkakade.beingvaidya.data.models.PatientModel;

import java.lang.reflect.Method;
import java.util.Objects;

public class SplashScreenActivity extends AppCompatActivity {

    public static final String TAG = "ActivityAuthentication";

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPreferences = getSharedPreferences("OnBoarding",MODE_PRIVATE);
        boolean isIntroShown = sharedPreferences.getBoolean("isIntroShown", false);

        if (isIntroShown) {
            if (Build.VERSION.SDK_INT >= 24) {
                try {
                    Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                    m.invoke(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            FirebaseFirestore.getInstance().setFirestoreSettings(settings);

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                new Handler(getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loginUser(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(), SplashScreenActivity.this);
                    }
                }, 3000);

            } else {

                new Handler(getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashScreenActivity.this, ActivityAuthentication.class));
                        finish();
                    }
                }, 3000);

            }
        } else {
            startActivity(new Intent(SplashScreenActivity.this, WelcomeActivity.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }


    public void loginUser(String userPhoneNumber, AppCompatActivity activity) {
        Log.d(TAG, "onComplete: " + "login USer");
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
                                if (doctorModel != null) {
                                    Log.d(TAG, "onComplete: " + "doctor exist");
                                    Repository.addDoctorToSharedPrefs(doctorModel,activity);
                                    Intent intent = new Intent(SplashScreenActivity.this, ActivityDoctor.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } else {
                                    Log.d(TAG, "onComplete: null doctor model"  );
                                }

                            } else {
                                firebaseFirestore.collection("Patients").document(userPhoneNumber).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "onComplete: " + "patient exist");
                                                    PatientModel patientModel = task.getResult().toObject(PatientModel.class);

                                                    if (patientModel == null) {
                                                        Toast.makeText(activity, "User does not exist in our database !", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(SplashScreenActivity.this, ActivityAuthentication.class));
                                                        finish();
                                                    } else {
                                                        Repository.addPatientToSharedPrefs(patientModel,activity);
                                                        Intent intent = new Intent(SplashScreenActivity.this, ActivityPatient.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
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

        // Update UI

    }
}