package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.PdfAdapter;
import com.mayurkakade.beingvaidya.data.models.PdfModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PdfListFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<PdfModel> pdfUrlsList;
    private PdfAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pdf_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        pdfUrlsList = new ArrayList<>();
        adapter = new PdfAdapter(requireContext(),pdfUrlsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            getPdfData(getArguments().getString("docId"),getArguments().getBoolean("is_purchased"));
        }

        return view;
    }

    private void getPdfData(String docId, boolean isPurchased) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("AdminPdfs/"+docId+"/files")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult()!=null) {
                        for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                            pdfUrlsList.add(new PdfModel(doc.getDocument().getString("downloadUrl"),doc.getDocument().getString("title"), isPurchased));
                        }
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(requireContext(), "Error : " + Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}