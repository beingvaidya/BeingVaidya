package com.mayurkakade.beingvaidya.ui.activities;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.mayurkakade.beingvaidya.R;

public class PDFViewerActivity extends AppCompatActivity {
    PDFView pdfview = null;
    Uri pdfURL = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer_new);
        pdfview = findViewById(R.id.pdfView);
        String uriPath = getIntent().getExtras().getString("PDF_URI");
        //Log.e("uri: ", uriPath.toString())
        if (uriPath != null) {
            pdfURL = Uri.parse(uriPath);
            // Log.e("uri pdfURL: ", pdfURL.toString())
        }
        pdfview.fromUri(pdfURL)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(false)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true)
                .spacing(0)
                .autoSpacing(false)
                .pageFitPolicy(FitPolicy.WIDTH)
                .fitEachPage(false)
                .pageSnap(false)
                .pageFling(false)
                .nightMode(false)
                .load();
    }


}