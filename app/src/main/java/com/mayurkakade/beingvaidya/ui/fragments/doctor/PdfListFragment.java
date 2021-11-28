package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.Manifest;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.custom.MyDownloadManager;
import com.mayurkakade.beingvaidya.data.adapters.PdfAdapter;
import com.mayurkakade.beingvaidya.data.models.PdfModel;
import com.mayurkakade.beingvaidya.listener.onPDFOpen;
import com.mayurkakade.beingvaidya.ui.activities.PDFViewerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PdfListFragment extends Fragment {

    PdfModel pdfModel;
    int REQUEST_PERMISSIONS = 111;
    Dialog dialog = null;
    private RecyclerView recyclerView;
    private List<PdfModel> pdfUrlsList;
    private PdfAdapter adapter;

    public Boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int read = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
            return read == PackageManager.PERMISSION_GRANTED;
        } else {
            int read = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
            int write = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setDownloadFile(pdfModel);
            } else {
                Toast.makeText(requireActivity(), getString(R.string.str_error_permission), Toast.LENGTH_LONG).show();
            }
        } else {
            boolean read =
                    grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean write =
                    grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED;
            if (read && write) {
                setDownloadFile(pdfModel);
            } else {
                Toast.makeText(requireActivity(), getString(R.string.str_error_permission), Toast.LENGTH_LONG).show();
            }
        }
        return;
    }

    public void takePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        } else {
            requestPermissions(
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_PERMISSIONS
            );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pdf_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        pdfUrlsList = new ArrayList<>();
        adapter = new PdfAdapter(requireContext(), pdfUrlsList, new onPDFOpen() {
            @Override
            public void onPath(PdfModel model) {
                pdfModel = model;
                if (isPermissionGranted()) {
                    setDownloadFile(pdfModel);
                } else {
                    takePermission();
                }

                // Toast.makeText(requireActivity(), ""+model.getDownloadUrl(), Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            getPdfData(getArguments().getString("docId"), getArguments().getBoolean("is_purchased"));
        }

        return view;
    }

    public void setDownloadFile(PdfModel pdfModel) {
        String urlPath = pdfModel.getDownloadUrl();
        String fileName = URLUtil.guessFileName(urlPath, null, null);
//        String fileName = pdfModel.getTitle();
//        pdfModel title = URLUtil.guessFileName(urlPath, null, null);
        ContextWrapper cw = new ContextWrapper(requireActivity());
        File directory = cw.getFilesDir();
        File fileDownload = new File(directory, fileName);

        if (fileDownload.exists()) {
            Uri uri = Uri.fromFile(fileDownload);
            if (uri != null) {
                Intent intent1 = new Intent(requireContext(), PDFViewerActivity.class);
                intent1.putExtra("PDF_URI", uri.toString());
                startActivity(intent1);
                return;
            }
        } else {
            ShowLoader();
            new MyDownloadManager().DownloadFile(requireActivity(), urlPath, fileName, dialog);
        }
    }

    public void ShowLoader() {
        dialog = new Dialog(requireActivity());
        dialog.setContentView(R.layout.layout_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        dialog.show();

    }


    private void getPdfData(String docId, boolean isPurchased) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("AdminPdfs/" + docId + "/files")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                            pdfUrlsList.add(new PdfModel(doc.getDocument().getString("downloadUrl"), doc.getDocument().getString("title"), isPurchased, doc.getDocument().getString("thumbnail")));
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