package com.mayurkakade.beingvaidya.data.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.custom.SquareImageView;
import com.mayurkakade.beingvaidya.data.models.PdfModel;
import com.mayurkakade.beingvaidya.listener.onPDFOpen;
import com.rajat.pdfviewer.PdfViewerActivity;
import com.skyhope.showmoretextview.ShowMoreTextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.ViewHolder> {
    public static final String TAG = "PDFADAPTER";
    private Context context;
    private List<PdfModel> pdfUrlList;
    onPDFOpen listener ;

    public PdfAdapter(Context context, List<PdfModel> pdfUrlList , onPDFOpen callBack) {
        this.context = context;
        this.pdfUrlList = pdfUrlList;
        this.listener = callBack;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_pdf_list_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            // getPdf(pdfUrlList.get(position).getDownloadUrl(), holder.pdfView);
            Glide.with(context)
                    .load(pdfUrlList.get(position).getThumbnail()).into(holder.pdfView);
        } catch (NullPointerException e) {
            Log.d("pdfViewer", "onBindViewHolder: " + e.getMessage());
        }
//        holder.tv_description.setText(pdfUrlList.get(position).getTitle());
        holder.text_view_show_more.setText(pdfUrlList.get(position).getTitle());

    }

    @Override
    public int getItemCount() {
        return pdfUrlList.size();
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        //        PDFView pdfView;
        SquareImageView pdfView;
        //        TextView tv_description;
        ShowMoreTextView text_view_show_more;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pdfView = itemView.findViewById(R.id.pdfView);
//            tv_description = itemView.findViewById(R.id.tv_description);
            text_view_show_more = itemView.findViewById(R.id.text_view_show_more);
            text_view_show_more.setShowingLine(3);
            text_view_show_more.addShowMoreText("Show More");
            text_view_show_more.addShowLessText("Less");
            text_view_show_more.setShowMoreColor(Color.RED); // or other color
            text_view_show_more.setShowLessTextColor(Color.RED); // or other color

            pdfView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pdfUrlList.get(getLayoutPosition()).isPurchased()) {
                        listener.onPath(pdfUrlList.get(getLayoutPosition()));
                        /*Log.e("Path: ", "" + pdfUrlList.get(getLayoutPosition()).getDownloadUrl());
                        Log.d(TAG, "onBindViewHolder: " + "isPurchased");
                        try {
                            Intent intent = PdfViewerActivity.Companion.launchPdfFromUrl(           //PdfViewerActivity.Companion.launchPdfFromUrl(..   :: incase of JAVA
                                    context,
                                    pdfUrlList.get(getLayoutPosition()).getDownloadUrl(),                                // PDF URL in String format
//                                    "http://www.africau.edu/images/default/sample.pdf",                                // PDF URL in String format
                                    pdfUrlList.get(getLayoutPosition()).getTitle(),                        // PDF Name/Title in String format
                                    "",                  // If nothing specific, Put "" it will save to Downloads
                                    false                    // This param is true by defualt.
                            );
                            context.startActivity(intent);


                        } catch (Exception e) {
                            Log.d("pdfViewer", "onBindViewHolder: " + e.getMessage());
                        }*/
                    } else {
                        Log.d(TAG, "onBindViewHolder: " + "NOT Purchased");
                    }


                }
            });


        }
    }
}
