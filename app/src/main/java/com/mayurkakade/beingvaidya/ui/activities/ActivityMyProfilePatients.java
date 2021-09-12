package com.mayurkakade.beingvaidya.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.PatientModel;

import java.util.Objects;

public class ActivityMyProfilePatients extends AppCompatActivity {
    public static final String TAG = "MyDoctorDebug";
    private TextView tv_name, tv_address, tv_phone_no, tv_phone_no_doctor, tv_mail, tv_availability, tv_age, tv_remaining_time, tv_awards_and_honors, tv_publication, tv_presentation, tv_course, tv_bio;
    private CardView bt_whatsapp_doctor;
    private ImageView iv_profile;
    private Button bt_edit;
    private ConstraintLayout rootLayout;
    private ProgressBar progressLoader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile_show);
        tv_name = findViewById(R.id.tv_name);
        tv_address = findViewById(R.id.tv_address);
        tv_phone_no = findViewById(R.id.tv_phone_no);
        tv_phone_no_doctor = findViewById(R.id.tv_phone_no_doctor);
        tv_mail = findViewById(R.id.tv_mail);
        tv_availability = findViewById(R.id.tv_availability);
        tv_age = findViewById(R.id.tv_age);
        tv_remaining_time = findViewById(R.id.tv_remaining_time);
        bt_whatsapp_doctor = findViewById(R.id.bt_whatsapp_doctor);
        iv_profile = findViewById(R.id.profile_image);
        bt_edit = findViewById(R.id.bt_edit_profile);

        rootLayout = findViewById(R.id.rootLayout);
        progressLoader = findViewById(R.id.progress_loader);


        String doc_id = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        getDoctorData(this, doc_id);
        bt_whatsapp_doctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whatsappMyDoctor();
            }
        });

        bt_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMyProfile = new Intent(ActivityMyProfilePatients.this, EditProfilePatients.class);
                startActivity(intentMyProfile);

            }
        });


    }

    private void whatsappMyDoctor() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Patients").document(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String doctorPhoneNo = task.getResult().getString("doctor_unique_id");
                        onClickWhatsApp("Hello Doctor, My name is " + task.getResult().getString("name") + ". My Age is " + task.getResult().getLong("age"), doctorPhoneNo);
                    }
                } else {
                    Log.d("ActivityPatient", "onComplete: " + task.getException().getMessage());
                }
            }
        });

    }


    public void onClickWhatsApp(String message, String number) {
        Uri uri = Uri.parse("smsto:" + number);
        PackageManager pm = ActivityMyProfilePatients.this.getPackageManager();
        try {
            Intent waIntent = new Intent(Intent.ACTION_SENDTO, uri);
            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            waIntent.setPackage("com.whatsapp");
            waIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(waIntent, "Share With Doctor"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(ActivityMyProfilePatients.this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show();
        }

    }


    private void getDoctorProfileImage(String doctor_id) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Patients").document(doctor_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                PatientModel doctorModel = task.getResult().toObject(PatientModel.class);
                                if (doctorModel != null) {
                                    if (doctorModel.getPhone_no() != null) {
                                        if (!doctorModel.getPhone_no().equals("") || !doctorModel.getPhone_no().equals("no_profile")) {
                                            Log.d(TAG + "profile", "onComplete: " + doctorModel.getProfile_url());
                                            Glide.with(ActivityMyProfilePatients.this).load(doctorModel.getProfile_url()).into(iv_profile);
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void getDoctorData(Context context, String doc_id) {

        getDoctorProfileImage(doc_id);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("Patients/" + doc_id + "/availability").document("availability").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                if (task.getResult().exists()) {
                                    tv_availability.setText(task.getResult().getString("availability"));
                                }
                            }
                        } else {
                            Log.d(TAG, "onComplete: " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore.collection("Patients").document(doc_id).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null) {
                                    PatientModel doctorModel = task.getResult().toObject(PatientModel.class);
                                    if (doctorModel != null) {
                                        tv_name.setText(doctorModel.getName());
                                        tv_phone_no.setText(doctorModel.getPhone_no());
                                        getDoctorName(doctorModel.getDoctor_unique_id());
                                        tv_mail.setText(doctorModel.getEmail());
                                        tv_age.setText(""+doctorModel.getAge());
                                        tv_address.setText(doctorModel.getAddress());
                                        rootLayout.setVisibility(View.VISIBLE);
                                        progressLoader.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(context, "Something went wrong : " + doc_id, Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } else {
                                Log.d(TAG, "onComplete: unsuccessful" + task.getException().getMessage());
                                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }

    }

    private void getDoctorName(String doctor_id) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors").document(doctor_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                if (task.getResult().exists()) {
                                    tv_phone_no_doctor.setText(task.getResult().getString("name"));
                                }
                            }
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
    }
}