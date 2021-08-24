package com.mayurkakade.beingvaidya.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.content.Intent;
import android.os.Bundle;
import android.os.TransactionTooLargeException;
import android.util.Log;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingCommunicationException;
import com.anjlab.android.iab.v3.BillingHistoryRecord;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.Config;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.SubscriptionsAdapter;
import com.mayurkakade.beingvaidya.data.models.SubscriptionModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class SubscriptionsActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler{

    RecyclerView recyclerView;
    SubscriptionsAdapter adapter;
    List<SubscriptionModel> subscriptionModelList;

    private BillingProcessor bp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriptions);

        bp = new BillingProcessor(this, getString(R.string.google_play_license_key), this);
        bp.initialize();

        try {
            List<BillingHistoryRecord> purchaseHistory = bp.getPurchaseHistory(Constants.PRODUCT_TYPE_SUBSCRIPTION, null);

        } catch (BillingCommunicationException e) {
            e.printStackTrace();
        }

        recyclerView = findViewById(R.id.recyclerView);
        subscriptionModelList = new ArrayList<>();
        adapter = new SubscriptionsAdapter(this, subscriptionModelList, bp);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    private void setSubscriptions() {

        SubscriptionModel modelFreePlan = new SubscriptionModel(0,"5 Patients", Config.Subscriptions.freePlanSubscriptionId, "Month");
        SubscriptionModel modelYearlyUnlimited = new SubscriptionModel(3499,"Unlimited", Config.Subscriptions.yearlyUnlimitedPlanSubscriptionId, "Year");
        SubscriptionModel modelHalfYearlyUnlimited = new SubscriptionModel(1999,"Unlimited", Config.Subscriptions.halfYearlyUnlimitedPlanSubscriptionId, "6 months");
        SubscriptionModel modelMonthlyUnlimited = new SubscriptionModel(399,"Unlimited", Config.Subscriptions.monthlyUnlimitedPlanSubscriptionId, "Month");
        SubscriptionModel modelMonthlyThirtyPatients = new SubscriptionModel(249,"30 Patients", Config.Subscriptions.monthlyThirtyPlanSubscriptionId, "Month");
        SubscriptionModel modelMonthlyFifteenPatients = new SubscriptionModel(149,"15 Patients", Config.Subscriptions.monthlyFifteenPlanSubscriptionId, "Month");

        subscriptionModelList.add(modelFreePlan);
        subscriptionModelList.add(modelYearlyUnlimited);
        subscriptionModelList.add(modelHalfYearlyUnlimited);
        subscriptionModelList.add(modelMonthlyUnlimited);
        subscriptionModelList.add(modelMonthlyThirtyPatients);
        subscriptionModelList.add(modelMonthlyFifteenPatients);

        adapter.notifyDataSetChanged();

    }



    public static final String TAG = "billing_subscriptions";
    @Override
    public void onBillingInitialized() {
        Log.d(TAG, "onBillingInitialized: ");
        setSubscriptions();
        /*if (bp.isPurchased(Config.Subscriptions.freePlanSubscriptionId)) {
            TransactionDetails subscriptionTransactionDetails = bp.getSubscriptionTransactionDetails(Config.Subscriptions.freePlanSubscriptionId);
            Toast.makeText(this, "FREE", Toast.LENGTH_SHORT).show();
        } else if (bp.isPurchased(Config.Subscriptions.yearlyUnlimitedPlanSubscriptionId)) {
            Toast.makeText(this, "YEARLY", Toast.LENGTH_SHORT).show();
        } else if (bp.isPurchased(Config.Subscriptions.halfYearlyUnlimitedPlanSubscriptionId)) {
            Toast.makeText(this, "HALF_YEARLY", Toast.LENGTH_SHORT).show();
        } else if (bp.isPurchased(Config.Subscriptions.monthlyUnlimitedPlanSubscriptionId)) {
            Toast.makeText(this, "MONTHLY_UNLIMITED", Toast.LENGTH_SHORT).show();
        } else if (bp.isPurchased(Config.Subscriptions.monthlyThirtyPlanSubscriptionId)) {
            Toast.makeText(this, "MONTHLY_THIRTY", Toast.LENGTH_SHORT).show();
        } else if (bp.isPurchased(Config.Subscriptions.monthlyFifteenPlanSubscriptionId)) {
            Toast.makeText(this, "MONTHLY_FIFTEEN", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "NO_SUBSCRIPTION", Toast.LENGTH_SHORT).show();
        }*/

    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Log.d(TAG, "onProductPurchased: ");

        Map<String,Object> params = new HashMap<>();

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        params.put("purchaseYear",currentYear);
        params.put("purchaseMonth",currentMonth);
        params.put("purchaseDay",currentDay);

        String collectionAddress = "Doctors/"+ FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/myPlan";
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(collectionAddress).document("plan_name").set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (!task.isSuccessful()) {
                } else {
                }
            }
        });

    }

    @Override
    public void onPurchaseHistoryRestored() {
        Log.d(TAG, "onPurchaseHistoryRestored: ");
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.d(TAG, "onBillingError: ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        if (bp!=null) {
            bp.release();
        }
        super.onDestroy();
    }
}