package com.mayurkakade.beingvaidya.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.FirebaseImageModel;
import com.mayurkakade.beingvaidya.ui.activities.ImageViewPagerActivity;

import java.util.ArrayList;
import java.util.List;

public class PatientPrescriptionsDoctorSideAdapter extends RecyclerView.Adapter<PatientPrescriptionsDoctorSideAdapter.ViewHolder> {

    Activity context;
    List<FirebaseImageModel> pList;

    public PatientPrescriptionsDoctorSideAdapter(Activity context, List<FirebaseImageModel> pList) {
        this.context = context;
        this.pList = pList;
    }

    @NonNull
    @Override
    public PatientPrescriptionsDoctorSideAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_f_image_layout,parent,false));
    }


    public static final String TAG = "prescriptionAdapter";
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(pList.get(position).getDownloadUri()).centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade()).into(holder.iv_prescription);


        Log.d(TAG, "onBindViewHolder: pList.get(position).getDownloadUri() :" + pList.get(position).getDownloadUri() );

       /* holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController((Activity)context, R.id.doctors_nav_host);
                Bundle args = new Bundle();
                args.putString("imgUrl",pList.get(position).getDownloadUri());
                navController.navigate(R.id.action_patientPrescriptionsFragment_to_fullScreenImageFragment,args);
            }
        });*/

    }

    @Override
    public int getItemCount() {
        return pList.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_prescription;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_prescription = itemView.findViewById(R.id.iv_prescription);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<String> localPath = new ArrayList<>();
                    for (int i = 0 ; i<pList.size() ; i++){
                        localPath.add(pList.get(i).getDownloadUri());
                    }
                    Intent intent = new Intent(context , ImageViewPagerActivity.class);
                    intent.putStringArrayListExtra("List" , localPath);
                    intent.putExtra("Position" , getLayoutPosition());
                    context.startActivity(intent);

                    }
            });

        }
    }
}
