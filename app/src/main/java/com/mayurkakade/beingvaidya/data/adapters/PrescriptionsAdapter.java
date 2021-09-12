package com.mayurkakade.beingvaidya.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.FirebaseImageModel;
import com.mayurkakade.beingvaidya.ui.activities.ImageViewPagerActivity;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionsAdapter extends RecyclerView.Adapter<PrescriptionsAdapter.ViewHolder> {

    Activity context;
    List<FirebaseImageModel> pList;

    public PrescriptionsAdapter(Activity context, List<FirebaseImageModel> pList) {
        this.context = context;
        this.pList = pList;
    }

    @NonNull
    @Override
    public PrescriptionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image_layout,parent,false));
    }


    public static final String TAG = "prescriptionAdapter";
    @Override
    public void onBindViewHolder(@NonNull PrescriptionsAdapter.ViewHolder holder, int position) {
//        Glide.with(context).load(pList.get(position).getDownloadUri()).centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade()).into(holder.iv_prescription);

        holder.iv_prescription.setImageResource(0);
        holder.progressBar.setVisibility(View.VISIBLE);
        Glide.with(context)
                .load(pList.get(position).getDownloadUri())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.iv_prescription.setImageDrawable(resource);
                        return false;
                    }
                })
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.iv_prescription);


    }

    @Override
    public int getItemCount() {
        return pList.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_prescription;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_prescription = itemView.findViewById(R.id.iv_prescription);
            progressBar = itemView.findViewById(R.id.progressBar);
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


                    /*NavController navController = Navigation.findNavController((Activity)context, R.id.patients_nav_host);
                    Bundle args = new Bundle();
                    args.putString("imgUrl",pList.get(position).getDownloadUri());
                    navController.navigate(R.id.action_prescription_to_fullScreenImageFragment2,args);*/
                }
            });

        }
    }
}
