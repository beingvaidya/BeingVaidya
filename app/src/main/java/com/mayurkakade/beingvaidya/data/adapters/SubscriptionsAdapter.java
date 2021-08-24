package com.mayurkakade.beingvaidya.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.SubscriptionModel;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class SubscriptionsAdapter extends RecyclerView.Adapter<SubscriptionsAdapter.ViewHolder> {
    Context context;
    List<SubscriptionModel> subscriptionModelList;
    BillingProcessor bp;

    public SubscriptionsAdapter(Context context, List<SubscriptionModel> subscriptionModelList, BillingProcessor bp) {
        this.context = context;
        this.subscriptionModelList = subscriptionModelList;
        this.bp = bp;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_subscription_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {

        String priceString = String.valueOf(subscriptionModelList.get(position).getPrice()) + " ₹ /" + subscriptionModelList.get(position).getSubscriptionPeriod();
        holder.tv_price.setText(priceString);

        String numberOfPatients = subscriptionModelList.get(position).getNumberOfPatients();
        holder.tv_number_of_patients.setText(numberOfPatients);

        holder.bt_subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribe(position, subscriptionModelList.get(position));
            }
        });

    }

    private void subscribe(int position, SubscriptionModel subscriptionModel) {
        Map<String,Object> params = new HashMap<>();

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        params.put("purchaseYear",currentYear);
        params.put("purchaseMonth",currentMonth);
        params.put("purchaseDay",currentDay);
        params.put("plan_name",subscriptionModel.getSubscriptionId());
        params.put("plan_duration",subscriptionModel.getSubscriptionPeriod());

        String collectionAddress = "Doctors/"+ FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/myPlan";
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(collectionAddress).document("plan_name").set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Log.d("subsTime", "onComplete: failure : " + task.getException().getMessage()  );
                } else {
                    Log.d("subsTime", "onComplete: success : " );
                }
            }
        });
        if (bp.isSubscriptionUpdateSupported()) {
            bp.purchase((Activity) context, subscriptionModel.getSubscriptionId());
        }
    }

    @Override
    public int getItemCount() {
        return subscriptionModelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_price;
        TextView tv_number_of_patients;
        Button bt_subscribe;
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tv_price = itemView.findViewById(R.id.tv_price);
            tv_number_of_patients = itemView.findViewById(R.id.tv_number_of_patients);
            bt_subscribe = itemView.findViewById(R.id.bt_subscribe);
        }
    }
}
