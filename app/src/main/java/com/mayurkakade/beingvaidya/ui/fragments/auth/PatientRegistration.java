package com.mayurkakade.beingvaidya.ui.fragments.auth;

import android.Manifest;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.Config;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.UploadToStorageInterface;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor.MY_CAMERA_PERMISSION_CODE;

public class PatientRegistration extends Fragment {

    private MainViewModel mViewModel;
    private View view;
    private EditText et_name,et_age,et_phone_no,et_email,et_address,et_doctors_unique_id;
    private String name,phone_no,email,address,doctor_unique_id;
    private int age;
    private Spinner sp_gender;
    private Button bt_submit;
    private Uri profile_uri = null;
    private CircleImageView civ_profile;
    private UploadToStorageInterface uploadToStorageInterface;
    public static final int CAMERA_REQUEST_PROFILE = 108;
    public static final int STORAGE_REQUEST_PROFILE = 109;
    public static final String COUNTRY_CODE = "+91";
    boolean canPatientBeAdded = false;
    public static final String TAG = "patientRegistra";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_registration, container, false);
        this.view = view;
        initViews(view);
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataFromViews();
            }
        });
        civ_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
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

    interface ReturnString {
        void onSuccess(String subscriptionId);
        void onFailure(String freeSubscriptionId);
    }
    
    private boolean isSubscribed( Date startDate, Date dateForChecking, Date endDate) {
        return dateForChecking.after(startDate) && dateForChecking.before(endDate) || startDate == dateForChecking || endDate == dateForChecking;
    }

    public void checkSubscriptionId ( String doctor_id, ReturnString returnString ) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors/"+doctor_id+"/myPlan").document("plan_name")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (Objects.requireNonNull(task.getResult()).exists()) {
                        returnString.onSuccess(task.getResult().getString("plan_name"));
                    } else {
                        returnString.onSuccess(Config.Subscriptions.freePlanSubscriptionId);
                    }
                } else {
                    returnString.onSuccess(Config.Subscriptions.freePlanSubscriptionId);
                }
            }
        });
    }

    private void getDataFromViews() {
        //name
        if (!TextUtils.isEmpty(et_name.getText().toString())) {
            name = et_name.getText().toString();
            //age
            if (!TextUtils.isEmpty(et_age.getText().toString())) {
                age = Integer.parseInt(et_age.getText().toString());
                //phone_no
                if (!TextUtils.isEmpty(et_phone_no.getText().toString())) {
                    phone_no = COUNTRY_CODE+et_phone_no.getText().toString();
                    //email
                    if (!TextUtils.isEmpty(et_email.getText().toString())) {
                        email = et_email.getText().toString();
                        //address
                        if (!TextUtils.isEmpty(et_address.getText().toString())) {
                            address = et_address.getText().toString();
                            //doctor id
                            if (!TextUtils.isEmpty(et_doctors_unique_id.getText().toString())) {
                                doctor_unique_id = "+91"+et_doctors_unique_id.getText().toString();

                                ReturnString returnString = new ReturnString() {
                                    @Override
                                    public void onSuccess(String subscriptionId) {
                                        Log.d(TAG, "1 : onSuccess: " + subscriptionId);
                                        int quota = 5;
                                        int validityInMonths = 1;
                                        switch (subscriptionId) {
                                            case Config.Subscriptions.freePlanSubscriptionId:
                                                quota = 5;
                                                validityInMonths = 1;
                                                break;

                                            case Config.Subscriptions.yearlyUnlimitedPlanSubscriptionId:
                                                quota = -1;
                                                validityInMonths = 12;
                                                break;

                                            case Config.Subscriptions.halfYearlyUnlimitedPlanSubscriptionId:
                                                quota = -1;
                                                validityInMonths = 6;
                                                break;

                                            case Config.Subscriptions.monthlyUnlimitedPlanSubscriptionId:
                                                quota = -1;
                                                validityInMonths = 1;
                                                break;

                                            case Config.Subscriptions.monthlyThirtyPlanSubscriptionId:
                                                quota = 30;
                                                validityInMonths = 1;
                                                break;

                                            case Config.Subscriptions.monthlyFifteenPlanSubscriptionId:
                                                quota = 15;
                                                validityInMonths = 1;
                                                break;
                                        }
                                        Log.d(TAG, "2. onSuccess: " + quota);
                                        Log.d(TAG, "3. onSuccess: " + validityInMonths);

                                        {
                                            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                                            int finalValidityInMonths = validityInMonths;
                                            int finalQuota = quota;
                                            firebaseFirestore.collection("Doctors/"+doctor_unique_id+"/myPlan").document("plan_name").get()
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                if (task.getResult().exists()) {
                                                                    Calendar purchaseCalendar = Calendar.getInstance();
                                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                                        purchaseCalendar.set(Math.toIntExact(task.getResult().getLong("purchaseYear")),Math.toIntExact(task.getResult().getLong("purchaseMonth") - 1),Math.toIntExact( task.getResult().getLong("purchaseDay")), 0,0);
                                                                    } else {
                                                                        purchaseCalendar.set(task.getResult().getLong("purchaseYear").intValue(),(task.getResult().getLong("purchaseMonth").intValue() - 1),( task.getResult().getLong("purchaseDay").intValue()), 0,0);
                                                                    }
                                                                    Date purchaseDate = purchaseCalendar.getTime();
                                                                    purchaseCalendar.add(Calendar.MONTH, finalValidityInMonths);
                                                                    Date subEndTime = purchaseCalendar.getTime();
                                                                    final boolean isSubscribed = isSubscribed(purchaseDate, new Date(), subEndTime);
                                                                    if (isSubscribed) {
                                                                        Log.d(TAG, "4. onComplete: " + "isSubscribed is true");
                                                                        firebaseFirestore.collection("Doctors/" + doctor_unique_id + "/Patients").get()
                                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                                                                        int numOfPatientsAdded = 0;
                                                                                        if (task.isSuccessful()) {
                                                                                            for (DocumentChange documentChange : task.getResult().getDocumentChanges()) {
                                                                                                Calendar registrationCalendar = Calendar.getInstance();
                                                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                                                                    registrationCalendar.set(Math.toIntExact(documentChange.getDocument().getLong("currentYear")), Math.toIntExact(documentChange.getDocument().getLong("currentMonth") - 1), Math.toIntExact(documentChange.getDocument().getLong("currentDay")), 0, 0);
                                                                                                } else {
                                                                                                    registrationCalendar.set(documentChange.getDocument().getLong("currentYear").intValue(), (documentChange.getDocument().getLong("currentMonth").intValue() - 1), (documentChange.getDocument().getLong("currentDay").intValue()), 0, 0);
                                                                                                }
                                                                                                Date registrationDate = registrationCalendar.getTime();
                                                                                                if (isSubscribed(purchaseDate, registrationDate, subEndTime)) {
                                                                                                    Log.d(TAG, "4.5 : onComplete: " + "purchaseDate : " + purchaseDate);
                                                                                                    Log.d(TAG, "4.5 : onComplete: " + "registrationDate : " + registrationDate);
                                                                                                    Log.d(TAG, "4.5 : onComplete: " + "subEndTime : " + subEndTime);
                                                                                                    numOfPatientsAdded++;
                                                                                                }
                                                                                            }
                                                                                                Log.d(TAG, "5. onComplete: " + " numOfPatientsAdded : " +numOfPatientsAdded);
                                                                                                Log.d(TAG, "6. onComplete: " + " finalQuota : " +finalQuota);
                                                                                                Log.d(TAG, "7. onComplete: " + " finalValidityInMonths : " +finalValidityInMonths);
                                                                                                if (numOfPatientsAdded < finalQuota || finalQuota == -1) {
                                                                                                    if (profile_uri != null) {
                                                                                                        mViewModel.uploadProfileImage(profile_uri, uploadToStorageInterface, "profiles", requireContext());
                                                                                                    } else {
                                                                                                        mViewModel.storePatientToDb(name, age, phone_no, email, address, doctor_unique_id, "no_profile", requireContext());
                                                                                                    }
                                                                                                } else {
                                                                                                    Log.d(TAG, "8. onComplete: " + "quota : " + finalQuota);
                                                                                                    Log.d(TAG, "9. onComplete: " + "validityInMonths : " + finalValidityInMonths);
                                                                                                    Log.d(TAG, "10. onComplete: " + "subscriptionPlan : " + subscriptionId);
                                                                                                    LimitExist();
                                                                                                    //Toast.makeText(requireContext(), "Doctor's this months patients quota is full", Toast.LENGTH_LONG).show();
                                                                                                }

                                                                                        } else {
                                                                                            Log.d(TAG, "11. onComplete: " + task.getException().getMessage());
                                                                                        }
                                                                                    }
                                                                                });
                                                                    } else {
                                                                        Log.d(TAG, "12. onComplete: " + "is Not Subscribed");
                                                                        if (profile_uri != null) {
                                                                            mViewModel.uploadProfileImage(profile_uri, uploadToStorageInterface, "profiles", requireContext());
                                                                        } else {
                                                                            mViewModel.storePatientToDb(name, age, phone_no, email, address, doctor_unique_id, "no_profile", requireContext());
                                                                        }
                                                                    }

                                                                } else {
                                                                    Log.d(TAG, "12. onComplete: " + "New Doctor");
                                                                    if (profile_uri != null) {
                                                                        mViewModel.uploadProfileImage(profile_uri, uploadToStorageInterface, "profiles", requireContext());
                                                                    } else {
                                                                        mViewModel.storePatientToDb(name, age, phone_no, email, address, doctor_unique_id, "no_profile", requireContext());
                                                                    }
                                                                }
                                                            } else {
                                                                Log.d(TAG, "13. onComplete: " + task.getException().getMessage());
                                                            }
                                                        }
                                                    });

                                        }
                                    }

                                    @Override
                                    public void onFailure(String freeSubscriptionId) {

                                    }
                                };

                                checkSubscriptionId(doctor_unique_id,returnString);
                            } else {
                                Snackbar.make(view,"Please Enter Doctor's unique id", BaseTransientBottomBar.LENGTH_SHORT).show();
                            }

                        } else {
                            Snackbar.make(view,"Please Enter address", BaseTransientBottomBar.LENGTH_SHORT).show();
                        }

                    } else {
                        Snackbar.make(view,"Please Enter Email", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }

                } else {
                    Snackbar.make(view,"Please Enter Phone Number", BaseTransientBottomBar.LENGTH_SHORT).show();
                }

            } else {
                Snackbar.make(view,"Please Enter Age", BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(view,"Please Enter Name", BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }

    private void initViews(View view) {
        civ_profile = view.findViewById(R.id.profile_image);
        et_name = view.findViewById(R.id.et_name);
//        et_name.setText("Nihal98");
        et_age = view.findViewById(R.id.et_age);
//        et_age.setText("30");
        sp_gender = view.findViewById(R.id.sp_gender);
        et_phone_no = view.findViewById(R.id.et_phone_no);
//        et_phone_no.setText("9898935606");
        et_email = view.findViewById(R.id.et_email);
//        et_email.setText("nihalde22@gmail.com");
        et_address = view.findViewById(R.id.et_address);
//        et_address.setText("Surat Gujarat");
        et_doctors_unique_id = view.findViewById(R.id.doctors_unique_id);
//        et_doctors_unique_id.setText("9510165810");
        bt_submit = view.findViewById(R.id.bt_submit);
        ImageView iv_background = view.findViewById(R.id.iv_background);
        Bitmap bitmapLocal = Config.decodeSampledBitmapFromResource(getResources(), R.drawable.register_bg, 500, 500);
        if (bitmapLocal != null && iv_background != null) {
            iv_background.setImageBitmap(bitmapLocal);
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        uploadToStorageInterface = new UploadToStorageInterface() {
            @Override
            public void onStart() {

            }

            @Override
            public void onProgress(int progress) {
            }

            @Override
            public void onSuccess(Uri downloadUri,String field) {
                mViewModel.storePatientToDb(name,age,phone_no,email,address,doctor_unique_id,downloadUri.toString(),requireContext());
            }

            @Override
            public void onFailure() {

            }
        };
    }

    public void LimitExist(){
        androidx.appcompat.app.AlertDialog deleteDialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                // set message, title, and icon
                .setTitle("Alert")
                .setMessage("Limit exceeded, please contact doctor.")

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }

                })
                .create();

        deleteDialog.show();
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
//            mViewModel.uploadProfileImage(getImageUri(requireContext(),bitmap), uploadToStorageInterface,"profiles", requireContext());
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
//            mViewModel.uploadProfileImage(getImageUri(requireContext(),bitmap), uploadToStorageInterface,"profiles", requireContext());
        }
        Glide.with(requireContext()).load(profile_uri.toString()).into(civ_profile);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

}