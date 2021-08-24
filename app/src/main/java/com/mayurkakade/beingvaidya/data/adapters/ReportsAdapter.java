package com.mayurkakade.beingvaidya.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.FirebaseImageModel;

import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder> {

    Context context;
    List<FirebaseImageModel> pList;

    public ReportsAdapter(Context context, List<FirebaseImageModel> pList) {
        this.context = context;
        this.pList = pList;
    }

    @NonNull
    @Override
    public ReportsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_f_image_layout,parent,false));
    }


    public static final String TAG = "prescriptionAdapter";
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(pList.get(position).getDownloadUri()).into(holder.iv_prescription);
        Log.d(TAG, "onBindViewHolder: pList.get(position).getDownloadUri() :" + pList.get(position).getDownloadUri() );

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController((Activity)context, R.id.patients_nav_host);
                Bundle args = new Bundle();
                args.putString("imgUrl",pList.get(position).getDownloadUri());
                navController.navigate(R.id.action_reportsFragment_to_fullScreenImageFragment2,args);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_prescription;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_prescription = itemView.findViewById(R.id.iv_prescription);
        }
    }
}
