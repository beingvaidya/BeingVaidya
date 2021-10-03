package com.mayurkakade.beingvaidya.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.LearningModel;
import com.mayurkakade.beingvaidya.data.models.LocalLearningModel;
import com.mayurkakade.beingvaidya.data.models.SubscriptionModel;
import com.mayurkakade.beingvaidya.ui.activities.SubscriptionsActivity;
import com.mayurkakade.beingvaidya.ui.fragments.doctor.LearningFragment;

import java.util.List;

public class LearningAdapter extends RecyclerView.Adapter<LearningAdapter.ViewHolder> {

    public static final String TAG = "LearningAdapter";
    private final Context context;
    private final BillingProcessor bp;
    private final LearningFragment learningFragment;
    public List<LocalLearningModel> localLearningList;
    SharedPreferences.Editor editor;
    private List<LearningModel> learningList;

    public LearningAdapter(Context context, List<LocalLearningModel> localLearningList, BillingProcessor bp, LearningFragment learningFragment) {
        this.context = context;
        this.localLearningList = localLearningList;
        this.bp = bp;
        this.learningFragment = learningFragment;

        SharedPreferences sharedPreferences = context.getSharedPreferences("StarLearningItems", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_pdf_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_description.setText(localLearningList.get(position).getLearningModel().getDescription());
        String price = String.valueOf(localLearningList.get(position).getLearningModel().getPrice()) + " Rs";
        holder.tv_title.setText(localLearningList.get(position).getLearningModel().getTitle());
        holder.tv_price.setText(price);


        if (localLearningList.get(position).isStarred()) {
            Log.d(TAG, "onBindViewHolder: " + localLearningList.get(position).getLearningModel().getTitle() + " : starred");
            holder.iv_star_pdf.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_selected_star));
        } else {
            Log.d(TAG, "onBindViewHolder: " + localLearningList.get(position).getLearningModel().getTitle() + " : not starred");
            holder.iv_star_pdf.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_star_unselected));
        }


        if (!localLearningList.get(position).isPurchase()) {
            holder.bt_preview.setVisibility(View.GONE);
            holder.bt_purchase.setText("Open");
        } else {
            holder.bt_preview.setVisibility(View.VISIBLE);
        }

        /*FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("AdminPdfs/"+ localLearningList.get(position).getLearningModel().DocId+"/buyers")
                .document(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        if (task.getResult().exists()) {
                            holder.bt_preview.setVisibility(View.GONE);
                            holder.bt_purchase.setText("Open");
                            holder.bt_purchase.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openPdfs(localLearningList.get(position).getLearningModel().DocId,true);
                                }
                            });
                        } else {
                            holder.bt_preview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openPdfs(localLearningList.get(position).getLearningModel().DocId,false);
                                }
                            });
                            holder.bt_purchase.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    proceedToPayments(holder, localLearningList.get(position).getLearningModel(), localLearningList.get(position).getLearningModel().DocId);
                                }
                            });
                        }
                    }
                }
            }
        });*/


    }

    private void openPdfs(String docId, boolean isPurchased) {
        NavController navController = Navigation.findNavController(((Activity) context), R.id.doctors_nav_host);
        Bundle args = new Bundle();
        args.putString("docId", docId);
        args.putBoolean("is_purchased", isPurchased);
        navController.navigate(R.id.action_learningFragment_to_pdfListFragment, args);
    }


    private void subscribe(int position, SubscriptionModel subscriptionModel) {
        if (bp.isSubscriptionUpdateSupported()) {
            bp.purchase((Activity) context, subscriptionModel.getSubscriptionId());
        }
    }

    public void proceedToPayments(LearningModel learningModel, String docId) {

//        if (bp.isSubscriptionUpdateSupported()) {
//            bp.purchase((Activity) context, learningModel.getProduct_id());
//        }
        learningFragment.addToSub(learningModel.getProduct_id(), docId);
//        learningFragment.addProductToUser(learningModel.getProduct_id(), docId);
    }

    @Override
    public int getItemCount() {
        return localLearningList.size();
    }

    public void filterByQuery(List<LocalLearningModel> localLearningModelList) {
        this.localLearningList = localLearningModelList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_title, tv_price, tv_description;
        Button bt_purchase, bt_preview;
        ImageView iv_star_pdf;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_title = itemView.findViewById(R.id.tv_title);
            tv_price = itemView.findViewById(R.id.tv_price);
            tv_description = itemView.findViewById(R.id.tv_description);
            bt_purchase = itemView.findViewById(R.id.bt_purchase);
            bt_preview = itemView.findViewById(R.id.bt_preview);
            iv_star_pdf = itemView.findViewById(R.id.iv_star_pdf);

            iv_star_pdf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (localLearningList.get(getLayoutPosition()).isStarred()) {
                        iv_star_pdf.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_star_unselected));
                    } else {
                        iv_star_pdf.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_selected_star));
                    }
                    editor.putBoolean(localLearningList.get(getLayoutPosition()).getLearningModel().DocId, !localLearningList.get(getLayoutPosition()).isStarred());
                    localLearningList.get(getLayoutPosition()).setStarred(!localLearningList.get(getLayoutPosition()).isStarred());
                    editor.apply();
                }
            });


            bt_purchase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!localLearningList.get(getLayoutPosition()).isPurchase()) {
                        openPdfs(localLearningList.get(getLayoutPosition()).getLearningModel().DocId, true);
                    } else {
                        proceedToPayments(localLearningList.get(getLayoutPosition()).getLearningModel(), localLearningList.get(getLayoutPosition()).getLearningModel().DocId);
//                        Intent intentSubscription = new Intent(context, SubscriptionsActivity.class);
//                        context.startActivity(intentSubscription);
                    }
                }
            });

            bt_preview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPdfs(localLearningList.get(getLayoutPosition()).getLearningModel().DocId, false);
                }
            });

        }
    }
}
