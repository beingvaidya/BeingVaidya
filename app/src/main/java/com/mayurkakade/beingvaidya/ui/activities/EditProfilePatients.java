package com.mayurkakade.beingvaidya.ui.activities;

import static com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor.MY_CAMERA_PERMISSION_CODE;

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
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.github.barteksc.pdfviewer.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.UploadToStorageInterface;
import com.mayurkakade.beingvaidya.data.models.PatientModel;
import com.mayurkakade.beingvaidya.ui.fragments.auth.MainViewModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfilePatients extends AppCompatActivity {
    public static final String TAG = "EditProfilePatients";
    public static final int CAMERA_REQUEST_PROFILE = 108;
    public static final int STORAGE_REQUEST_PROFILE = 109;
    final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private EditText tv_name, tv_phone_no, tv_mail, tv_age,tv_address;
    private ImageView iv_name_edit, iv_phone_no_edit, iv_mail_edit, iv_age_edit,iv_edit_address;
    private CircleImageView civ_profile;
    private Uri profile_uri = null;
    private MainViewModel mViewModel;
    private UploadToStorageInterface uploadToStorageInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_patient_profile);
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
            public void onSuccess(Uri downloadUri, String field) {

                Map<String, Object> params = new HashMap<>();
                params.put("profile_url", downloadUri.toString());
                firebaseFirestore.collection("Patients").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
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

        initViews();

        setEditors();

        civ_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        getPatientsData(this);
    }

    private void getPatientsData(Context context) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore.collection("Patients").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null) {
                                    PatientModel patientModel = task.getResult().toObject(PatientModel.class);
                                    if (patientModel != null) {
                                        tv_name.setText(patientModel.getName());

                                        tv_phone_no.setText(patientModel.getPhone_no());
                                        tv_mail.setText(patientModel.getEmail());
                                        tv_age.setText(""+patientModel.getAge());
                                        tv_address.setText(""+patientModel.getAddress());

                                        if (patientModel.getProfile_url() != null) {
                                            Glide.with(EditProfilePatients.this).load(patientModel.getProfile_url()).into(civ_profile);
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


    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose your profile picture");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(EditProfilePatients.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(EditProfilePatients.this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
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

    private void setTextWatchUpdater() {

        tv_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setName(s.toString());
            }
        });

        tv_mail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setEmail(s.toString());
            }
        });

        tv_age.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setAge(Integer.parseInt(s.toString()));
            }
        });
        tv_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setAddress(s.toString());
            }
        });

        tv_phone_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setPhoneNumber(s.toString());
            }
        });
    }


    private void setName(String s) {

        Map<String, Object> params = new HashMap<>();
        params.put("name", s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Patients").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        }, 1000);
    }

    private void setQualification(String s) {

        Map<String, Object> params = new HashMap<>();
        params.put("qualification", s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Patients").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        }, 1000);

    }

    private void setUniversity(String s) {

        Map<String, Object> params = new HashMap<>();
        params.put("university", s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Patients").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        }, 1000);

    }

    private void setPhoneNumber(String s) {

        Map<String, Object> params = new HashMap<>();
        params.put("phone_no", s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Patients").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        }, 1000);

    }

    private void setEmail(String s) {

        Map<String, Object> params = new HashMap<>();
        params.put("email", s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Patients").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        }, 1000);

    }

    private void setAge(int s) {

        Map<String, Object> params = new HashMap<>();
        params.put("age", s);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Patients").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        }, 1000);

    }


    private void setAddress(String s) {

        Map<String, Object> params = new HashMap<>();
        params.put("address", s);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Patients").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        }, 1000);

    }


    private void setPincode(String s) {

        Map<String, Object> params = new HashMap<>();
        params.put("pincode", s.toString());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseFirestore.collection("Patients").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        }, 1000);

    }


    private void initViews() {
        civ_profile = findViewById(R.id.profile_image);
        tv_name = findViewById(R.id.et_name);
        tv_phone_no = findViewById(R.id.et_phone_no);
        tv_mail = findViewById(R.id.et_mail);
        tv_age = findViewById(R.id.et_age);
        tv_address = findViewById(R.id.et_address);
        iv_name_edit = findViewById(R.id.iv_edit_name);
        iv_phone_no_edit = findViewById(R.id.iv_edit_phone_number);
        iv_mail_edit = findViewById(R.id.iv_edit_email);
        iv_age_edit = findViewById(R.id.iv_edit_age);
        iv_edit_address = findViewById(R.id.iv_edit_address);

    }

    private void setEditors() {
        iv_name_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv_name.isEnabled()) {
                    if (!TextUtils.isEmpty(tv_name.getText())) {
                        setName(tv_name.getText().toString());
                    } else {
                        Toast.makeText(EditProfilePatients.this, "name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                }
                toggleEditText(tv_name, iv_name_edit);
            }
        });


        iv_phone_no_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditText(tv_phone_no, iv_phone_no_edit);
            }
        });

        iv_mail_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditText(tv_mail, iv_mail_edit);
            }
        });
        iv_age_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv_age.isEnabled()) {
                    if (!TextUtils.isEmpty(tv_age.getText())) {
                        setAge(Integer.parseInt(tv_age.getText().toString()));
                    } else {
                        Toast.makeText(EditProfilePatients.this, "age cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                }

                toggleEditText(tv_age, iv_age_edit);
            }
        });

        iv_edit_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv_address.isEnabled()) {
                    if (!TextUtils.isEmpty(tv_address.getText())) {
                        setAddress(tv_address.getText().toString());
                    } else {
                        Toast.makeText(EditProfilePatients.this, "Address cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                }

                toggleEditText(tv_address, iv_edit_address);
            }
        });

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void toggleEditText(EditText editText, ImageView iv_drawable) {
        if (editText.isEnabled()) {
            editText.setEnabled(false);
            if (iv_drawable != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    iv_drawable.setImageDrawable(this.getDrawable(R.drawable.ic_baseline_edit_24));
                } else {
                    iv_drawable.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_edit_24));
                }
            }
        } else {
            editText.setEnabled(true);
            if (iv_drawable != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    iv_drawable.setImageDrawable(this.getDrawable(R.drawable.ic_baseline_done_24));
                } else {
                    iv_drawable.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_done_24));
                }
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_PROFILE) {
            Bitmap bitmap = null;
            if (data.getData() == null) {
                bitmap = (Bitmap) data.getExtras().get("data");
            } else {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            profile_uri = getImageUri(this, bitmap);
            mViewModel.uploadProfileImage(profile_uri, uploadToStorageInterface, "profiles", this);
        } else if (requestCode == STORAGE_REQUEST_PROFILE) {
            Bitmap bitmap = null;
            if (data.getData() == null) {
                bitmap = (Bitmap) data.getExtras().get("data");
            } else {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            profile_uri = getImageUri(this, bitmap);
            mViewModel.uploadProfileImage(profile_uri, uploadToStorageInterface, "profiles", this);
        }
        Glide.with(this).load(profile_uri.toString()).into(civ_profile);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, String.valueOf(new Date().getTime()), null);
        return Uri.parse(path);
    }
}