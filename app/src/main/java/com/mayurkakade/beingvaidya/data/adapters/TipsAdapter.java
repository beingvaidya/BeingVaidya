package com.mayurkakade.beingvaidya.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;
import com.mayurkakade.beingvaidya.data.models.PatientsCommunityImageModel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TipsAdapter extends RecyclerView.Adapter<TipsAdapter.ViewHolder> {
    private Context context;
    private List<PatientsCommunityImageModel> iList;

    public TipsAdapter(Context context, List<PatientsCommunityImageModel> iList) {
        this.context = context;
        this.iList = iList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_f_image_layout,parent,false));
    }

    public static final String TAG = "PatientsCommunity";

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        if (iList.get(position).getDownloadUri().equals("no_image")) {
            holder.iv_prescription.setVisibility(View.GONE);
        } else {
//            Glide.with(context).load(iList.get(position).getDownloadUri()).into(holder.iv_prescription);

            holder.iv_prescription.setVisibility(View.VISIBLE);
//            Glide.with(context).load(iList.get(position).getDownloadUri()).centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade()).into(holder.iv_prescription);
            holder.iv_prescription.setImageResource(0);
            holder.progressBar.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(iList.get(position).getDownloadUri())
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

        if (iList.get(position).getDescription() != null) {
            if (!iList.get(position).getDescription().equals("")) {
                holder.tv_description.setVisibility(View.VISIBLE);
                holder.tv_description.setText(iList.get(position).getDescription());
            }
        }



        holder.iv_prescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController((Activity)context, R.id.patients_nav_host);
                Bundle args = new Bundle();
                args.putString("imgUrl",iList.get(position).getDownloadUri());
                navController.navigate(R.id.action_tipsFragment_to_fullScreenImageFragment2,args);
            }
        });


        //getDoctorName(iList.get(position).getDoctor_id(),holder);
        holder.tv_doctor_name.setText(iList.get(position).getDoctorName());
        Glide.with(context).load(iList.get(position).getDoctor_profile_photo()).into(holder.civ_profile);


       /* FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors").document(iList.get(position).getDoctor_id()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                DoctorModel doctorModel = task.getResult().toObject(DoctorModel.class);
                                if (doctorModel!= null) {
                                    if (doctorModel.getPhone_no() != null) {
                                        if (!doctorModel.getPhone_no().equals("") || !doctorModel.getPhone_no().equals("no_profile") ) {
                                            Glide.with(context).load(doctorModel.getProfile_url()).into(holder.civ_profile);
                                        }
                                    }
                                }
                            }
                        }
                    }
                });*/



    }

    private void getDoctorName(String doctor_id, ViewHolder holder) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors").document(doctor_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                if (task.getResult().exists()) {
                                    holder.tv_doctor_name.setText(task.getResult().getString("name"));
                                }
                            }
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return iList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_prescription,iv_options;
        TextView tv_description;
        TextView tv_doctor_name;
        ProgressBar progressBar;

        public CircleImageView civ_profile;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
            iv_prescription = itemView.findViewById(R.id.iv_prescription);
            tv_description = itemView.findViewById(R.id.tv_description);
            tv_doctor_name = itemView.findViewById(R.id.tv_doctor_name);
            civ_profile = itemView.findViewById(R.id.civ_profile);
            iv_options = itemView.findViewById(R.id.iv_options);

            civ_profile.setVisibility(View.VISIBLE);
            tv_doctor_name.setVisibility(View.VISIBLE);

        }
    }
}
