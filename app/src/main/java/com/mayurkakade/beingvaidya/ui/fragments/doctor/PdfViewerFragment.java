package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//import com.github.barteksc.pdfviewer.PDFView;
import com.mayurkakade.beingvaidya.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class PdfViewerFragment extends Fragment {

//    PDFView pdfView;
    Thread t;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pdf_viewer, container, false);
//        pdfView = view.findViewById(R.id.pdfView);

        if (getArguments()!=null) {
            try {
                loadPdf(getArguments().getString("url"));
            } catch (Exception e){
                Log.d("pdfFragNew", "onCreateView: " + e.getMessage());

            }
        }

        return view;
    }

    private void loadPdf(String url) {
//        pdfView.setVisibility(View.VISIBLE);

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream ins = new URL(url).openStream();
//                    pdfView.fromStream(ins)
//                            .enableDoubletap(false)
//                            .spacing(0)
//                            .pageFitPolicy(FitPolicy.WIDTH)
//                            .load();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
                ;
            }
        });

        t.start();

    }


}