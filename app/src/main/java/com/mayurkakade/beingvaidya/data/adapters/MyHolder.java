package com.mayurkakade.beingvaidya.data.adapters;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.chrisbanes.photoview.PhotoView;
import com.mayurkakade.beingvaidya.R;


class MyHolder extends RecyclerView.ViewHolder {


    PhotoView mMainImage;

    public MyHolder(@NonNull View itemView) {
        super(itemView);
        mMainImage = itemView.findViewById(R.id.iv_imgview);

    }
}
