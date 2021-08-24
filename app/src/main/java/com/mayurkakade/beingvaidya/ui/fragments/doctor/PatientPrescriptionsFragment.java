package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.PatientPrescriptionsDoctorSideAdapter;
import com.mayurkakade.beingvaidya.data.adapters.PatientReportsDoctorSideAdapter;
import com.mayurkakade.beingvaidya.data.models.FirebaseImageModel;

import java.util.ArrayList;
import java.util.List;


public class PatientPrescriptionsFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<FirebaseImageModel> pList;
    private PatientPrescriptionsDoctorSideAdapter adapter;
    String doctor_id,patient_id;

    public static final String TAG = "patientPrescriptions";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_prescriptions, container, false);


        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        pList = new ArrayList<>();
        adapter = new PatientPrescriptionsDoctorSideAdapter(container.getContext(), pList);
        recyclerView.setAdapter(adapter);

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.doctors_nav_host);
                Bundle args = new Bundle();
                args.putString("argName",getArguments().getString("argName"));
                args.putInt("argAge",getArguments().getInt("argAge"));
                args.putString("argPhoneNo",getArguments().getString("argPhoneNo"));
                args.putString("argEmail",getArguments().getString("argEmail"));
                args.putString("argAddress",getArguments().getString("argAddress"));
                args.putString("argDoctorId",getArguments().getString("argDoctorId"));
                navController.navigate(R.id.action_patientPrescriptionsFragment_to_patientDetailsFragment,args);
            }
        });

        if (getArguments() != null) {
            patient_id = getArguments().getString("arg_patient_id");
            doctor_id = getArguments().getString("arg_doctor_id");
        }

        getReportsFromServer(pList,adapter);

        // Inflate the layout for this fragment
        return view;
    }

    private void getReportsFromServer(List<FirebaseImageModel> pList, PatientPrescriptionsDoctorSideAdapter adapter) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors/"+ doctor_id +"/Patients/"+ patient_id+"/prescriptions").orderBy("currentTime", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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