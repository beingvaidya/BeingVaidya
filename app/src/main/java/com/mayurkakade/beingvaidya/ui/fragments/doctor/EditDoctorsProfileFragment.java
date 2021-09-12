package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.UploadToStorageInterface;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;
import com.mayurkakade.beingvaidya.ui.fragments.auth.MainViewModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor.MY_CAMERA_PERMISSION_CODE;


public class EditDoctorsProfileFragment extends Fragment {

    public static final String TAG = "DocProfileFrag";

    private EditText tv_name,tv_qualification,tv_phone_no,tv_mail,tv_university,tv_pincode,et_availablity,et_bio,et_awards_and_honors,et_link_awards_and_honors
            ,et_publication,et_link_publication,et_presentation,et_link_presentation,et_course,et_link_course;

    private ImageView iv_name_edit,iv_qualification_edit,iv_phone_no_edit,iv_mail_edit,iv_university_edit,iv_pincode_edit,iv_availability_edit
            ,iv_bio_edit,iv_awards_and_honors_edit,iv_publication_edit,iv_presentation_edit,iv_course_edit;

    private Spinner tv_degree;
    private CircleImageView civ_profile;
    private Uri profile_uri = null;

    private MainViewModel mViewModel;
    private UploadToStorageInterface uploadToStorageInterface;

