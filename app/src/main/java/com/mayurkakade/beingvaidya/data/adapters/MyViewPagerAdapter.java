package com.mayurkakade.beingvaidya.data.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mayurkakade.beingvaidya.R;

import java.util.ArrayList;

public class MyViewPagerAdapter extends RecyclerView.Adapter<MyHolder> {

    private final Activity context;
    private final ArrayList<String> mList;

    public MyViewPagerAdapter(Activity context, ArrayList<String> list) {
        this.context = context;
        this.mList = list;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(context).inflate(R.layout.item_view_pager, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Glide.with(context).load(mList.get(position)).diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade()).into(holder.mMainImage);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
