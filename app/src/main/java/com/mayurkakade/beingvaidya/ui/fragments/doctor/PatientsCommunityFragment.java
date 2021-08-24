package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.OnQueryDataListener;
import com.mayurkakade.beingvaidya.data.UploadToStorageInterface;
import com.mayurkakade.beingvaidya.data.adapters.PatientsCommunityAdapter;
import com.mayurkakade.beingvaidya.data.adapters.PrescriptionsAdapter;
import com.mayurkakade.beingvaidya.data.models.FirebaseImageModel;
import com.mayurkakade.beingvaidya.data.models.PatientsCommunityImageModel;
import com.mayurkakade.beingvaidya.ui.ProgressUtils;
import com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PatientsCommunityFragment extends Fragment {

    FloatingActionButton fab_upload;
    RecyclerView recyclerView;
    List<PatientsCommunityImageModel> iList;

    public static final String TAG = "PatientsCommunity";
    PatientsCommunityAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patients_community, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        fab_upload = view.findViewById(R.id.fab_upload);
        iList = new ArrayList<>();
        adapter = new PatientsCommunityAdapter(container.getContext(),iList);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(adapter);
        getPatientsCommunityImagesFromServer(iList,adapter);
        return view;
    }

    UploadToStorageInterface uploadToStorageInterface;

    private MyViewModel mViewModel;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new MyViewModel();

        final ProgressUtils progressUtils = ProgressUtils.getInstance(requireContext());

        onQueryDataListener = new OnQueryDataListener() {
            @Override
            public void onSuccess() {
                adapter.notifyDataSetChanged();
                progressUtils.hideProgress();
            }

            @Override
            public void onSuccessPatientsCommunity(PatientsCommunityImageModel patientsCommunityImageModel) {
                iList.add(0,patientsCommunityImageModel);
                adapter.notifyDataSetChanged();
                progressUtils.hideProgress();
            }

            @Override
            public void onStart() {
                progressUtils.showProgress("Please Wait", "Saving Post");
            }

            @Override
            public void onFailure(String exception) {

            }
        };




        uploadToStorageInterface = new UploadToStorageInterface() {
            @Override
            public void onStart() {
                Log.d(TAG, "onStart: ");
                progressUtils.showProgress("Please wait", "uploading Image");
            }

            @Override
            public void onProgress(int progress) {
                Log.d(TAG, "onProgress: " + progress);
            }

            @Override
            public void onSuccess(Uri downloadUri, String field) {
                Log.d(TAG, "onSuccess: " + downloadUri.toString());
                progressUtils.hideProgress();
                mViewModel.addImageToCommunityDirectory(downloadUri.toString(),field,onQueryDataListener);

            }

            @Override
            public void onFailure() {
                progressUtils.hideProgress();
                Toast.makeText(requireContext(), "Upload Unsuccessful", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: " + "failed");
            }
        };

        fab_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(requireContext(),v);
                popupMenu.inflate(R.menu.community_fragment_upload_menu);
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.image_and_text:
                                mViewModel.getImagePatientsCommunity(PatientsCommunityFragment.this,requireActivity());
                                break;

                            case R.id.text_only:
                                getDescriptionDialog(null);
                                break;
                        }
                        return true;
                    }
                });


            }
        });
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ActivityDoctor.CAMERA_REQUEST_PATIENTS_COMMUNITY) {
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
            getDescriptionDialog(getImageUri(requireContext(),bitmap));
//            mViewModel.uploadImage(getImageUri(requireContext(),bitmap), uploadToStorageInterface,"patientsCommunityItems", requireContext());
        }
    }

    OnQueryDataListener onQueryDataListener;



    @SuppressLint("UseCompatLoadingForDrawables")
    private void getDescriptionDialog(Uri imageUri) {

        //TODO :: Change dialog to xml layouts
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext());

        // Setting Dialog Title
        alertDialog.setTitle("Description");

        // Setting Dialog Message
//        alertDialog.setMessage("Enter Description");

            final EditText input = new EditText(requireContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            input.setBackground(requireContext().getDrawable(R.drawable.round_corner_green_background));
            input.setPadding(24,24,24,24);

        }
        alertDialog.setView(input);

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_baseline_rss_feed_24);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("SUBMIT",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        if (imageUri != null) {
                            mViewModel.uploadImage(imageUri, uploadToStorageInterface, "patientsCommunityItems", input.getText().toString(), requireContext());
                        } else {
                            mViewModel.addImageToCommunityDirectory("no_image",input.getText().toString(),onQueryDataListener);
                        }
                    }
                });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                    }
                });

        // closed

        // Showing Alert Message
        alertDialog.show();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, String.valueOf(new Date().getTime()), null);
        return Uri.parse(path);
    }

    private void getPatientsCommunityImagesFromServer(List<PatientsCommunityImageModel> iList, PatientsCommunityAdapter adapter) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("PatientsCommunity").orderBy("currentTime", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentChange doc: task.getResult().getDocumentChanges()) {
                        if (task.isSuccessful()) {
                            PatientsCommunityImageModel model = doc.getDocument().toObject(PatientsCommunityImageModel.class).withId(doc.getDocument().getId());
                            iList.add(model);
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                } else {
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });

    }
}