package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.PatientsAdapter;
import com.mayurkakade.beingvaidya.data.models.PatientModel;

import java.util.ArrayList;
import java.util.List;


public class NonStarredPatientsFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<PatientModel> pList;
    private PatientsAdapter adapter;
    private EditText search;
    public static final String TAG = "PATIENTS";
    boolean handler;
    public void refreshPatientsList() {
        handler =   new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter!=null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }, 1000);
    }

    public NonStarredPatientsFragment(List<PatientModel> pList) {
        this.pList = pList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_non_starred_patients, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PatientsAdapter(requireContext(),pList);
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