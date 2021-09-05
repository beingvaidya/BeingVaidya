package com.mayurkakade.beingvaidya.ui.fragments.patient;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.TipsAdapter;
import com.mayurkakade.beingvaidya.data.models.PatientsCommunityImageModel;

import java.util.ArrayList;
import java.util.List;

public class TipsFragment extends Fragment {


    public static final String TAG = "Tips";
    FloatingActionButton fab_upload;
    RecyclerView recyclerView;
    List<PatientsCommunityImageModel> iList;
    TipsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tips, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        fab_upload = view.findViewById(R.id.fab_upload);
        iList = new ArrayList<>();
        adapter = new TipsAdapter(container.getContext(), iList);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(adapter);
        getPatientsTipsImagesFromServer(iList, adapter);
        return view;
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