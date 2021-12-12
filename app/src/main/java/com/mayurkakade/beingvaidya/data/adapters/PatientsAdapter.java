package com.mayurkakade.beingvaidya.data.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.PatientModel;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PatientsAdapter extends RecyclerView.Adapter<PatientsAdapter.ViewHolder> {
    private final Context context;
    private List<PatientModel> pList = new ArrayList<>();
    public static final String TAG = "PatientsAdapter";
    boolean mIsStarted = false;

    public PatientsAdapter(Context context, List<PatientModel> pList, boolean mIsStarted) {
        this.context = context;
        this.pList = pList;
        this.mIsStarted = mIsStarted;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_patient_card,parent,false));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_number.setText(""+(position+1));
        holder.tv_patient_name.setText(""+pList.get(position).getName());
        holder.tv_patient_age.setText(pList.get(position).getAge() + " Years");

        Log.d(TAG, "onBindViewHolder "+position+" :" + "Profile url " + pList.get(position).getProfile_url());
        if (pList.get(position).getProfile_url() != null) {
            if (!pList.get(position).getProfile_url().equals("no_profile") && !pList.get(position).getProfile_url().equals("null"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Glide.with(context).load(pList.get(position).getProfile_url()).into(holder.civ_profile).onLoadFailed(context.getDrawable(R.drawable.doctor_png));
            } else {
                Glide.with(context).load(pList.get(position).getProfile_url()).into(holder.civ_profile).onLoadFailed(AppCompatResources.getDrawable(context, R.drawable.doctor_png));
            }
        }

        if(mIsStarted && pList.get(position).isLocked()){
         holder.ivLock.setVisibility(View.VISIBLE);
        }else{
            holder.ivLock.setVisibility(View.GONE);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsStarted && pList.get(position).isLocked()){
                    return;
                }

                    NavController navController = Navigation.findNavController((Activity) context, R.id.doctors_nav_host);
                    Bundle args = new Bundle();
                    args.putString("argName", pList.get(position).getName());
                    args.putInt("argAge", pList.get(position).getAge());
                    args.putString("argPhoneNo", pList.get(position).getPhone_no());
                    args.putString("argEmail", pList.get(position).getEmail());
                    args.putString("argAddress", pList.get(position).getAddress());
                    args.putString("argDoctorId", pList.get(position).getDoctor_unique_id());
                    args.putBoolean("mIsStarted", mIsStarted);
                    navController.navigate(R.id.action_patientsFragment_to_patientDetailsFragment, args);

            }
        });
    }

    @Override
    public int getItemCount() {
        return pList.size();
    }

    public void filterByQuery(List<PatientModel> filteredList) {
        this.pList = filteredList;
        notifyDataSetChanged();
    }

    public void updateList(List<PatientModel> pList) {
        this.pList = pList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_patient_name;
        TextView tv_patient_age;
        TextView tv_number;
        CircleImageView civ_profile;
        ImageView ivLock;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_patient_name = itemView.findViewById(R.id.tv_patient_name);
            tv_patient_age = itemView.findViewById(R.id.tv_patient_age);
            tv_number = itemView.findViewById(R.id.tv_number);
            civ_profile = itemView.findViewById(R.id.civ_profile);
            ivLock = itemView.findViewById(R.id.ivLock);
        }
    }
}
