package com.mayurkakade.beingvaidya.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.SliderItem;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SliderAdapterFeed extends
        SliderViewAdapter<SliderAdapterFeed.SliderAdapterVH> {

    private Context context;
    private List<SliderItem> mSliderItems;
    private String DocId = "";


    public SliderAdapterFeed(Context context) {
        this.context = context;
    }

    public void renewItems(List<SliderItem> sliderItems) {
        this.mSliderItems = sliderItems;
        notifyDataSetChanged();
    }

    public void setDocId(String id) {
        this.DocId = id;
    }

    public void deleteItem(int position) {
        this.mSliderItems.remove(position);
        notifyDataSetChanged();
    }

    public void addItem(SliderItem sliderItem) {
        this.mSliderItems.add(sliderItem);
        notifyDataSetChanged();
    }

    public void addItemAll(List<SliderItem> local) {
        this.mSliderItems.clear();
        this.mSliderItems = local;
        notifyDataSetChanged();
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {

        SliderItem sliderItem = mSliderItems.get(position);

        if (sliderItem.getImgUri() != null) {
            Glide.with(context)
                    .load(sliderItem.getImgUri())
                    .fitCenter()
                    .into(viewHolder.imageViewBackground);
        } else {
            Glide.with(context)
                    .load(sliderItem.getUrl())
                    .centerCrop()
                    .into(viewHolder.imageViewBackground);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NavController navController = null;
                try {
                    registerView(DocId);
                    navController = Navigation.findNavController((Activity) context, R.id.doctors_nav_host);
                    Bundle args = new Bundle();
                    args.putString("imgUrl", sliderItem.getUrl());
                    navController.navigate(R.id.action_feedFragment_to_fullScreenImageFragment, args);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void registerView(String docId) {
        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> params = new HashMap<>();
        params.put("docId", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        params.put("timestamp", currentTime);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore.collection("DoctorsFeed/" + docId + "/views").document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                    .set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("registerView", "onComplete: ");
                    } else {
                        Log.d("registerView", "onFail: ");
                    }
                }
            });
        }
    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return mSliderItems.size();
    }

    static class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imageViewBackground;
        ImageView imageGifContainer;
        TextView textViewDescription;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            imageGifContainer = itemView.findViewById(R.id.iv_gif_container);
            textViewDescription = itemView.findViewById(R.id.tv_auto_image_slider);
            this.itemView = itemView;
        }
    }

}