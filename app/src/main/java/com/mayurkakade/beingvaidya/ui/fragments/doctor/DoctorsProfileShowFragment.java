package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;


import java.util.Date;
import java.util.Objects;

public class DoctorsProfileShowFragment extends Fragment {

    private TextView tv_name,tv_qualification,tv_phone_no,tv_mail,tv_availability,tv_review_date,tv_remaining_time,tv_awards_and_honors,tv_publication,tv_presentation,tv_course,tv_bio;
    private CardView bt_whatsapp_doctor;
    public static final String TAG = "MyDoctorDebug";
    private ImageView iv_profile;
    private Button bt_edit;
    private ConstraintLayout rootLayout;
    private ProgressBar progressLoader;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctors_profile_show, container, false);
        initViews(view);

        String doc_id = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        if (getArguments()!=null) {
            if (getArguments().getString("doc_id") != null) {
                if (getArguments().getString("from") != null) {
                    if (getArguments().getString("from").equals("self")) {
                        bt_edit.setVisibility(View.VISIBLE);
                    }
                }
                getDoctorData(container.getContext(),getArguments().getString("doc_id"));
            } else {
                getDoctorData(container.getContext(),doc_id);

            }
        }

        bt_whatsapp_doctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whatsappMyDoctor();
            }
        });

        bt_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController((Activity)requireContext(), R.id.doctors_nav_host);
                navController.navigate(R.id.action_doctorsProfileShowFragment_to_doctorsProfileFragment);
            }
        });

        return view;
    }

    private void getDoctorProfileImage(String doctor_id){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors").document(doctor_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                DoctorModel doctorModel = task.getResult().toObject(DoctorModel.class);
                                if (doctorModel!= null) {
                                    if (doctorModel.getPhone_no() != null) {
                                        if (!doctorModel.getPhone_no().equals("") || !doctorModel.getPhone_no().equals("no_profile") ) {
                                            Log.d(TAG + "profile", "onComplete: " + doctorModel.getProfile_url());
                                            Glide.with(requireContext()).load(doctorModel.getProfile_url()).into(iv_profile);
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

        firebaseFirestore.collection("Doctors/"+doc_id+"/availability").document("availability").get()
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
            firebaseFirestore.collection("Doctors").document(doc_id).get()
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

                                        setFieldWithNullCheck(tv_bio,doctorModel.getBio(),null);
                                        setFieldWithNullCheck(tv_presentation,doctorModel.getPresentation(),doctorModel.getPresentation_link());
                                        setFieldWithNullCheck(tv_awards_and_honors,doctorModel.getAwards_and_honors(), doctorModel.getAwards_and_honors_link());
                                        setFieldWithNullCheck(tv_publication,doctorModel.getPublication(), doctorModel.getPublication_link());
                                        setFieldWithNullCheck(tv_course,doctorModel.getCourse(), doctorModel.getCourse_link());

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
        if (text!=null) {
            textView.setText(text);
        } else {
            textView.setVisibility(View.GONE);
        }
        if (link != null) {
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(link));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage("com.android.chrome");
                    try {
                        requireContext().startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        // Chrome browser presumably not installed so allow user to choose instead
                        Toast.makeText(requireContext(), "Browser not found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void initViews(View view) {
        tv_name = view.findViewById(R.id.tv_name);
        tv_qualification = view.findViewById(R.id.tv_qualification);
        tv_phone_no = view.findViewById(R.id.tv_phone_no);
        tv_mail = view.findViewById(R.id.tv_mail);
        tv_availability = view.findViewById(R.id.tv_availability);
        tv_review_date = view.findViewById(R.id.tv_review_date);
        tv_remaining_time = view.findViewById(R.id.tv_remaining_time);
        bt_whatsapp_doctor = view.findViewById(R.id.bt_whatsapp_doctor);
        iv_profile = view.findViewById(R.id.profile_image);
        bt_edit = view.findViewById(R.id.bt_edit_profile);

        tv_awards_and_honors = view.findViewById(R.id.tv_awards_and_honors);
        tv_publication = view.findViewById(R.id.tv_publication);
        tv_presentation = view.findViewById(R.id.tv_presentation);
        tv_course = view.findViewById(R.id.tv_course);
        tv_bio = view.findViewById(R.id.tv_bio);
        rootLayout = view.findViewById(R.id.rootLayout);
        progressLoader = view.findViewById(R.id.progress_loader);
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
                        onClickWhatsApp("Hello Doctor, My name is "+ task.getResult().getString("name") + ". My Age is "+ task.getResult().getLong("age") , doctorPhoneNo);
                    }
                } else {
                    Log.d("ActivityPatient", "onComplete: " + task.getException().getMessage());
                }
            }
        });

    }


    public void onClickWhatsApp(String message, String number) {
        Uri uri = Uri.parse("smsto:" + number);
        PackageManager pm=requireContext().getPackageManager();
        try {
            Intent waIntent = new Intent(Intent.ACTION_SENDTO,uri);
            PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            waIntent.setPackage("com.whatsapp");
            waIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(waIntent, "Share With Doctor"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(requireContext(), "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show();
        }

    }
}