    public static final int CAMERA_REQUEST_PROFILE = 108;
    public static final int STORAGE_REQUEST_PROFILE = 109;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_doctors_profile, container, false);;
        initViews(view);

        setEditors();

        civ_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        getDoctorData(container.getContext());
        setAvailability();
        getAvailability();
        //setTextWatchUpdater();

        return view;
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose your profile picture");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requireActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                    } else {
                        if (options[item].equals("Take Photo")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, CAMERA_REQUEST_PROFILE);
                        } else if (options[item].equals("Choose from Gallery")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, STORAGE_REQUEST_PROFILE);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                }
            }
        });
        builder.show();
    }
    final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private void setTextWatchUpdater() {

        tv_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                setName(s.toString());
            }
        });

        tv_mail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                setEmail(s.toString());
            }
        });

        tv_degree.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("degree", tv_degree.getSelectedItem().toString());
                    if (!tv_degree.getSelectedItem().toString().equals("Select Degree")) {
                        firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: success");
                                } else {
                                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                                }
                            }
                        });
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        tv_phone_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                setPhoneNumber(s.toString());
            }
        });

        tv_pincode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                setPincode(s.toString());
            }
        });

        tv_qualification.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                setQualification(s.toString());
            }
        });

        tv_university.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                setUniversity(s.toString());
            }
        });

    }

    private void getAvailability() {
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors/"+ Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()+"/availability")
                .document("availability").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        et_availablity.setText(task.getResult().getString("availability"));
                    }
                } else {
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }

    private void setName(String s) {

        Map<String, Object> params = new HashMap<>();
        params.put("name",s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }
        },1000);
    }

    private void setQualification(String s) {

        Map<String, Object> params = new HashMap<>();
        params.put("qualification",s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }
        },1000);

    }

    private void setUniversity(String s) {

        Map<String, Object> params = new HashMap<>();
        params.put("university",s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }
        },1000);

    }

    private void setPhoneNumber(String s) {

        Map<String, Object> params = new HashMap<>();
        params.put("phone_no",s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }
        },1000);

    }

    private void setEmail(String s) {

        Map<String, Object> params = new HashMap<>();
        params.put("email",s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }
        },1000);

    }

    private void setPincode(String s) {

        Map<String, Object> params = new HashMap<>();
        params.put("pincode",s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }
        },1000);

    }

    private void setAvailability() {
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        et_availablity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                Map<String, Object> params = new HashMap<>();
                params.put("availability",s.toString());

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        firebaseFirestore.collection("Doctors/"+ Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()+"/availability")
                                .document("availability").set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: success");
                                } else {
                                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                                }
                            }
                        });
                    }
                },1000);

            }
        });
    }

    private void setBio(String s) {
        Map<String, Object> params = new HashMap<>();
        params.put("bio",s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }
        },1000);
    }

    private void setAwardsAndHonors(String s) {
        Map<String, Object> params = new HashMap<>();
        params.put("awards_and_honors",s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }
        },1000);
    }

    private void setAwardsAndHonorsLink(String s) {
        Map<String, Object> params = new HashMap<>();
        params.put("awards_and_honors_link",s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }
        },1000);
    }

    private void setPublication(String s) {
        Map<String, Object> params = new HashMap<>();
        params.put("publication",s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }
        },1000);
    }

    private void setPublicationLink(String s) {
        Map<String, Object> params = new HashMap<>();
        params.put("publication_link",s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }
        },1000);
    }

    private void setPresentation(String s) {
        Map<String, Object> params = new HashMap<>();
        params.put("presentation",s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }
        },1000);
    }

    private void setPresentationLink(String s) {
        Map<String, Object> params = new HashMap<>();
        params.put("presentation_link",s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }
        },1000);
    }

    private void setCourse(String s) {
        Map<String, Object> params = new HashMap<>();
        params.put("course",s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }
        },1000);
    }

    private void setCourseLink(String s) {
        Map<String, Object> params = new HashMap<>();
        params.put("course_link",s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }
        },1000);
    }




    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        uploadToStorageInterface = new UploadToStorageInterface() {
            @Override
            public void onStart() {

            }

            @Override
            public void onProgress(int progress) {
            }

            @Override
            public void onSuccess(Uri downloadUri,String field) {

                Map<String, Object> params = new HashMap<>();
                params.put("profile_url",downloadUri.toString());
                firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
            }

            @Override
            public void onFailure() {

            }
        };


    }

    private void getDoctorData(Context context) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore.collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())).get()
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
                                        tv_university.setText(doctorModel.getUniversity());
                                        tv_pincode.setText(doctorModel.getPincode());
                                        if (doctorModel.getBio() != null) {
                                            et_bio.setText(doctorModel.getBio());
                                        }
                                        if (doctorModel.getAwards_and_honors() != null) {
                                            et_awards_and_honors.setText(doctorModel.getAwards_and_honors());
                                        }
                                        if (doctorModel.getAwards_and_honors_link() != null) {
                                            et_link_awards_and_honors.setText(doctorModel.getAwards_and_honors_link());
                                        }
                                        if (doctorModel.getPublication() != null) {
                                            et_publication.setText(doctorModel.getPublication());
                                        }
                                        if (doctorModel.getPublication_link() != null) {
                                            et_link_publication.setText(doctorModel.getPublication_link());
                                        }
                                        if (doctorModel.getPresentation() != null) {
                                            et_presentation.setText(doctorModel.getPresentation());
                                        }
                                        if (doctorModel.getPresentation_link() != null) {
                                            et_link_presentation.setText(doctorModel.getPresentation_link());
                                        }
                                        if (doctorModel.getCourse() != null) {
                                            et_course.setText(doctorModel.getCourse());
                                        }
                                        if (doctorModel.getCourse_link() != null) {
                                            et_link_course.setText(doctorModel.getCourse_link());
                                        }

                                        if (doctorModel.getDegree().equals("MBBS")) {
                                            tv_degree.setSelection(1);
                                        } else if (doctorModel.getDegree().equals("BAMS")) {
                                            tv_degree.setSelection(2);
                                        } else if (doctorModel.getDegree().equals("BDS")) {
                                            tv_degree.setSelection(3);
                                        } else if (doctorModel.getDegree().equals("BUMS")) {
                                            tv_degree.setSelection(4);
                                        } else if (doctorModel.getDegree().equals("BNYS")) {
                                            tv_degree.setSelection(5);
                                        } else if (doctorModel.getDegree().equals("MD/MS")) {
                                            tv_degree.setSelection(6);
                                        }
