package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.PatientReportsDoctorSideAdapter;
import com.mayurkakade.beingvaidya.data.models.FirebaseImageModel;

import java.util.ArrayList;
import java.util.List;

public class PatientsReportsFragment extends Fragment {

    public static final String TAG = "patientReports";
    String doctor_id, patient_id;
    ProgressBar progressBar;
    boolean handler;
    private RecyclerView recyclerView;
    private List<FirebaseImageModel> pList;
    private PatientReportsDoctorSideAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patients_reports, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        pList = new ArrayList<>();
        adapter = new PatientReportsDoctorSideAdapter(requireActivity(), pList);
        recyclerView.setAdapter(adapter);

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavController navController = null;
                try {
                    navController = Navigation.findNavController(requireActivity(), R.id.doctors_nav_host);
                    Bundle args = new Bundle();
                    args.putString("argName", getArguments().getString("argName"));
                    args.putInt("argAge", getArguments().getInt("argAge"));
                    args.putString("argPhoneNo", getArguments().getString("argPhoneNo"));
                    args.putString("argEmail", getArguments().getString("argEmail"));
                    args.putString("argAddress", getArguments().getString("argAddress"));
                    args.putString("argDoctorId", getArguments().getString("argDoctorId"));
                    navController.navigate(R.id.action_patientsReportsFragment_to_patientDetailsFragment, args);
                } catch (Exception e) {
                    e.printStackTrace();
                }



            }
        });

        if (getArguments() != null) {
            patient_id = getArguments().getString("arg_patient_id");
            doctor_id = getArguments().getString("arg_doctor_id");
        }

        getReportsFromServer(pList, adapter);

        return view;
    }

    private void getReportsFromServer(List<FirebaseImageModel> pList, PatientReportsDoctorSideAdapter adapter) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors/" + doctor_id + "/Patients/" + patient_id + "/reports").orderBy("currentTime", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                        if (task.isSuccessful()) {
                            FirebaseImageModel model = doc.getDocument().toObject(FirebaseImageModel.class);
                            pList.add(model);
                            handler = new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (adapter != null && progressBar != null) {
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