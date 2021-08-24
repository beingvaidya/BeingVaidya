package com.mayurkakade.beingvaidya.data.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.FeedModel;
import com.mayurkakade.beingvaidya.ui.ProgressUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SavedPostsAdapter extends RecyclerView.Adapter<SavedPostsAdapter.ViewHolder> {
    Context context;
    List<FeedModel> fList;
    ProgressUtils progressUtils;


    public SavedPostsAdapter(Context context, List<FeedModel> fList) {
        this.context = context;
        this.fList = fList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_feed_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_title.setText(fList.get(position).getTitle());

        getDoctorName(fList.get(position).getDoctor_id(),holder);
        getNumberOfComments(fList.get(position).DocId,holder);
        getNumberOfViews(fList.get(position).DocId,holder);

        holder.tv_caption.setText(fList.get(position).getCaption());

        if (!fList.get(position).getImg_url().equals("no_image")) {
            Glide.with(context).load(fList.get(position).getImg_url()).into(holder.photoView);
        } else {
            holder.photoView.setVisibility(View.GONE);
            holder.circularProgressIndicator.setVisibility(View.GONE);
        }

        holder.photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController((Activity)context, R.id.doctors_nav_host);
                Bundle args = new Bundle();
                args.putString("imgUrl",fList.get(position).getImg_url());
                registerView(fList.get(position).DocId);
                navController.navigate(R.id.action_savedPostsFragment_to_fullScreenImageFragment,args);
            }
        });

        holder.tv_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController((Activity)context, R.id.doctors_nav_host);
                Bundle args = new Bundle();
                args.putString("feedId",fList.get(position).DocId);
                navController.navigate(R.id.action_savedPostsFragment_to_commentsFragment,args);
            }
        });


        holder.tv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerShare(fList.get(position).DocId);
            }
        });

        holder.tv_unsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveDialog(fList.get(position), position);
            }
        });

    }

    private void showRemoveDialog(FeedModel feedModel, int position) {
        ProgressUtils progressUtils = ProgressUtils.getInstance(context);

        AlertDialog deleteDialog = new AlertDialog.Builder(context)
                // set message, title, and icon
                .setTitle("Delete")
                .setMessage("Are you sure want to Unsave ?")

                .setPositiveButton("Unsave", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        unsavePost(feedModel.DocId, position);
                        dialog.dismiss();
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .create();

        deleteDialog.show();
    }

    private void unsavePost(String docId, int position) {
        progressUtils = ProgressUtils.getInstance(context);
        progressUtils.showProgress("Please wait", "unsaving");
        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> params = new HashMap<>();
        params.put("docId",docId);
        params.put("timestamp",currentTime);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore.collection("Doctors/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + "/savedPosts").document(docId)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            firebaseFirestore.collection("Doctors/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + "/savedPosts").document(docId).delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressUtils.hideProgress();
                                                fList.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, fList.size());
                                            }
                                        }
                                    });
                        }
                    }
                }
            });
        }
    }

    private void registerView(String docId) {
        progressUtils = ProgressUtils.getInstance(context);
        progressUtils.showProgress("Please wait", "Sharing");
        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> params = new HashMap<>();
        params.put("docId",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        params.put("timestamp",currentTime);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore.collection("DoctorsFeed/" + docId + "/views").document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                    .set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressUtils.hideProgress();
                    } else {
                        Log.d(TAG, "onComplete: " + task.getException().getMessage());
                    }
                }
            });
        }
    }

    private void registerShare(String docId) {
        progressUtils = ProgressUtils.getInstance(context);
        progressUtils.showProgress("Please wait", "Sharing");
        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> params = new HashMap<>();
        params.put("docId",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        params.put("timestamp",currentTime);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore.collection("DoctorsFeed/" + docId + "/shares").document()
                    .set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressUtils.hideProgress();
                    } else {
                        Log.d(TAG, "onComplete: " + task.getException().getMessage());
                    }
                }
            });
        }
    }


    public static final String TAG = "FEEDADAPTER";

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

    private void getNumberOfComments(String feedId, ViewHolder holder) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("DoctorsFeed/"+feedId+"/comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        holder.tv_comments_show.setText(String.valueOf(task.getResult().size()) + " Comments");
                    } else {
                        holder.tv_comments_show.setText("0 Comments");
                    }
                }
            }
        });
    }

    private void getNumberOfViews(String feedId, ViewHolder holder) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("DoctorsFeed/"+feedId+"/views").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        holder.tv_views.setText(String.valueOf(task.getResult().size()) + " Views");
                    } else {
                        holder.tv_views.setText("0 Views");
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return fList.size();
    }

    public void filterByQuery(List<FeedModel> list) {
        this.fList = list;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView photoView;
        public ProgressBar circularProgressIndicator;
        TextView tv_doctor_name,tv_title,tv_caption, tv_comments_show,tv_views, tv_unsave,tv_comment,tv_share;
        //        ImageView iv_feed;
        @SuppressLint("SetTextI18n")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_doctor_name = itemView.findViewById(R.id.tv_doctor_name);
            tv_title = itemView.findViewById(R.id.tv_disease);
            tv_caption = itemView.findViewById(R.id.tv_caption);
            tv_comments_show = itemView.findViewById(R.id.tv_comments);
            tv_views = itemView.findViewById(R.id.tv_views);
            tv_unsave = itemView.findViewById(R.id.tv_save);
            tv_comment = itemView.findViewById(R.id.tv_comment);
            tv_share = itemView.findViewById(R.id.tv_share);

            tv_unsave.setText("Unsave");

//            iv_feed = itemView.findViewById(R.id.iv_feed);
            photoView = itemView.findViewById(R.id.iv_feed);
            circularProgressIndicator = itemView.findViewById(R.id.c_progress);

        }
    }
}

