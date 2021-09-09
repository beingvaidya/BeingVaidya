package com.mayurkakade.beingvaidya.ui.activities;

import android.content.ActivityNotFoundException;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;

import java.util.Objects;

public class ActivityMyProfilePatients extends AppCompatActivity {
    public static final String TAG = "MyDoctorDebug";
    private TextView tv_name, tv_qualification, tv_phone_no, tv_mail, tv_availability, tv_review_date, tv_remaining_time, tv_awards_and_honors, tv_publication, tv_presentation, tv_course, tv_bio;
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
        tv_qualification = findViewById(R.id.tv_qualification);
        tv_phone_no = findViewById(R.id.tv_phone_no);
        tv_mail = findViewById(R.id.tv_mail);
        tv_availability = findViewById(R.id.tv_availability);
        tv_review_date = findViewById(R.id.tv_review_date);
        tv_remaining_time = findViewById(R.id.tv_remaining_time);
        bt_whatsapp_doctor = findViewById(R.id.bt_whatsapp_doctor);
        iv_profile = findViewById(R.id.profile_image);
        bt_edit = findViewById(R.id.bt_edit_profile);

        tv_awards_and_honors = findViewById(R.id.tv_awards_and_honors);
        tv_publication = findViewById(R.id.tv_publication);
        tv_presentation = findViewById(R.id.tv_presentation);
        tv_course = findViewById(R.id.tv_course);
        tv_bio = findViewById(R.id.tv_bio);
        rootLayout = findViewById(R.id.rootLayout);
        progressLoader = findViewById(R.id.progress_loader);


        String doc_id = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        bt_edit.setVisibility(View.GONE);
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
                                DoctorModel doctorModel = task.getResult().toObject(DoctorModel.class);
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
                                    DoctorModel doctorModel = task.getResult().toObject(DoctorModel.class);
                                    if (doctorModel != null) {
                                        tv_name.setText(doctorModel.getName());
                                        tv_qualification.setText(doctorModel.getQualification());
                                        tv_phone_no.setText(doctorModel.getPhone_no());
                                        tv_mail.setText(doctorModel.getEmail());

                                        setFieldWithNullCheck(tv_bio, doctorModel.getBio(), null);
                                        setFieldWithNullCheck(tv_presentation, doctorModel.getPresentation(), doctorModel.getPresentation_link());
                                        setFieldWithNullCheck(tv_awards_and_honors, doctorModel.getAwards_and_honors(), doctorModel.getAwards_and_honors_link());
                                        setFieldWithNullCheck(tv_publication, doctorModel.getPublication(), doctorModel.getPublication_link());
                                        setFieldWithNullCheck(tv_course, doctorModel.getCourse(), doctorModel.getCourse_link());

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

    private void setFieldWithNullCheck(TextView textView, String text, String link) {
        if (text != null) {
            textView.setText(text);
        } else {
            textView.setVisibility(View.GONE);
        }
        if (link != null) {
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage("com.android.chrome");
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        // Chrome browser presumably not installed so allow user to choose instead
                        Toast.makeText(ActivityMyProfilePatients.this, "Browser not found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


}