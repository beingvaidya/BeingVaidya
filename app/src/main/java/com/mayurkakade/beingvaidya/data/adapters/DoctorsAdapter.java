package com.mayurkakade.beingvaidya.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorsAdapter extends RecyclerView.Adapter<DoctorsAdapter.ViewHolder> {

    private Context context;
    private List<DoctorModel> dList;

    public DoctorsAdapter(Context context, List<DoctorModel> dList) {
        this.context = context;
        this.dList = dList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_doctor_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        if (dList.get(position).getProfile_url() != null) {
            if ( !dList.get(position).getProfile_url().equals("no_profile") )
            Glide.with(context).load(dList.get(position).getProfile_url()).into(holder.civ_profile).onLoadFailed(AppCompatResources.getDrawable(context,R.drawable.doctor_png));
        }
        holder.tv_name.setText(dList.get(position).getName());
        holder.tv_bio.setText(dList.get(position).getBio());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController((Activity)context, R.id.doctors_nav_host);
                Bundle args = new Bundle();
                args.putString("doc_id","+91"+dList.get(position).getPhone_no());
                navController.navigate(R.id.action_searchDoctorsFragment_to_doctorsProfileShowFragment,args);

            }
        });

    }

    @Override
    public int getItemCount() {
        return dList.size();
    }

    public void filterByQuery(List<DoctorModel> filteredList) {
        this.dList = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name,tv_bio;
        CircleImageView civ_profile;
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.name);
            tv_bio = itemView.findViewById(R.id.bio);
            civ_profile = itemView.findViewById(R.id.profile_image);
        }
    }
}
