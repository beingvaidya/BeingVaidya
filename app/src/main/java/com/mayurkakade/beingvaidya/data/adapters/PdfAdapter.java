package com.mayurkakade.beingvaidya.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.PdfModel;
import com.rajat.pdfviewer.PdfViewerActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.ViewHolder> {
    private Context context;
    private List<PdfModel> pdfUrlList;

    public PdfAdapter(Context context, List<PdfModel> pdfUrlList) {
        this.context = context;
        this.pdfUrlList = pdfUrlList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_pdf_list_card,parent,false));
    }

    public static final String TAG = "PDFADAPTER";

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            getPdf(pdfUrlList.get(position).getDownloadUrl(), holder.pdfView);
        } catch (NullPointerException e) {
            Log.d("pdfViewer", "onBindViewHolder: " + e.getMessage());
        }
        holder.tv_description.setText(pdfUrlList.get(position).getTitle());
        if (pdfUrlList.get(position).isPurchased())
        {
            Log.d(TAG, "onBindViewHolder: " + "isPurchased");
            holder.pdfView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = PdfViewerActivity.Companion.launchPdfFromUrl(           //PdfViewerActivity.Companion.launchPdfFromUrl(..   :: incase of JAVA
                                context,
                                pdfUrlList.get(position).getDownloadUrl(),                                // PDF URL in String format
                                pdfUrlList.get(position).getTitle(),                        // PDF Name/Title in String format
                                "",                  // If nothing specific, Put "" it will save to Downloads
                                 false                    // This param is true by defualt.
                        );
                        context.startActivity(intent);

                    } catch (Exception e) {
                        Log.d("pdfViewer", "onBindViewHolder: " + e.getMessage());
                    }
                }
            });
        } else {
            Log.d(TAG, "onBindViewHolder: " + "NOT Purchased");
        }
    }

    @Override
    public int getItemCount() {
        return pdfUrlList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        PDFView pdfView;
        TextView tv_description;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pdfView = itemView.findViewById(R.id.pdfView);
            tv_description = itemView.findViewById(R.id.tv_description);
        }
    }


    private void getPdf(String url, PDFView pdfView) {
        pdfView.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream ins = new URL(url).openStream();
                    pdfView.fromStream(ins)
                            .enableDoubletap(false)
                            .spacing(0)
                            .pageFitPolicy(FitPolicy.WIDTH)
                            .load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
