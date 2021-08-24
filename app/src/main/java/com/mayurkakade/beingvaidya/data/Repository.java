package com.mayurkakade.beingvaidya.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;
import com.mayurkakade.beingvaidya.data.models.PatientModel;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class Repository {

    public static final String TAG = "repository_tag";

    public static void addDoctor(DoctorModel doctorModel, OnQueryDataListener firebaseDataListener) {
        firebaseDataListener.onStart();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        Map<String, Object> params = new HashMap<>();
        params.put("name",doctorModel.getName());
        params.put("degree",doctorModel.getDegree());
        params.put("phone_no",doctorModel.getPhone_no());
        params.put("email",doctorModel.getEmail());
        params.put("qualification",doctorModel.getQualification());
        params.put("university",doctorModel.getUniversity());
        params.put("pincode",doctorModel.getPincode());
        if (doctorModel.getProfile_url() != null)
        params.put("profile_url",doctorModel.getProfile_url());

        firebaseFirestore.collection("Doctors").document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()) {
                            firebaseFirestore.collection("Doctors").document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                                    .set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        firebaseDataListener.onSuccess();
                                    } else {
                                        Log.d(TAG, "failed : " + task.getException().getMessage());
                                        firebaseDataListener.onFailure("failed : " + task.getException().getMessage());
                                    }
                                }
                            });
                        }else {
                            firebaseDataListener.onFailure("User Already Exist");
                        }
                    }
                });


    }

    public static void addPatient(PatientModel patientModel, OnQueryDataListener onQueryDataListener, String doctor_unique_id) {
        onQueryDataListener.onStart();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        WriteBatch batch = firebaseFirestore.batch();

        DocumentReference doctorDocumentReference = firebaseFirestore.collection("Doctors").document(doctor_unique_id);
        doctorDocumentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        Map<String, Object> patientData = new HashMap<>();
                        patientData.put("name",patientModel.getName());
                        patientData.put("phone_no",patientModel.getPhone_no());
                        patientData.put("email",patientModel.getEmail());
                        patientData.put("address",patientModel.getAddress());
                        patientData.put("doctor_unique_id",patientModel.getDoctor_unique_id());
                        patientData.put("age",patientModel.getAge());
                        patientData.put("profile_url",patientModel.getProfile_url());

                        CollectionReference patientCollectionReference = firebaseFirestore.collection("Doctors/" + doctor_unique_id + "/Patients");
                        CollectionReference patientDataCollectionReference = firebaseFirestore.collection("Patients");
                        DocumentReference patientDataDocument = patientDataCollectionReference.document(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()));

                        batch.set(patientDataDocument,patientData);

                        Map<String, Object> patientReferenceData = new HashMap<>();
                        patientReferenceData.put("name",patientModel.getName());
                        patientReferenceData.put("phone_no",patientModel.getPhone_no());
                        patientReferenceData.put("doc_id",doctor_unique_id);

                        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                        int currentYear = calendar.get(Calendar.YEAR);
                        int currentMonth = calendar.get(Calendar.MONTH) + 1;
                        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

                        patientReferenceData.put("currentYear",currentYear);
                        patientReferenceData.put("currentMonth",currentMonth);
                        patientReferenceData.put("currentDay",currentDay);

                        DocumentReference patientReference = patientCollectionReference.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()));
                        batch.set(patientReference,patientReferenceData);

                        batch.commit();

                        onQueryDataListener.onSuccess();
                    } else {
                        onQueryDataListener.onFailure("Wrong Doctor Id");
                    }
                } else {
                    onQueryDataListener.onFailure(task.getException().getMessage());
                }
            }
        });


    }

    public static void addPatientToSharedPrefs(PatientModel patientModel, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LOCAL_AUTH",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name",patientModel.getName());
        editor.putString("phone_no",patientModel.getPhone_no());
        editor.putString("email",patientModel.getEmail());
        editor.putString("address",patientModel.getAddress());
        editor.putString("doctor_unique_id",patientModel.getDoctor_unique_id());
        editor.putInt("age",patientModel.getAge());
        editor.putString("profile_url",patientModel.getProfile_url());
        editor.putString("role","patient");
        editor.apply();
    }

    public static void addDoctorToSharedPrefs(DoctorModel doctorModel, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LOCAL_AUTH",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name",doctorModel.getName());
        editor.putString("degree",doctorModel.getDegree());
        editor.putString("phone_no",doctorModel.getPhone_no());
        editor.putString("email",doctorModel.getEmail());
        editor.putString("qualification",doctorModel.getQualification());
        editor.putString("university",doctorModel.getUniversity());
        editor.putString("pincode",doctorModel.getPincode());
        editor.putString("profile_url",doctorModel.getProfile_url());
        editor.putString("role","doctor");
        editor.apply();
    }
}
