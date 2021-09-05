package com.mayurkakade.beingvaidya.ui.fragments.patient;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.TipsAdapter;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;
import com.mayurkakade.beingvaidya.data.models.PatientsCommunityImageModel;

import java.util.ArrayList;
import java.util.List;

public class TipsFragment extends Fragment {


    public static final String TAG = "Tips";
    FloatingActionButton fab_upload;
    RecyclerView recyclerView;
    List<PatientsCommunityImageModel> iList;
    TipsAdapter adapter;
    ProgressBar progress_loader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tips, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        fab_upload = view.findViewById(R.id.fab_upload);
        progress_loader = view.findViewById(R.id.progress_loader);
        iList = new ArrayList<>();
        adapter = new TipsAdapter(container.getContext(), iList);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(adapter);
        progress_loader.setVisibility(View.VISIBLE);
        getPatientsTipsImagesFromServer(iList, adapter);
        return view;
    }

    private void getDoctorName(String doctor_id, PatientsCommunityImageModel feedModel) {
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

    }


    private void getPatientsTipsImagesFromServer(List<PatientsCommunityImageModel> iList, TipsAdapter adapter) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("PatientsCommunity").orderBy("currentTime", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                        if (task.isSuccessful()) {
                            PatientsCommunityImageModel model = doc.getDocument().toObject(PatientsCommunityImageModel.class);
                            getDoctorName(model.getDoctor_id(), model);
                            iList.add(model);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progress_loader.setVisibility(View.GONE);
                                    adapter.notifyDataSetChanged();
                                }
                            }, 1000);

                        } else {
                            progress_loader.setVisibility(View.GONE);
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