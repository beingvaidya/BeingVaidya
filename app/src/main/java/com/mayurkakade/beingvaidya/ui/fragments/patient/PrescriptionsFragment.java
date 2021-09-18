package com.mayurkakade.beingvaidya.ui.fragments.patient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.PrescriptionsAdapter;
import com.mayurkakade.beingvaidya.data.models.FirebaseImageModel;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionsFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<FirebaseImageModel> pList;
    private PrescriptionsAdapter adapter;
    private ProgressBar progressBar;


    public static final String TAG = "PrescriptionFrag";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prescription, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        pList = new ArrayList<>();
        adapter = new PrescriptionsAdapter(requireActivity(), pList);
        recyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.VISIBLE);
        getPrescriptionsFromServer(pList,adapter);

        return view;
    }
    boolean handler;
    private void getPrescriptionsFromServer(List<FirebaseImageModel> pList, PrescriptionsAdapter adapter) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LOCAL_AUTH", Context.MODE_PRIVATE);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors/"+ sharedPreferences.getString("doctor_unique_id","NULL") +"/Patients/"+FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/prescriptions").orderBy("currentTime", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()){
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    for (DocumentChange doc: task.getResult().getDocumentChanges()) {
                        if (task.isSuccessful()) {
                          FirebaseImageModel model = doc.getDocument().toObject(FirebaseImageModel.class);
                          pList.add(model);
                            handler =   new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(adapter != null && progressBar != null) {
                                        progressBar.setVisibility(View.GONE);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }, 1000);

                        } else {
                            progressBar.setVisibility(View.GONE);
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }
}