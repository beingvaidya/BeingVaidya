package com.mayurkakade.beingvaidya.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.LocalStoreModel;
import com.mayurkakade.beingvaidya.data.models.StoreModel;

import java.net.URLEncoder;
import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {
    private Context context;
    private List<LocalStoreModel> localStoreModel;

    public StoreAdapter(Context context, List<LocalStoreModel> itemList) {
        this.context = context;
        this.localStoreModel = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_store_item,parent,false));
    }

    public static final String TAG = "StoreAdapter";

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_title.setText(localStoreModel.get(position).getStoreModel().getTitle());
        holder.tv_description.setText(localStoreModel.get(position).getStoreModel().getDescription());
        holder.tv_price.setText(String.valueOf("₹"+ localStoreModel.get(position).getStoreModel().getPrice()));
        if (localStoreModel.get(position).getStoreModel().getTags() != null) {
            holder.tv_tags.setText(localStoreModel.get(position).getStoreModel().getTags());
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences("StoreWishlistItems", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        if (localStoreModel.get(position).isWishlisted()) {
            holder.ic_wishlist.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_added_to_wishlist));
        } else {
            holder.ic_wishlist.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_add_to_wishlist));
        }


        holder.ic_wishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (localStoreModel.get(position).isWishlisted()) {
                    holder.ic_wishlist.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_add_to_wishlist));
                } else {
                    holder.ic_wishlist.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_added_to_wishlist));
                }
                editor.putBoolean(localStoreModel.get(position).getStoreModel().DocId, !localStoreModel.get(position).isWishlisted());
                localStoreModel.get(position).setWishlisted(!localStoreModel.get(position).isWishlisted());
                editor.apply();
            }
        });

        Glide.with(context)
                .load(localStoreModel.get(position).getStoreModel().getDownloadUrl())
                .into(holder.iv_image);

        holder.iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(((Activity)context),R.id.doctors_nav_host);
                Bundle args = new Bundle();
                args.putString("imgUrl", localStoreModel.get(position).getStoreModel().getDownloadUrl());
                navController.navigate(R.id.action_storeFragment_to_fullScreenImageFragment,args);
            }
        });

        holder.bt_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickWhatsApp("Hi " + " I want to inquire about product " + localStoreModel.get(position).getStoreModel().getTitle(),"91"+ localStoreModel.get(position).getStoreModel().getContact_no());
            }
        });

    }

    private void onClickWhatsApp(String mensaje,String numero){

        try{
            PackageManager packageManager = context.getPackageManager();
            Intent i = new Intent(Intent.ACTION_VIEW);
            String url = "https://api.whatsapp.com/send?phone="+ numero +"&text=" + URLEncoder.encode(mensaje, "UTF-8");
            i.setPackage("com.whatsapp");
            i.setData(Uri.parse(url));
            if (i.resolveActivity(packageManager) != null) {
                context.startActivity(i);
            }else {
                Toast.makeText(context, "WhatsApp not Installed : ", Toast.LENGTH_SHORT)
                        .show();
            }
        } catch(Exception e) {
            Log.e("ERROR WHATSAPP",e.toString());
            Toast.makeText(context, "WhatsApp not Installed : " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }

    }

    @Override
    public int getItemCount() {
        return localStoreModel.size();
    }

    public void filterByQuery(List<LocalStoreModel> filteredList) {
        this.localStoreModel = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_title,tv_price,tv_description,tv_tags;
        Button bt_purchase;
        ImageView iv_image,ic_wishlist;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_price = itemView.findViewById(R.id.tv_price);
            tv_description = itemView.findViewById(R.id.tv_description);
            bt_purchase = itemView.findViewById(R.id.bt_purchase);
            iv_image = itemView.findViewById(R.id.iv_image);
            tv_tags = itemView.findViewById(R.id.tv_tags);
            ic_wishlist = itemView.findViewById(R.id.ic_wishlist);
        }
    }
}
