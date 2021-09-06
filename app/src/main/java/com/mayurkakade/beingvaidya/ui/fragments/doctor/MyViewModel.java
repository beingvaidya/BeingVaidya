package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mayurkakade.beingvaidya.Config;
import com.mayurkakade.beingvaidya.data.AddCommentListener;
import com.mayurkakade.beingvaidya.data.OnQueryDataListener;
import com.mayurkakade.beingvaidya.data.UploadMultipleImagesInterface;
import com.mayurkakade.beingvaidya.data.UploadToStorageInterface;
import com.mayurkakade.beingvaidya.data.adapters.CommentsAdapter;
import com.mayurkakade.beingvaidya.data.adapters.FeedAdapter;
import com.mayurkakade.beingvaidya.data.adapters.MyPostsAdapter;
import com.mayurkakade.beingvaidya.data.adapters.PatientsAdapter;
import com.mayurkakade.beingvaidya.data.adapters.SavedPostsAdapter;
import com.mayurkakade.beingvaidya.data.adapters.SliderAdapterFeed;
import com.mayurkakade.beingvaidya.data.models.CommentModel;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;
import com.mayurkakade.beingvaidya.data.models.FeedModel;
import com.mayurkakade.beingvaidya.data.models.PatientModel;
import com.mayurkakade.beingvaidya.data.models.PatientsCommunityImageModel;
import com.mayurkakade.beingvaidya.data.models.SliderItem;
import com.mayurkakade.beingvaidya.notification.MessagingUtils;
import com.mayurkakade.beingvaidya.ui.ProgressUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor.CAMERA_REQUEST_FEED;
import static com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor.CAMERA_REQUEST_PATIENTS_COMMUNITY;
import static com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor.CAMERA_REQUEST_PRESCRIPTION;
import static com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor.CAMERA_REQUEST_REPORT;
import static com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor.MY_CAMERA_PERMISSION_CODE;

public class MyViewModel {

    public static final String TAG = "myViewModelDebug";
    MessagingUtils messagingUtils;

    public void getDataFromServer(List<PatientModel> pListStarred, List<PatientModel> pListNonStarred, OnQueryDataListener onQueryDataListener) {
        onQueryDataListener.onStart();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors/"+ FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/Patients").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentChange doc: task.getResult().getDocumentChanges()) {
                        boolean starPatient = false;
                        if (doc.getDocument().getBoolean("starPatient")!=null) {
                            starPatient = doc.getDocument().getBoolean("starPatient");
                        }
                        if (starPatient) {
                            firebaseFirestore.collection("Patients").document(doc.getDocument().getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        PatientModel patientModel = task.getResult().toObject(PatientModel.class);
                                        pListStarred.add(patientModel);
                                        onQueryDataListener.onSuccess();
                                    } else {
                                        Log.d(TAG, "onComplete: " + task.getException().getMessage());
                                        onQueryDataListener.onFailure(task.getException().getMessage());
                                    }
                                }
                            });
                        } else {
                            firebaseFirestore.collection("Patients").document(doc.getDocument().getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        PatientModel patientModel = task.getResult().toObject(PatientModel.class);
                                        pListNonStarred.add(patientModel);
                                        onQueryDataListener.onSuccess();
                                    } else {
                                        Log.d(TAG, "onComplete: " + task.getException().getMessage());
                                        onQueryDataListener.onFailure(task.getException().getMessage());
                                    }
                                }
                            });
                        }

                    }

                } else {
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }

    public void uploadImages(List<SliderItem> files, UploadMultipleImagesInterface uploadToStorageInterface, String field, String description, Context context) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        String filename = String.valueOf(Calendar.getInstance().getTimeInMillis());


        uploadToStorageInterface.onStart();

        List<String> downloadUrls = new ArrayList<>();
        uploadToStorageInterface.onStart();
        for (/*SliderItem file : files*/int i =0 ; i< files.size(); i++) {
            StorageReference prescriptionsReference = storageReference.child("prescriptions/"+filename+i+".jpg");
            SliderItem file = files.get(i);
            uploadToStorageInterface.onProgress(i);
            UploadTask uploadTask = prescriptionsReference.putFile(file.getImgUri());
            int finalI = i;
            Task<Uri> urlTask = uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if (!task.isSuccessful()) {
                    uploadToStorageInterface.onFailure();
                    Log.d(TAG, "uploadImage: " + task.getException().getMessage());
                }
                return prescriptionsReference.getDownloadUrl();
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
//                        downloadUrls.add(downloadUri.toString());
                        uploadToStorageInterface.addUrl(downloadUri.toString(), finalI);

                        //uploadToStorageInterface.onSuccess(downloadUrls, description);

                        Log.d(TAG+"1", "onComplete: " + downloadUri.toString());
                    } else {
                        uploadToStorageInterface.onFailure();
                        Log.d(TAG, "onComplete: " + task.getException().getMessage());
                    }
                }
            });
        }
        Log.d("dload_size", "uploadImages: " + downloadUrls.size());