//                                        tv_degree.setText(doctorModel.getDegree());

                                        if (doctorModel.getProfile_url() != null) {
                                            Glide.with(requireContext()).load(doctorModel.getProfile_url()).into(civ_profile);
                                        }



                                    } else {
                                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } else {
                                Log.d(TAG, "onComplete: unsuccessful" + Objects.requireNonNull(task.getException()).getMessage());
                                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }
    }

    private void initViews(View view) {
        civ_profile = view.findViewById(R.id.profile_image);
        tv_name = view.findViewById(R.id.et_name);
        tv_qualification = view.findViewById(R.id.et_qualification);
        tv_phone_no = view.findViewById(R.id.et_phone_no);
        tv_mail = view.findViewById(R.id.et_mail);
        tv_university = view.findViewById(R.id.et_university);
        tv_degree = view.findViewById(R.id.et_degree);
        tv_pincode = view.findViewById(R.id.et_pincode);
        et_availablity = view.findViewById(R.id.et_availablity);

        et_bio = view.findViewById(R.id.et_bio);
        et_awards_and_honors = view.findViewById(R.id.et_awards_and_honors);
        et_link_awards_and_honors = view.findViewById(R.id.et_link_awards_and_honors);
        et_publication = view.findViewById(R.id.et_publication);
        et_link_publication = view.findViewById(R.id.et_link_publication);
        et_presentation = view.findViewById(R.id.et_presentation);
        et_link_presentation = view.findViewById(R.id.et_link_presentation);
        et_course = view.findViewById(R.id.et_course);
        et_link_course = view.findViewById(R.id.et_link_course);

        iv_name_edit = view.findViewById(R.id.iv_edit_name);
        iv_qualification_edit = view.findViewById(R.id.iv_edit_qualification);
        iv_phone_no_edit = view.findViewById(R.id.iv_edit_phone_number);
        iv_mail_edit = view.findViewById(R.id.iv_edit_email);
        iv_university_edit = view.findViewById(R.id.iv_edit_university);
        iv_pincode_edit = view.findViewById(R.id.iv_edit_pincode);
        iv_availability_edit = view.findViewById(R.id.iv_edit_availability);

        iv_bio_edit = view.findViewById(R.id.iv_edit_bio);
        iv_awards_and_honors_edit = view.findViewById(R.id.iv_edit_awards_and_honors);
        iv_publication_edit = view.findViewById(R.id.iv_edit_publication);
        iv_presentation_edit = view.findViewById(R.id.iv_edit_presentation);
        iv_course_edit = view.findViewById(R.id.iv_edit_course);

//        et_bio,et_awards_and_honors,et_link_awards_and_honors
//                ,et_publication,et_link_publication,et_presentation,et_link_presentation,et_course,et_link_course

    }

    private void setEditors() {
        iv_name_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv_name.isEnabled()) {
                    if (!TextUtils.isEmpty(tv_name.getText())) {
                        setName(tv_name.getText().toString());
                    } else {
                        Toast.makeText(requireContext(), "name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                }
                toggleEditText(tv_name,iv_name_edit);
            }
        });

        iv_qualification_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditText(tv_qualification,iv_qualification_edit);
            }
        });

        iv_phone_no_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditText(tv_phone_no,iv_phone_no_edit);
            }
        });

        iv_mail_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditText(tv_mail,iv_mail_edit);
            }
        });

        iv_university_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditText(tv_university,iv_university_edit);
            }
        });

        iv_pincode_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditText(tv_pincode,iv_pincode_edit);
            }
        });

        iv_availability_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditText(et_availablity,iv_availability_edit);
            }
        });

        iv_bio_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_bio.isEnabled()) {
                    if (!TextUtils.isEmpty(et_bio.getText())) {
                        setBio(et_bio.getText().toString());
                    } else {
                        Toast.makeText(requireContext(), "bio cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                }
                toggleEditText(et_bio,iv_bio_edit);
            }
        });

        iv_awards_and_honors_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (et_awards_and_honors.isEnabled()) {
                    if (!TextUtils.isEmpty(et_awards_and_honors.getText())) {
                        setAwardsAndHonors(et_awards_and_honors.getText().toString());
                    } else {
                        Toast.makeText(requireContext(), "bio cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                }

                if (et_link_awards_and_honors.isEnabled()) {
                    if (!TextUtils.isEmpty(et_link_awards_and_honors.getText())) {
                        setAwardsAndHonorsLink(et_link_awards_and_honors.getText().toString());
                    } else {
                        Toast.makeText(requireContext(), "bio cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                }

                toggleEditText(et_awards_and_honors,iv_awards_and_honors_edit);
                toggleEditText(et_link_awards_and_honors,null);
            }
        });

        iv_publication_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (et_publication.isEnabled()) {
                    if (!TextUtils.isEmpty(et_publication.getText())) {
                        setPublication(et_publication.getText().toString());
                    } else {
                        Toast.makeText(requireContext(), "bio cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                }

                if (et_link_publication.isEnabled()) {
                    if (!TextUtils.isEmpty(et_link_publication.getText())) {
                        setPublicationLink(et_link_publication.getText().toString());
                    } else {
                        Toast.makeText(requireContext(), "bio cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                }

                toggleEditText(et_publication,iv_publication_edit);
                toggleEditText(et_link_publication,null);
            }
        });

        iv_presentation_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (et_presentation.isEnabled()) {
                    if (!TextUtils.isEmpty(et_presentation.getText())) {
                        setPresentation(et_presentation.getText().toString());
                    } else {
                        Toast.makeText(requireContext(), "bio cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                }

                if (et_link_presentation.isEnabled()) {
                    if (!TextUtils.isEmpty(et_link_presentation.getText())) {
                        setPresentationLink(et_link_presentation.getText().toString());
                    } else {
                        Toast.makeText(requireContext(), "bio cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                }

                toggleEditText(et_presentation,iv_presentation_edit);
                toggleEditText(et_link_presentation,null);
            }
        });

        iv_course_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (et_course.isEnabled()) {
                    if (!TextUtils.isEmpty(et_course.getText())) {
                        setCourse(et_course.getText().toString());
                    } else {
                        Toast.makeText(requireContext(), "bio cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                }

                if (et_link_course.isEnabled()) {
                    if (!TextUtils.isEmpty(et_link_course.getText())) {
                        setCourseLink(et_link_course.getText().toString());
                    } else {
                        Toast.makeText(requireContext(), "bio cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                }

                toggleEditText(et_course,iv_course_edit);
                toggleEditText(et_link_course,null);
            }
        });

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void toggleEditText(EditText editText, ImageView iv_drawable) {
        if (editText.isEnabled()) {
            editText.setEnabled(false);
            if (iv_drawable != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    iv_drawable.setImageDrawable(requireContext().getDrawable(R.drawable.ic_baseline_edit_24));
                } else {
                    iv_drawable.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_edit_24));
                }
            }
        } else {
            editText.setEnabled(true);
            if (iv_drawable != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    iv_drawable.setImageDrawable(requireContext().getDrawable(R.drawable.ic_baseline_done_24));
                } else {
                    iv_drawable.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_done_24));
                }
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_PROFILE) {
            Bitmap bitmap = null;
            if(data.getData()==null){
                bitmap = (Bitmap)data.getExtras().get("data");
            }else{
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            profile_uri = getImageUri(requireContext(),bitmap);
            mViewModel.uploadProfileImage(profile_uri, uploadToStorageInterface,"profiles", requireContext());
        } else if(requestCode == STORAGE_REQUEST_PROFILE) {
            Bitmap bitmap = null;
            if(data.getData()==null){
                bitmap = (Bitmap)data.getExtras().get("data");
            }else{
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            profile_uri = getImageUri(requireContext(),bitmap);
            mViewModel.uploadProfileImage(profile_uri, uploadToStorageInterface,"profiles", requireContext());
        }
        Glide.with(requireContext()).load(profile_uri.toString()).into(civ_profile);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, String.valueOf(new Date().getTime()), null);
        return Uri.parse(path);
    }
}