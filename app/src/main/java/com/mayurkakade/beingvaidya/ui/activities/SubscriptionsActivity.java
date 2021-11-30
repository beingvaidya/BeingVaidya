package com.mayurkakade.beingvaidya.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.anjlab.android.iab.v3.BillingHistoryRecord;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.PurchaseInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.Config;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.SubscriptionsAdapter;
import com.mayurkakade.beingvaidya.data.models.SubscriptionModel;
import com.mayurkakade.beingvaidya.listener.PurchasePlanCalled;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class SubscriptionsActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    public static final String TAG = "billing_subscriptions";
    RecyclerView recyclerView;
    SubscriptionsAdapter adapter;
    List<SubscriptionModel> subscriptionModelList;
    ProgressBar progressBar;
    SubscriptionModel subscriptionModel;
    private BillingProcessor bp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriptions);

        initScreen();
    }
    public void initScreen(){
        bp = new BillingProcessor(this, getString(R.string.google_play_license_key), this);
        bp.initialize();

       /* try {
            List<BillingHistoryRecord> purchaseHistory = bp.getPurchaseHistory(Constants.PRODUCT_TYPE_SUBSCRIPTION, null);

        } catch (BillingCommunicationException e) {
            e.printStackTrace();
        }*/

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        subscriptionModelList = new ArrayList<>();
        adapter = new SubscriptionsAdapter(this, subscriptionModelList, bp, new PurchasePlanCalled() {
            @Override
            public void onPlan(SubscriptionModel model) {
                subscriptionModel = model;
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    public void checkSubscriptionId(String doctor_id, ReturnString returnString) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors/" + doctor_id + "/myPlan").document("plan_name")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (Objects.requireNonNull(task.getResult()).exists()) {
                        returnString.onSuccess(task.getResult().getString("plan_name"));
                    } else {
                        returnString.onSuccess(Config.Subscriptions.freePlanSubscriptionId);
                    }
                } else {
                    returnString.onSuccess(Config.Subscriptions.freePlanSubscriptionId);
                }
            }
        });
    }

    private void setSubscriptions() {
        progressBar.setVisibility(View.VISIBLE);

        String doctor_unique_id = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        ReturnString returnString = new ReturnString() {
            @Override
            public void onSuccess(String subscriptionId) {
                subscriptionModelList.clear();

                SubscriptionModel modelFreePlan = new SubscriptionModel(0, "5 Patients", Config.Subscriptions.freePlanSubscriptionId, "Month");
                SubscriptionModel modelYearlyUnlimited = new SubscriptionModel(3499, "Unlimited", Config.Subscriptions.yearlyUnlimitedPlanSubscriptionId, "Year");
                SubscriptionModel modelHalfYearlyUnlimited = new SubscriptionModel(1999, "Unlimited", Config.Subscriptions.halfYearlyUnlimitedPlanSubscriptionId, "6 months");
                SubscriptionModel modelMonthlyUnlimited = new SubscriptionModel(399, "Unlimited", Config.Subscriptions.monthlyUnlimitedPlanSubscriptionId, "Month");
                SubscriptionModel modelMonthlyThirtyPatients = new SubscriptionModel(249, "30 Patients", Config.Subscriptions.monthlyThirtyPlanSubscriptionId, "Month");
                SubscriptionModel modelMonthlyFifteenPatients = new SubscriptionModel(149, "15 Patients", Config.Subscriptions.monthlyFifteenPlanSubscriptionId, "Month");

                modelFreePlan.setSelected(false);
                modelYearlyUnlimited.setSelected(false);
                modelHalfYearlyUnlimited.setSelected(false);
                modelMonthlyUnlimited.setSelected(false);
                modelMonthlyThirtyPatients.setSelected(false);
                modelMonthlyFifteenPatients.setSelected(false);


                Log.d(TAG, "1 : onSuccess: " + subscriptionId);
                int quota = 5;
                int validityInMonths = 1;
                switch (subscriptionId) {
                    case Config.Subscriptions.freePlanSubscriptionId:
                        quota = 5;
                        validityInMonths = 1;
                        modelFreePlan.setSelected(true);
                        break;

                    case Config.Subscriptions.yearlyUnlimitedPlanSubscriptionId:
                        quota = -1;
                        validityInMonths = 12;
                        modelYearlyUnlimited.setSelected(true);
                        break;

                    case Config.Subscriptions.halfYearlyUnlimitedPlanSubscriptionId:
                        quota = -1;
                        validityInMonths = 6;
                        modelHalfYearlyUnlimited.setSelected(true);
                        break;

                    case Config.Subscriptions.monthlyUnlimitedPlanSubscriptionId:
                        quota = -1;
                        validityInMonths = 1;
                        modelMonthlyUnlimited.setSelected(true);
                        break;

                    case Config.Subscriptions.monthlyThirtyPlanSubscriptionId:
                        quota = 30;
                        validityInMonths = 1;
                        modelMonthlyThirtyPatients.setSelected(true);
                        break;

                    case Config.Subscriptions.monthlyFifteenPlanSubscriptionId:
                        quota = 15;
                        validityInMonths = 1;
                        modelMonthlyFifteenPatients.setSelected(true);
                        break;
                }
                Log.d(TAG, "2. onSuccess: " + quota);
                Log.d(TAG, "3. onSuccess: " + validityInMonths);
                subscriptionModelList.add(modelFreePlan);
                subscriptionModelList.add(modelYearlyUnlimited);
                subscriptionModelList.add(modelHalfYearlyUnlimited);
                subscriptionModelList.add(modelMonthlyUnlimited);
                subscriptionModelList.add(modelMonthlyThirtyPatients);
                subscriptionModelList.add(modelMonthlyFifteenPatients);
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String freeSubscriptionId) {
                progressBar.setVisibility(View.GONE);
            }
        };

        checkSubscriptionId(doctor_unique_id, returnString);

    }

    @Override
    public void onBillingInitialized() {
        Log.d(TAG, "onBillingInitialized: ");
        setSubscriptions();
    }



    private void onPurchaseDone() {
        Map<String, Object> params = new HashMap<>();

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        params.put("purchaseYear", currentYear);
        params.put("purchaseMonth", currentMonth);
        params.put("purchaseDay", currentDay);
        params.put("plan_name", subscriptionModel.getSubscriptionId());
        params.put("plan_duration", subscriptionModel.getSubscriptionPeriod());

        String collectionAddress = "Doctors/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + "/myPlan";
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(collectionAddress).document("plan_name").set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Log.d("subsTime", "onComplete: failure : " + task.getException().getMessage());
                } else {
                    Log.d("subsTime", "onComplete: success : ");
                    initScreen();
                }
            }
        });
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable PurchaseInfo details) {
        Log.d(TAG, "onProductPurchased: ");
        onPurchaseDone();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        Log.d(TAG, "onPurchaseHistoryRestored: ");
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.d(TAG, "onBillingError: ");
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }*/

    @Override
    protected void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    interface ReturnString {
        void onSuccess(String subscriptionId);

        void onFailure(String freeSubscriptionId);
    }
}