//        progress.hideProgress();
    }

    public void uploadImage(Uri file, UploadToStorageInterface uploadToStorageInterface, String field,String description, Context context) {
        final ProgressUtils progress = ProgressUtils.getInstance(context);
        progress.showProgress("Please wait", "Uploading Image");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        String filename = String.valueOf(Calendar.getInstance().getTimeInMillis());
        StorageReference prescriptionsReference = storageReference.child("prescriptions/"+filename+".jpg");

        uploadToStorageInterface.onStart();

        UploadTask uploadTask = prescriptionsReference.putFile(file);
        Task<Uri> urlTask = uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
            uploadToStorageInterface.onStart();
            if (!task.isSuccessful()) {
                uploadToStorageInterface.onFailure();
                progress.hideProgress();
                Log.d(TAG, "uploadImage: " + task.getException().getMessage());
            }
            return prescriptionsReference.getDownloadUrl();
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                progress.hideProgress();
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    uploadToStorageInterface.onSuccess(downloadUri,description);
                    Log.d(TAG, "onComplete: " + downloadUri.toString());
                } else {
                    uploadToStorageInterface.onFailure();
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }

    public void uploadPrescriptionImage(Uri file, UploadToStorageInterface uploadToStorageInterface, String field,String description, Context context) {
        final ProgressUtils progress = ProgressUtils.getInstance(context);
        progress.showProgress("Please wait", "Uploading Image");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        String filename = String.valueOf(Calendar.getInstance().getTimeInMillis());
        StorageReference prescriptionsReference = storageReference.child("prescriptions/"+filename+".jpg");

        uploadToStorageInterface.onStart();

        UploadTask uploadTask = prescriptionsReference.putFile(file);
        Task<Uri> urlTask = uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
            uploadToStorageInterface.onStart();
            if (!task.isSuccessful()) {
                uploadToStorageInterface.onFailure();
                progress.hideProgress();
                Log.d(TAG, "uploadImage: " + task.getException().getMessage());
            }
            return prescriptionsReference.getDownloadUrl();
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                progress.hideProgress();
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    uploadToStorageInterface.onSuccess(downloadUri,field);
                    Log.d(TAG, "onComplete: " + downloadUri.toString());
                } else {
                    uploadToStorageInterface.onFailure();
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }

    public void getImage(Fragment fragment, FragmentActivity context, boolean isPrescription) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        context.requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                    } else {
                        if (isPrescription) {
                            Intent cropImageIntent = CropImage.activity().getIntent(context);
                            fragment.startActivityForResult(cropImageIntent, CAMERA_REQUEST_PRESCRIPTION);
                        } else {
                            Intent cropImageIntent = CropImage.activity().getIntent(context);
                            fragment.startActivityForResult(cropImageIntent, CAMERA_REQUEST_REPORT);
                        }
                    }
                }
    }

    public void getImageFeed(Fragment fragment, FragmentActivity context) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    {
                        context.requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                    } else {
                        Intent cropImageIntent = new Intent();
                        // setting type to select to be image
                        cropImageIntent.setType("image/*");
                        cropImageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        cropImageIntent.setAction(Intent.ACTION_GET_CONTENT);
                        fragment.startActivityForResult(cropImageIntent, CAMERA_REQUEST_FEED);
                    }
                }

    }

    public void updateDate(int year, int monthOfYear, int dayOfMonth, EditText et_datePicker, String phoneNo, ProgressUtils progressUtils) {

        Map<String, Object> params = new HashMap<>();
        params.put("year",year);
        params.put("monthOfYear",monthOfYear+1);
        params.put("dayOfMonth",dayOfMonth);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("Doctors/"+FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/Patients").document(phoneNo).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                firebaseFirestore.collection("Doctors/"+FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/Patients").document(phoneNo)
                                        .update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            et_datePicker.setText( dayOfMonth + " / " + (monthOfYear+1)+" / " + year);
                                            progressUtils.hideProgress();
                                        } else {
                                            progressUtils.hideProgress();
                                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                                        }
                                    }
                                });
                            } else {
                                firebaseFirestore.collection("Doctors/"+FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/Patients").document(phoneNo)
                                        .set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            progressUtils.hideProgress();
                                            et_datePicker.setText( dayOfMonth + " / " + (monthOfYear+1)+" / " + year);
                                        } else {
                                            progressUtils.hideProgress();
                                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
    }

    public void setReviewDate(EditText et_datePicker, String argPhoneNo) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + "/Patients").document(argPhoneNo)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        try {
                            String dayOfMonth = String.valueOf(task.getResult().getLong("dayOfMonth"));
                            String monthOfYear = String.valueOf(task.getResult().getLong("monthOfYear"));
                            String year = String.valueOf(task.getResult().getLong("year"));

                            et_datePicker.setText( dayOfMonth + " / " + monthOfYear+" / " + year );
                        } catch (NullPointerException e) {
                            Log.d(TAG, "onComplete: " + e.getMessage());
                        }

                    }
                } else {
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }

    public void getStarPatientOrNot(SwitchCompat switchStarPatient, String argPhoneNo) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + "/Patients").document(argPhoneNo)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        if (task.getResult() != null) {
                            if (task.getResult().getBoolean("starPatient") != null) {
                                switchStarPatient.setChecked(task.getResult().getBoolean("starPatient"));
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }

    public void setStarPatientOrNot(SwitchCompat switchStarPatient, String argPhoneNo, Boolean starPatient, ProgressUtils progressUtils) {
        Map<String, Object> params = new HashMap<>();
        params.put("starPatient",starPatient);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("Doctors/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + "/Patients").document(argPhoneNo)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        firebaseFirestore.collection("Doctors/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + "/Patients").document(argPhoneNo)
                                .update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    switchStarPatient.setChecked(starPatient);

                                } else {
                                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                                }
                                progressUtils.hideProgress();
                            }
                        });
                    } else {
                        firebaseFirestore.collection("Doctors/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + "/Patients").document(argPhoneNo)
                                .set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    switchStarPatient.setChecked(starPatient);
                                } else {
                                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                                }
                                progressUtils.hideProgress();
                            }
                        });
                    }
                }
            }
        });
    }

    public void addImageToPatientDirectory(String downloadUri, String argPhoneNo,String field) {
        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> params = new HashMap<>();
        params.put("downloadUri",downloadUri);
        params.put("currentTime",currentTime);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + "/Patients/"+argPhoneNo+"/" + field).add(params)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: addImageToPatientDir");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
    }


    public void getAllPostsFeed(FeedAdapter adapter, List<FeedModel> fList , ProgressBar  progress_loader, RecyclerView recyclerView) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("DoctorsFeed").orderBy("currentTime", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        if (!task.getResult().isEmpty()) {

                            for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                FeedModel feedModel = doc.getDocument().toObject(FeedModel.class).withId(doc.getDocument().getId());
                                if (feedModel.isBanner()) {
                                    firebaseFirestore.collection("BannerImages").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        List<SliderItem> local  = new ArrayList<>();
                                                        for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                                            local.add(new SliderItem(doc.getDocument().getString("img_url")));
                                                        }
                                                        feedModel.setmSliderItems(local);
                                                    }
                                                }
                                            });
                                }else {

                                    getDoctorName(feedModel.getDoctor_id(),feedModel );
                                    getNumberOfCommentsViews(feedModel.DocId, feedModel);
                                    firebaseFirestore.collection("Doctors").document(feedModel.getDoctor_id()).get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        if (task.getResult() != null) {
                                                            DoctorModel doctorModel = task.getResult().toObject(DoctorModel.class);
                                                            if (doctorModel != null) {

                                                                if (doctorModel.getPhone_no() != null) {
                                                                    if (!doctorModel.getPhone_no().equals("") || !doctorModel.getPhone_no().equals("no_profile")) {
                                                                        feedModel.setDoctor_profile_photo(doctorModel.getProfile_url());
//                                                                        Glide.with(context).load(doctorModel.getProfile_url()).into(holder.civ_profile);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                    firebaseFirestore.collection("DoctorsFeed/" + feedModel.DocId + "/images").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        List<SliderItem> local  = new ArrayList<>();
                                                        for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                                            local.add(new SliderItem(doc.getDocument().getString("img_url")));
                                                        }
                                                        feedModel.setmSliderItemsDoctor(local);

                                                    }
                                                }
                                            });
                                }

                                fList.add(feedModel);
                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progress_loader.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    adapter.notifyDataSetChanged();
                                }
                            }, 1000);


                        }
                    }
                } else {
                    progress_loader.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }


    private void getNumberOfCommentsViews(String feedId, FeedModel feedModel ) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("DoctorsFeed/"+feedId+"/comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        feedModel.setComment_show(String.valueOf(task.getResult().size()) + " Comments");
                    } else {
                        feedModel.setComment_show("0 Comments");
                    }
                }
            }
        });
        firebaseFirestore.collection("DoctorsFeed/"+feedId+"/views").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        feedModel.setView_show(String.valueOf(task.getResult().size()) + " Views");
                    } else {
                        feedModel.setView_show("0 Views");
                    }
                }
            }
        });

    }







    private void getDoctorName(String doctor_id, FeedModel feedModel) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors").document(doctor_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                if (task.getResult().exists()) {
                                    feedModel.setDoctorName(task.getResult().getString("name"));
                                }
                            }
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });


    }

    public void getCommentsFromServer(CommentsAdapter mAdapter, List<CommentModel> cList, String feedId, ProgressBar progressBar) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("DoctorsFeed/"+feedId+"/comments").orderBy("timestamp", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        if (task.getResult() != null) {
                            for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                cList.add(doc.getDocument().toObject(CommentModel.class));
                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }, 1000);


                        }else {
                            progressBar.setVisibility(View.GONE);
                        }
                    }else {
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });


    }


    public void addFeedComment(String feedId, String commentText, AddCommentListener addCommentListener) {
        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> params = new HashMap<>();
        params.put("docId",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        params.put("commentText",commentText);
        params.put("timestamp",currentTime);
        addCommentListener.onStart();

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("DoctorsFeed/"+feedId+"/comments").document()
                .set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    addCommentListener.onSuccess();
                } else {
                    addCommentListener.onFailure();
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }

    public void getImagePatientsCommunity(PatientsCommunityFragment fragment, FragmentActivity context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    {
                        context.requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                    } else {
                        if (options[item].equals("Take Photo")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            fragment.startActivityForResult(takePicture, CAMERA_REQUEST_PATIENTS_COMMUNITY);
                        } else if (options[item].equals("Choose from Gallery")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            fragment.startActivityForResult(pickPhoto , CAMERA_REQUEST_PATIENTS_COMMUNITY);

                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                }

            }
        });
        builder.show();

    }

    public void addImageToCommunityDirectory(String downloadUri, String description, OnQueryDataListener onQueryDataListener) {
        onQueryDataListener.onStart();
        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> params = new HashMap<>();
        params.put("doctor_id",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        params.put("currentTime",currentTime);
        params.put("description",description);
        params.put("downloadUri",downloadUri);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        WriteBatch batch = firebaseFirestore.batch();
        CollectionReference collectionReference = firebaseFirestore.collection("PatientsCommunity");
        collectionReference.add(params).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {

                    if (task.getResult() != null) {
                        onQueryDataListener.onSuccessPatientsCommunity(new PatientsCommunityImageModel(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),downloadUri,description, Timestamp.now()));
                        String patients_community_id = task.getResult().getId();
                        Map<String, Object> referenceToFeed = new HashMap<>();
                        referenceToFeed.put("patients_community_id", patients_community_id);
                        params.put("currentTime",currentTime);

                        DocumentReference documentReference =  firebaseFirestore.collection("Doctors/"+FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/MyPatientsCommunityTips").document(patients_community_id);
                        batch.set(documentReference,referenceToFeed);
                        batch.commit();

                        firebaseFirestore.collection("Patients").get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()){
                                            if (task.getResult() != null) {
                                                messagingUtils = MessagingUtils.getInstance();
                                                for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                                    messagingUtils.sendCloudNotification(doc.getDocument().getId(),FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),"New post added in Tips", false, Config.NOTIFICATION_TYPE_TIPS, patients_community_id );
                                                }
                                            }
                                        }
                                    }
                                });

                    }
                } else {
                    onQueryDataListener.onFailure(task.getException().getMessage());
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }

    public void getMyPostsData(FeedAdapter adapter, List<FeedModel> fList) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors/"+FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/MyFeeds").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        if (!task.getResult().isEmpty()) {
                            Log.d(TAG, "onComplete: " + "success in task");
                            for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                Log.d(TAG, "onComplete: " + doc.getDocument().getId());
                                firebaseFirestore.collection("DoctorsFeed").document(doc.getDocument().getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            FeedModel feedModel = task.getResult().toObject(FeedModel.class).withId(task.getResult().getId());
                                            fList.add(feedModel);
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                                        }
                                    }
                                });
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }
    public void getMyPostsData(MyPostsAdapter adapter, List<FeedModel> fList) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors/"+FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/MyFeeds").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        if (!task.getResult().isEmpty()) {
                            Log.d(TAG, "onComplete: " + "success in task");
                            for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                Log.d(TAG, "onComplete: " + doc.getDocument().getId());
                                firebaseFirestore.collection("DoctorsFeed").document(doc.getDocument().getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().exists()) {
                                                FeedModel feedModel = task.getResult().toObject(FeedModel.class).withId(task.getResult().getId());
                                                fList.add(feedModel);
                                                adapter.notifyDataSetChanged();
                                            }
                                        } else {
                                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                                        }
                                    }
                                });
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }

    public void getSavedPostsData(SavedPostsAdapter adapter, List<FeedModel> fList) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors/"+FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/savedPosts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        if (!task.getResult().isEmpty()) {
                            Log.d(TAG, "onComplete: " + "success in task");
                            for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                Log.d(TAG, "onComplete: " + doc.getDocument().getId());
                                firebaseFirestore.collection("DoctorsFeed").document(doc.getDocument().getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().exists()) {
                                                FeedModel feedModel = task.getResult().toObject(FeedModel.class).withId(task.getResult().getId());
                                                fList.add(feedModel);
                                                adapter.notifyDataSetChanged();
                                            }
                                        } else {
                                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                                        }
                                    }
                                });
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }

    public void addMultipleFeedImages(String name, String details, List<String> downloadUrls) {

        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> params = new HashMap<>();
        params.put("doctor_id",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        params.put("currentTime",currentTime);
        params.put("title", name);
        params.put("caption",details);
        params.put("img_url","no_image");
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
//        WriteBatch batch = firebaseFirestore.batch();
        firebaseFirestore.collection("DoctorsFeed").add(params).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {

                    if (task.getResult() != null) {
                        String feed_id = task.getResult().getId();
                        Map<String, Object> referenceToFeed = new HashMap<>();
                        referenceToFeed.put("feed_id", feed_id);

                        CollectionReference imagesCollectionReference = firebaseFirestore.collection("DoctorsFeed/"+feed_id+"/images");

                        Log.d("downloadUrls_mdl", "onComplete: ref: " + downloadUrls.size());

                        for (String url : downloadUrls) {
                            Map<String, Object> imagesFeed = new HashMap<>();
                            imagesFeed.put("img_url",url);
                            DocumentReference imageDocumentReference =  imagesCollectionReference.document();
                            imageDocumentReference.set(imagesFeed);
                        }

                        params.put("currentTime",currentTime);

                        DocumentReference documentReference =  firebaseFirestore.collection("Doctors/"+FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/MyFeeds").document(feed_id);
                        documentReference.set(referenceToFeed);
//                        batch.set(documentReference,referenceToFeed);
//                        batch.commit();

                    }
                } else {
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });


    }

    public void addImageToFeedDirectory(String name, String details,String downloadUri) {

        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> params = new HashMap<>();
        params.put("doctor_id",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        params.put("currentTime",currentTime);
        params.put("title", name);
        params.put("caption",details);
        params.put("img_url",downloadUri);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        WriteBatch batch = firebaseFirestore.batch();
        CollectionReference collectionReference = firebaseFirestore.collection("DoctorsFeed");
        collectionReference.add(params).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {

                    if (task.getResult() != null) {
                        String feed_id = task.getResult().getId();
                        Map<String, Object> referenceToFeed = new HashMap<>();
                        referenceToFeed.put("feed_id", feed_id);
                        params.put("currentTime",currentTime);

                        DocumentReference documentReference =  firebaseFirestore.collection("Doctors/"+FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/MyFeeds").document(feed_id);
                        batch.set(documentReference,referenceToFeed);
                        batch.commit();

                    }
                } else {
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }
}
