package com.mayurkakade.beingvaidya.data.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.mayurkakade.beingvaidya.Config;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.NotificationModel;
import com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor;
import com.mayurkakade.beingvaidya.ui.activities.ActivityPatient;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    Context context;
    List<NotificationModel> notificationList;

    public NotificationAdapter(Context context, List<NotificationModel> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new NotificationAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_notification_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.tv_message.setText(notificationList.get(position).getMsg());
        Intent doctorIntent = new Intent(context, ActivityDoctor.class);
        Intent patientIntent = new Intent(context, ActivityPatient.class);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (Integer.parseInt(String.valueOf(notificationList.get(position).getNotificationType()))) {
                    case -1:
                        break;
                    case Config.NOTIFICATION_TYPE_PDF_ADDED:
                    case Config.NOTIFICATION_TYPE_STORE_ITEM_ADDED:
                    case Config.NOTIFICATION_TYPE_COMMENT:
                    case Config.NOTIFICATION_TYPE_PATIENT_ADDED:
                        doctorIntent.putExtra("notificationType",notificationList.get(position).getNotificationType());
                        doctorIntent.putExtra("docId",notificationList.get(position).getDocId());
                        context.startActivity(doctorIntent);
                        break;
                    case Config.NOTIFICATION_TYPE_BLOGS:
                    case Config.NOTIFICATION_TYPE_TIPS:
                        patientIntent.putExtra("notificationType",notificationList.get(position).getNotificationType());
                        patientIntent.putExtra("docId",notificationList.get(position).getDocId());
                        context.startActivity(patientIntent);
                        break;
                    default:
                        break;
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name,tv_message;
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_message = itemView.findViewById(R.id.tv_message);
        }
    }
}
