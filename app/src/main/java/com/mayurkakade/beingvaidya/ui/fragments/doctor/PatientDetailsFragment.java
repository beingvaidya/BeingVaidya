package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.canhub.cropper.CropImage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.UploadToStorageInterface;
import com.mayurkakade.beingvaidya.ui.ProgressUtils;
import com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import kotlin.jvm.JvmStatic;

import static android.app.Activity.RESULT_OK;


public class PatientDetailsFragment extends Fragment {

    TextView tv_name,tv_age,tv_phone,tv_email,tv_address;
    FloatingActionButton fab_upload;
    EditText et_datePicker;
    SwitchCompat switchStarPatient;

    Button bt_reports;
    Button bt_prescriptions;

    UploadToStorageInterface uploadToStorageInterface;

    public static final String TAG = "PatientDetailsDebug";

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_details, container, false);

        tv_name = view.findViewById(R.id.tv_name);
        tv_age = view.findViewById(R.id.tv_age);
        tv_phone = view.findViewById(R.id.tv_phone_no);
        tv_email = view.findViewById(R.id.tv_mail);
        tv_address = view.findViewById(R.id.tv_address);
        et_datePicker = view.findViewById(R.id.datePicker);
        switchStarPatient = view.findViewById(R.id.switchStarPatient);
        bt_reports = view.findViewById(R.id.bt_reports);
        bt_prescriptions = view.findViewById(R.id.bt_prescriptions);

        fab_upload = view.findViewById(R.id.fab_upload);

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.doctors_nav_host);
                navController.navigate(R.id.action_patientDetailsFragment_to_patientsFragment);
            }
        });


    if (getArguments() != null) {
        tv_name.setText("Name :" + getArguments().getString("argName"));
        tv_age.setText("Age :"+String.valueOf(getArguments().getInt("argAge")));
        tv_phone.setText("Phone no :"+getArguments().getString("argPhoneNo"));
        tv_email.setText("Email :"+getArguments().getString("argEmail"));
        tv_address.setText("Address :"+getArguments().getString("argAddress"));
    }

        return view;
    }

    private MyViewModel mViewModel;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new MyViewModel();

        uploadToStorageInterface = new UploadToStorageInterface() {
            @Override
            public void onStart() {
                Log.d(TAG, "onStart: ");
            }

            @Override
            public void onProgress(int progress) {
                Log.d(TAG, "onProgress: " + progress);
            }

            @Override
            public void onSuccess(Uri downloadUri,String field) {
                Log.d(TAG, "onSuccess: " + downloadUri.toString());
                mViewModel.addImageToPatientDirectory(downloadUri.toString(),getArguments().getString("argPhoneNo"), field);
            }

            @Override
            public void onFailure() {
                Log.d(TAG, "onFailure: " + "failed");
            }
        };

        mViewModel.setReviewDate(et_datePicker,getArguments().getString("argPhoneNo"));
        mViewModel.getStarPatientOrNot(switchStarPatient,getArguments().getString("argPhoneNo"));

        fab_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(requireContext(),fab_upload);
                popupMenu.inflate(R.menu.doctor_upload_popup);
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_prescription:
                                mViewModel.getImage(PatientDetailsFragment.this,requireActivity(),true);
                                break;

                            case R.id.menu_report:
                                mViewModel.getImage(PatientDetailsFragment.this,requireActivity(),false);
                                break;
                        }
                        return true;
                    }
                });

            }
        });

        bt_reports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(),R.id.doctors_nav_host);
                Bundle args = new Bundle();
                Log.d("checkreports", "onClick: " +getArguments().getString("argPhoneNo"));
                args.putString("arg_patient_id",getArguments().getString("argPhoneNo"));
                args.putString("arg_doctor_id", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

                args.putString("argName",getArguments().getString("argName"));
                args.putInt("argAge",getArguments().getInt("argAge"));
                args.putString("argPhoneNo",getArguments().getString("argPhoneNo"));
                args.putString("argEmail",getArguments().getString("argEmail"));
                args.putString("argAddress",getArguments().getString("argAddress"));
                args.putString("argDoctorId",getArguments().getString("argDoctorId"));

                navController.navigate(R.id.action_patientDetailsFragment_to_patientsReportsFragment,args);
            }
        });

        bt_prescriptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(),R.id.doctors_nav_host);
                Bundle args = new Bundle();
                args.putString("arg_patient_id",getArguments().getString("argPhoneNo"));
                args.putString("arg_doctor_id", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

                args.putString("argName",getArguments().getString("argName"));
                args.putInt("argAge",getArguments().getInt("argAge"));
                args.putString("argPhoneNo",getArguments().getString("argPhoneNo"));
                args.putString("argEmail",getArguments().getString("argEmail"));
                args.putString("argAddress",getArguments().getString("argAddress"));
                args.putString("argDoctorId",getArguments().getString("argDoctorId"));

                navController.navigate(R.id.action_patientDetailsFragment_to_patientPrescriptionsFragment,args);
            }
        });

        Calendar myCalendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                ProgressUtils progressUtils = ProgressUtils.getInstance(requireContext());
                progressUtils.showProgress("Please Wait", "Updating Next Review");
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                mViewModel.updateDate( year, monthOfYear, dayOfMonth, et_datePicker, getArguments().getString("argPhoneNo"), progressUtils);
            }

        };
        et_datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(requireContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        switchStarPatient.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ProgressUtils progressUtils = ProgressUtils.getInstance(requireContext());
                progressUtils.showProgress("Please Wait", "Updating Starred Status");
                mViewModel.setStarPatientOrNot(switchStarPatient,getArguments().getString("argPhoneNo"),isChecked, progressUtils);
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ActivityDoctor.CAMERA_REQUEST_PRESCRIPTION) {
            CropImage.ActivityResult result = CropImage.INSTANCE.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = null;
                if (result != null) {
                    resultUri = result.getUriContent();
                    mViewModel.uploadPrescriptionImage(resultUri, uploadToStorageInterface,"prescriptions",null, requireContext());
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        } else if (requestCode == ActivityDoctor.CAMERA_REQUEST_REPORT) {
            CropImage.ActivityResult result = CropImage.INSTANCE.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = null;
                if (result != null) {
                    resultUri = result.getUriContent();
                    mViewModel.uploadPrescriptionImage(resultUri, uploadToStorageInterface,"reports",null, requireContext());
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}