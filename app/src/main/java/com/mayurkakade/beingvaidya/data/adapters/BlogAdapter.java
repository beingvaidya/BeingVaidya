package com.mayurkakade.beingvaidya.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.BlogModel;

import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.ViewHolder> {
    private final Context context;
    private List<BlogModel> bList;

    public BlogAdapter(Context context, List<BlogModel> bList) {
        this.context = context;
        this.bList = bList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_blog_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(bList.get(position).getImg_url()).centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade()).into(holder.imageView);


        holder.title.setText(bList.get(position).getTitle());


        holder.readMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NavController navController = Navigation.findNavController((Activity)context, R.id.patients_nav_host);
                Bundle args = new Bundle();
                args.putString("arg_title",bList.get(position).getTitle());
                args.putString("arg_img",bList.get(position).getImg_url());
                args.putString("arg_content",bList.get(position).getContent());
                navController.navigate(R.id.action_blogsFragment_to_blogDetailsFragment,args);

            }
        });

    }

    @Override
    public int getItemCount() {
        return bList.size();
    }

    public void filterByQuery(List<BlogModel> list) {
        this.bList = list;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title;
        Button readMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.iv_cover);
            title = itemView.findViewById(R.id.tv_title);
            readMore = itemView.findViewById(R.id.bt_read_more);

        }
    }
}
