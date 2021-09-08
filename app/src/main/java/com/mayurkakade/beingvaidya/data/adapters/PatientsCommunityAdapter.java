package com.mayurkakade.beingvaidya.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;
import com.mayurkakade.beingvaidya.data.models.PatientsCommunityImageModel;
import com.mayurkakade.beingvaidya.ui.ProgressUtils;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PatientsCommunityAdapter extends RecyclerView.Adapter<PatientsCommunityAdapter.ViewHolder> {
    private Context context;
    private List<PatientsCommunityImageModel> iList;

    public PatientsCommunityAdapter(Context context, List<PatientsCommunityImageModel> iList) {
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
            Glide.with(context).load(iList.get(position).getDownloadUri()).centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade()).into(holder.iv_prescription);

        }


//        getDoctorName(iList.get(position).getDoctor_id(),holder);
        holder.tv_doctor_name.setText(iList.get(position).getDoctorName());


        Glide.with(context).load(iList.get(position).getDoctorImage()).into(holder.civ_profile);



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




        if (iList.get(position).getDescription() != null) {
            if (!iList.get(position).getDescription().equals("")) {
                holder.tv_description.setVisibility(View.VISIBLE);
                holder.tv_description.setText(iList.get(position).getDescription());
            }
        }

        holder.iv_prescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController((Activity)context, R.id.doctors_nav_host);
                Bundle args = new Bundle();
                args.putString("imgUrl",iList.get(position).getDownloadUri());
                navController.navigate(R.id.action_patientsCommunityFragment_to_fullScreenImageFragment,args);
            }
        });

        if (iList.get(position).getDoctor_id().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber())) {
            holder.iv_options.setVisibility(View.VISIBLE);

            holder.iv_options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context,v);
                    popupMenu.inflate(R.menu.my_posts_more);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menu_delete:
                                    showDeleteDialog(iList.get(position), position);
                                    break;
                            }
                            return true;
                        }
                    });

                    popupMenu.show();
                }
            });

        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                return true;
            }
        });

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

    private void showDeleteDialog(PatientsCommunityImageModel patientsCommunityImageModel, int position) {
        ProgressUtils progressUtils = ProgressUtils.getInstance(context);

        AlertDialog deleteDialog = new AlertDialog.Builder(context)
                .setTitle("Delete")
                .setMessage("Are you sure want to Delete ?")

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        progressUtils.showProgress("Please wait", "Removing");
                        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                        firebaseFirestore.collection("PatientsCommunity").document(patientsCommunityImageModel.DocId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    firebaseFirestore.collection("Doctors/"+ Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()+"/MyPatientsCommunityTips").document(patientsCommunityImageModel.DocId).delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        progressUtils.hideProgress();
                                                        iList.remove(position);
                                                        notifyItemRemoved(position);
                                                        notifyItemRangeChanged(position, iList.size());
                                                    }
                                                }
                                            });
                                }
                            }
                        });

                        dialog.dismiss();
                    }

                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        deleteDialog.show();

        deleteDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public int getItemCount() {
        return iList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_prescription,iv_options;
        TextView tv_description;
        TextView tv_doctor_name;

        public CircleImageView civ_profile;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
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
