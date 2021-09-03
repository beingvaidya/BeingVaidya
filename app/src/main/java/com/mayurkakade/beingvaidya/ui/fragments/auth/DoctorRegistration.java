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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mayurkakade.beingvaidya.Config;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.UploadToStorageInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor.CAMERA_REQUEST_PRESCRIPTION;
import static com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor.CAMERA_REQUEST_REPORT;
import static com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor.MY_CAMERA_PERMISSION_CODE;

public class DoctorRegistration extends Fragment {

    private MainViewModel mViewModel;
    private UploadToStorageInterface uploadToStorageInterface;
    private EditText et_name,et_phone_no,et_mail,et_qualification,et_university,et_pincode;
    private Spinner sp_degree;
    private Button bt_submit;
    private String name,degree,phone_no,email,qualification,university,pincode;
    private View view;
    private Uri profile_uri = null;
    private CircleImageView civ_profile;
    public static final int CAMERA_REQUEST_PROFILE = 108;
    public static final int STORAGE_REQUEST_PROFILE = 109;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_registration, container, false);
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

    private void getDataFromViews() {
        //name
        if (!TextUtils.isEmpty(et_name.getText())) {
            name = et_name.getText().toString();
            //select dr ug pg
            if (!sp_degree.getSelectedItem().toString().equals("Select Degree")) {
                degree = sp_degree.getSelectedItem().toString();
                //phone no
                if (!TextUtils.isEmpty(et_phone_no.getText())) {
                    phone_no = et_phone_no.getText().toString();
                    //phone no
                    if (!TextUtils.isEmpty(et_mail.getText())) {
                        email = et_mail.getText().toString();
                        //qualification
                        if (!TextUtils.isEmpty(et_qualification.getText())) {
                            qualification = et_qualification.getText().toString();
                            //university
                            if (!TextUtils.isEmpty(et_university.getText())) {
                                university = et_university.getText().toString();
                                //pincode
                                if (!TextUtils.isEmpty(et_pincode.getText())) {
                                    pincode = et_pincode.getText().toString();

                                    if (profile_uri != null) {
                                        Toast.makeText(requireContext(), "profile not null", Toast.LENGTH_SHORT).show();
                                        mViewModel.uploadProfileImage(profile_uri, uploadToStorageInterface, "profiles", requireContext());
                                    } else {
                                        Toast.makeText(requireContext(), "no profile", Toast.LENGTH_SHORT).show();
                                        mViewModel.storeDoctorToDb(name, degree,phone_no,email,qualification,university,pincode,"no_profile",requireContext());
                                    }

                                } else {
                                    Snackbar.make(view,"Please Enter Pincode", BaseTransientBottomBar.LENGTH_SHORT).show();
                                }
                            } else {
                                Snackbar.make(view,"Please Enter University", BaseTransientBottomBar.LENGTH_SHORT).show();
                            }
                        } else {
                            Snackbar.make(view,"Please Enter Qualification", BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    } else {
                        Snackbar.make(view,"Please Enter Email", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(view,"Please Enter Phone Number", BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(view,"Please Select Degree", BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(view,"Please Enter Name", BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }

    private void initViews(View view) {
        civ_profile = view.findViewById(R.id.profile_image);
        et_name = view.findViewById(R.id.et_name);
        sp_degree = view.findViewById(R.id.sp_degree);
        et_phone_no = view.findViewById(R.id.et_phone_no);
        et_mail = view.findViewById(R.id.et_mail);
        et_qualification = view.findViewById(R.id.et_qualification);
        et_university = view.findViewById(R.id.et_university);
        et_pincode = view.findViewById(R.id.et_pincode);
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
                mViewModel.storeDoctorToDb(name, degree,phone_no,email,qualification,university,pincode,downloadUri.toString(),requireContext());
            }

            @Override
            public void onFailure() {

            }
        };


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

        Glide.with(civ_profile).load(profile_uri.toString()).into(civ_profile);

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

}