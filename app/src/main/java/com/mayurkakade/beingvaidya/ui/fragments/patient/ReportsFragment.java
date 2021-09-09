package com.mayurkakade.beingvaidya.ui.fragments.patient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.ReportsAdapter;
import com.mayurkakade.beingvaidya.data.models.FirebaseImageModel;

import java.util.ArrayList;
import java.util.List;

public class ReportsFragment extends Fragment {


    private RecyclerView recyclerView;
    private List<FirebaseImageModel> pList;
    private ReportsAdapter adapter;


    public static final String TAG = "PrescriptionFrag";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        pList = new ArrayList<>();
        adapter = new ReportsAdapter(requireActivity(), pList);
        recyclerView.setAdapter(adapter);

        getReportsFromServer(pList,adapter);
        return view;
    }


    private void getReportsFromServer(List<FirebaseImageModel> pList, ReportsAdapter adapter) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LOCAL_AUTH", Context.MODE_PRIVATE);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors/"+ sharedPreferences.getString("doctor_unique_id","NULL") +"/Patients/"+ FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/reports").orderBy("currentTime", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentChange doc: task.getResult().getDocumentChanges()) {
                        if (task.isSuccessful()) {
                            FirebaseImageModel model = doc.getDocument().toObject(FirebaseImageModel.class);
                            pList.add(model);
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