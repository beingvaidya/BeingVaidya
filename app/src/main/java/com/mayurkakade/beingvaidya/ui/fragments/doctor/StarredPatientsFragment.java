package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.PatientsAdapter;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;
import com.mayurkakade.beingvaidya.data.models.PatientModel;
import com.mayurkakade.beingvaidya.ui.activities.HowToAddPatientsActivity;

import java.util.ArrayList;
import java.util.List;

public class StarredPatientsFragment extends Fragment {

    public static final String TAG = "PATIENTS";
    boolean handler;
    private RecyclerView recyclerView;
    private List<PatientModel> pList;
    private PatientsAdapter adapter;
    private EditText search;
    private Button btn_add_patients;

    public StarredPatientsFragment(List<PatientModel> pList) {
        this.pList = pList;
    }

    public void refreshPatientsList() {
        handler = new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter != null) {
                    int count = 0;
                    for(int i=0 ; i<pList.size() ;i++){
                        if(oldTotal != -1){
                            if(oldTotal == 0 ){
                                pList.get(i).setLocked(true);
                            } else if(count <= oldTotal){
                                pList.get(i).setLocked(false);
                                count = count+1;
                            }else {
                                pList.get(i).setLocked(true);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (pList.size() == 0) {
                        btn_add_patients.setVisibility(View.VISIBLE);
                    } else {
                        btn_add_patients.setVisibility(View.GONE);
                    }
                }
            }
        }, 1000);

    }
    int oldTotal = 0 ;
    private void getDoctorData(Context context, String doc_id) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore.collection("Doctors").document(doc_id).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null) {
                                    DoctorModel doctorModel = task.getResult().toObject(DoctorModel.class);
                                    if (doctorModel != null) {
                                        oldTotal = doctorModel.getTotal_patients();



                                        adapter = new PatientsAdapter(requireContext(), pList,true);
                                        recyclerView.setAdapter(adapter);


                                    } else {
                                        Toast.makeText(context, "Something went wrong : " + doc_id, Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } else {
                                Log.d(TAG, "onComplete: unsuccessful" + task.getException().getMessage());
                                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_starred_patients, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        btn_add_patients = view.findViewById(R.id.btn_add_patients);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));


        getDoctorData(requireContext(),FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        search = view.findViewById(R.id.et_search);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    new Handler(requireActivity().getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String query = s.toString();
                            filterResults(query);
                        }
                    }, 100);
                } else {
                    adapter.filterByQuery(pList);
                }

            }
        });

        btn_add_patients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(requireActivity(), HowToAddPatientsActivity.class);
                requireActivity().startActivity(intent);
            }
        });

        return view;
    }

    private void filterResults(String query) {
        List<PatientModel> filteredList = new ArrayList<>();
        for (PatientModel model : pList) {
            if (model.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(model);
            }
        }
        adapter.filterByQuery(filteredList);
    }


}