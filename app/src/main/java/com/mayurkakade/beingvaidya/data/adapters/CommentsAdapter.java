package com.mayurkakade.beingvaidya.data.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.CommentModel;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    private final Context context;
    private final List<CommentModel> cList;

    public CommentsAdapter(Context context, List<CommentModel> cList) {
        this.context = context;
        this.cList = cList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_comment_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        getDoctorName(cList.get(position).getDocId(),holder);
        holder.tv_comment.setText(cList.get(position).getCommentText());
        holder.tv_timestamp.setText(getDateFromTimestamp(cList.get(position).getTimestamp()));
        Log.d(TAG, "onBindViewHolder: " + cList.get(position).getCommentText());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.expandableLayout.toggle();
            }
        });
    }

    private String getDateFromTimestamp(Timestamp timestamp) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sfd = new SimpleDateFormat("dd-MMMM-yyyy  HH:mm");
        return sfd.format(timestamp.toDate());
    }

    public static final String TAG = "CommentsAdapter";
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
        Log.d(TAG, "getItemCount: " + cList.size());
        return cList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_doctor_name,tv_comment,tv_timestamp;
        ExpandableLayout expandableLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_doctor_name = itemView.findViewById(R.id.tv_doctor_name);
            tv_comment = itemView.findViewById(R.id.tv_comment);
            tv_timestamp = itemView.findViewById(R.id.tv_timestamp);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);

        }
    }
}
