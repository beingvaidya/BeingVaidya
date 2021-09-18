package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.PatientsAdapter;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_starred_patients, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        btn_add_patients = view.findViewById(R.id.btn_add_patients);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PatientsAdapter(requireContext(), pList);
        recyclerView.setAdapter(adapter);


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