package com.mayurkakade.beingvaidya.ui.activities;

import static com.android.billingclient.api.BillingClient.BillingResponseCode.OK;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
//import com.anjlab.android.iab.v3.BillingProcessor;
//import com.anjlab.android.iab.v3.PurchaseInfo;
import com.github.barteksc.pdfviewer.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.Config;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.SubscriptionsAdapter;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;
import com.mayurkakade.beingvaidya.data.models.SubscriptionModel;
import com.mayurkakade.beingvaidya.listener.PurchasePlanCalled;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class SubscriptionsActivity extends AppCompatActivity implements /*BillingProcessor.IBillingHandler*/ PurchasesUpdatedListener {

    public static final String TAG = "billing_subscriptions";
    RecyclerView recyclerView;
    SubscriptionsAdapter adapter;
    List<SubscriptionModel> subscriptionModelList;
    ProgressBar progress_loader;
    SubscriptionModel subscriptionModel;
//    private BillingProcessor bp;
    private TextView tv_patient_limit;
    private TextView tv_my_plan;
    private LinearLayout lin_hold_restore;

    private void openPlaystoreAccount() {
        try {

            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions?"+subscriptionId+ "=$sku&package="+getPackageName())));

        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Cant open the browser", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriptions);
       Button tv_hold = findViewById(R.id.tv_hold);
       Button tv_restore = findViewById(R.id.tv_restore);

        tv_hold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("purchase" , "Hold");
                openPlaystoreAccount();
            }
        });

        tv_restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("purchase" , "Restore");
                openPlaystoreAccount();
            }
        });

        initScreen();
    }

    public void initScreen() {
//        bp = BillingProcessor.newBillingProcessor(this, getString(R.string.google_play_license_key), this);
//        bp.initialize();

       /* try {
            List<BillingHistoryRecord> purchaseHistory = bp.getPurchaseHistory(Constants.PRODUCT_TYPE_SUBSCRIPTION, null);

        } catch (BillingCommunicationException e) {
            e.printStackTrace();
        }*/

        progress_loader = findViewById(R.id.progressBar);
        tv_patient_limit = findViewById(R.id.tv_patient_limit);
        tv_my_plan = findViewById(R.id.tv_my_plan);
        lin_hold_restore = findViewById(R.id.lin_hold_restore);
        recyclerView = findViewById(R.id.recyclerView);
        subscriptionModelList = new ArrayList<>();
        adapter = new SubscriptionsAdapter(this, subscriptionModelList, /*bp,*/ new PurchasePlanCalled() {
            @Override
            public void onPlan(SubscriptionModel model) {
                subscriptionModel = model;
                Constants.SUBSCRIPTION_SKU_ADS_FREE = subscriptionModel.getSubscriptionId() ;
                BillingManager();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        setSubscriptions();
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
        progress_loader.setVisibility(View.VISIBLE);

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
                int quota = Constants.CurrentPlanPatients;
                int validityInMonths = 1;
                switch (subscriptionId) {
                    case Config.Subscriptions.freePlanSubscriptionId:
                        quota = 5;
                        validityInMonths = 1;
                        lin_hold_restore.setVisibility(View.GONE);
                        modelFreePlan.setSelected(true);
                        break;

                    case Config.Subscriptions.yearlyUnlimitedPlanSubscriptionId:
                        quota = -1;
                        validityInMonths = 12;
                        lin_hold_restore.setVisibility(View.VISIBLE);
                        modelYearlyUnlimited.setSelected(true);
                        break;

                    case Config.Subscriptions.halfYearlyUnlimitedPlanSubscriptionId:
                        quota = -1;
                        validityInMonths = 6;
                        lin_hold_restore.setVisibility(View.VISIBLE);
                        modelHalfYearlyUnlimited.setSelected(true);
                        break;

                    case Config.Subscriptions.monthlyUnlimitedPlanSubscriptionId:
                        quota = -1;
                        validityInMonths = 1;
                        lin_hold_restore.setVisibility(View.VISIBLE);
                        modelMonthlyUnlimited.setSelected(true);
                        break;

                    case Config.Subscriptions.monthlyThirtyPlanSubscriptionId:
                        quota = 30;
                        validityInMonths = 1;
                        lin_hold_restore.setVisibility(View.VISIBLE);
                        modelMonthlyThirtyPatients.setSelected(true);
                        break;

                    case Config.Subscriptions.monthlyFifteenPlanSubscriptionId:
                        quota = 15;
                        validityInMonths = 1;
                        lin_hold_restore.setVisibility(View.VISIBLE);
                        modelMonthlyFifteenPatients.setSelected(true);
                        break;
                }
                Log.d(TAG, "2. onSuccess: " + quota);
                Log.d(TAG, "3. onSuccess: " + validityInMonths);
                if(subscriptionId.equals(Config.Subscriptions.freePlanSubscriptionId)){
                    subscriptionModelList.add(modelFreePlan);
                }
                subscriptionModelList.add(modelYearlyUnlimited);
                subscriptionModelList.add(modelHalfYearlyUnlimited);
                subscriptionModelList.add(modelMonthlyUnlimited);
                subscriptionModelList.add(modelMonthlyThirtyPatients);
                subscriptionModelList.add(modelMonthlyFifteenPatients);
                progress_loader.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();

                for (int i = 0; i < subscriptionModelList.size(); i++) {
                    if (subscriptionModelList.get(i).isSelected()) {
//                        String numberOfPatients = subscriptionModelList.get(i).getNumberOfPatients();
//                        tv_patient_limit.setText(numberOfPatients);
                        String priceString = String.valueOf(subscriptionModelList.get(i).getPrice()) + " ₹ /" + subscriptionModelList.get(i).getSubscriptionPeriod();
                        tv_my_plan.setText(priceString);
                        subscriptionId =  subscriptionModelList.get(i).getSubscriptionId();

                    }
                }

            }

            @Override
            public void onFailure(String freeSubscriptionId) {
                progress_loader.setVisibility(View.GONE);
            }
        };

        checkSubscriptionId(doctor_unique_id, returnString);

        getDoctorData(this, doctor_unique_id);

    }
    String subscriptionId = "";

    private void getDoctorData(Context context, String doc_id) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore.collection("Doctors").document(doc_id).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null) {
                                    DoctorModel doctorModel = task.getResult().toObject(DoctorModel.class);
                                    if (doctorModel != null) {
                                        oldTotal = doctorModel.getTotal_patients();
                                        if(oldTotal == -1){
                                            tv_patient_limit.setText("Unlimited");
                                        }else {
                                            tv_patient_limit.setText(""+doctorModel.getTotal_patients());
                                        }

                                    } else {
                                        Toast.makeText(context, "Something went wrong : " + doc_id, Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } else {
                                Log.d(TAG, "onComplete: unsuccessful" + task.getException().getMessage());
                                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }

    }
int oldTotal = 0;


   /* @Override
    public void onBillingInitialized() {
        Log.d(TAG, "onBillingInitialized: ");
        setSubscriptions();
    }*/


    private void setCurrentPlanPatients(int s) {

        Map<String, Object> params = new HashMap<>();
        params.put("current_plan_patients",s);

        /*new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {*/
                FirebaseFirestore.getInstance().collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
//            }
//        },100);
    }

    private void setTotalPatients(int newCount) {
        int total = 0;
        if(oldTotal == -1){
             total = oldTotal;
        }else {
             total = oldTotal +  newCount;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("total_patients",total);

        /*new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {*/
        FirebaseFirestore.getInstance().collection("Doctors").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()).update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: success");
                } else {
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
//            }
//        },100);
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
        int quota = Constants.CurrentPlanPatients;
        switch (subscriptionModel.getSubscriptionId()) {
            case Config.Subscriptions.freePlanSubscriptionId:
                quota = Constants.CurrentPlanPatients;
                break;

            case Config.Subscriptions.yearlyUnlimitedPlanSubscriptionId:
                quota = -1;
                break;

            case Config.Subscriptions.halfYearlyUnlimitedPlanSubscriptionId:
                quota = -1;
                break;

            case Config.Subscriptions.monthlyUnlimitedPlanSubscriptionId:
                quota = -1;
                break;

            case Config.Subscriptions.monthlyThirtyPlanSubscriptionId:
                quota = 30;
                break;

            case Config.Subscriptions.monthlyFifteenPlanSubscriptionId:
                quota = 15;
                break;
        }

        setCurrentPlanPatients(quota);
        setTotalPatients(quota);
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

   /* @Override
    public void onProductPurchased(@NonNull String productId, @Nullable PurchaseInfo details) {
        Log.d(TAG, "onProductPurchased: ");
        onPurchaseDone();
    }*/


   /* @Override
    public void onPurchaseHistoryRestored() {
        Log.d(TAG, "onPurchaseHistoryRestored: ");
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.d(TAG, "onBillingError: ");
    }
*/
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }*/

    /*@Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }*/

    interface ReturnString {
        void onSuccess(String subscriptionId);

        void onFailure(String freeSubscriptionId);
    }

    public Boolean isBillingReady = false;
    public List<SkuDetails> playStoreSkuDetailsList = new ArrayList<>();
    public BillingClient mBillingClient;
    public List<PurchaseHistoryRecord> purchaseHistoryList;
    public PurchaseHistoryRecord currPurchase;
    public void BillingManager() {
        progress_loader.setVisibility(View.VISIBLE);
        mBillingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == OK) {
                    Log.i("TAG", "onBillingSetupFinished() response: " + billingResult.getResponseCode());
                    isBillingReady = true;
                    playStoreSkuDetailsList.clear();
                    getSkuDetails();
                    getHistory();

//                    purchaseProAccount();
                    new Handler(getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progress_loader.setVisibility(View.GONE);
                            if (isBillingReady) {
                                purchaseProAccount();
                            } else {
                                //BillingManager();
                            }
                        }
                    }, 2000);
                } else {
                    isBillingReady = false;
                    progress_loader.setVisibility(View.GONE);
                    Log.w("TAG", "onBillingSetupFinished() error code: " + billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                isBillingReady = false;
                progress_loader.setVisibility(View.GONE);
                Log.w("TAG", "onBillingServiceDisconnected()");
            }
        });
    }
    public void purchaseProAccount() {
        SkuDetails skuDetails = null;
        for (int p = 0; p < playStoreSkuDetailsList.size(); p++) {
            if (playStoreSkuDetailsList.get(p).getSku().equalsIgnoreCase(Constants.SUBSCRIPTION_SKU_ADS_FREE)) {
                skuDetails = playStoreSkuDetailsList.get(p);
            }
        }
        if (skuDetails != null) {
            BillingFlowParams billingFlowParams = BillingFlowParams
                    .newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();
            mBillingClient.launchBillingFlow(this, billingFlowParams);
        }
    }
    private void getSkuDetails() {
        SkuDetailsResponseListener responseListener = new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                if (billingResult.getResponseCode() == OK) {
                    if (!skuDetailsList.isEmpty()) {
                        for (SkuDetails details : skuDetailsList) {
                            if (!playStoreSkuDetailsList.contains(details)) {
                                playStoreSkuDetailsList.add(details);
                                Log.w("TAG", "Got Price: " + details.getPrice());

//                                Toast.makeText(BaseActivity.this, "" + details.getPrice(), Toast.LENGTH_SHORT).show();
//                                T                                UserHelperApp.setPrice(details.getPrice());
                            }

                            Log.w("TAG", "Got a SKU: " + details);
                        }
                    }
                }
            }
        };
        SkuDetailsParams subsSkuDetails = SkuDetailsParams.newBuilder()
                .setSkusList(Arrays.asList(Constants.SUBSCRIPTION_SKU_ADS_FREE))
                .setType(BillingClient.SkuType.INAPP)
                .build();
        mBillingClient.querySkuDetailsAsync(subsSkuDetails, responseListener);
    }


    private void getHistory() {
        purchaseHistoryList = new ArrayList<>();
        PurchaseHistoryResponseListener purchaseHistoryListener = new PurchaseHistoryResponseListener() {
            @Override
            public void onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> purchaseHistoryRecordList) {
                if (billingResult.getResponseCode() == OK && purchaseHistoryRecordList != null && purchaseHistoryRecordList.size() > 0) {
                    purchaseHistoryList.addAll(purchaseHistoryRecordList);
                }
                currPurchase = getCurrentSubscription();
                if (currPurchase != null) {
//                    UserHelperApp.setSubscription(Constants.SUBSCRIPTION_SKU_ADS_FREE);
//                    EventNotifier notifier = NotifierFactory.getInstance().getNotifier(NotifierFactory.EVENT_NOTIFIER_AD_STATUS);
//                    notifier.eventNotify(EventTypes.EVENT_PRO_ACCOUND, null);
                } else {
//                    UserHelperApp.setSubscription(ConstantsApp.SUBSCRIPTION_SKU_BASIC);
                }
            }
        };
        mBillingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, purchaseHistoryListener);
    }

    private PurchaseHistoryRecord getCurrentSubscription() {
        long oldMS = 0;
        PurchaseHistoryRecord purchaseHistoryRecord = null;
        for (int i = 0; i < purchaseHistoryList.size(); i++) {
            String purchase = getDate(purchaseHistoryList.get(i).getPurchaseTime(), "dd/MM/yyyy hh:mm:ss.SSS");
            if (purchaseHistoryList.get(i).getPurchaseTime() > oldMS) {
                oldMS = purchaseHistoryList.get(i).getPurchaseTime();
                purchaseHistoryRecord = purchaseHistoryList.get(i);
            }
            String lastPurchase = getDate(oldMS, "dd/MM/yyyy hh:mm:ss.SSS");
            // Log.e("TAG", "Purchase " + purchaseHistoryList.get(i).getSku() + " on : " + purchase + ", Last Purchase : " + lastPurchase);
        }
        return purchaseHistoryRecord;
    }
    public String getDate(long milliSeconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        progress_loader.setVisibility(View.GONE);
        if (billingResult.getResponseCode() == OK && purchases != null && purchases.size() > 0) {
            Log.d("TAG", "onPurchasesUpdated() response: " + billingResult.getResponseCode());
//            for (int p = 0; p < purchases.size(); p++) {
                handlePurchase(purchases.get(0));
//            }

//            onPurchaseDone();

        } else { // Failed payment or cancel payment
           /* if (billingResult.getResponseCode() == 7) {
                Toast.makeText(requireActivity(), "You are already subscribed with another user. Please login with the subscribed user.", Toast.LENGTH_SHORT).show();
            } else {*/
            Toast.makeText(this, !TextUtils.isEmpty(billingResult.getDebugMessage()) ? billingResult.getDebugMessage() : "Subscription Failed, Try again.", Toast.LENGTH_SHORT).show();
//            }
            isBillingReady = false;
            if(mBillingClient != null){
                mBillingClient.endConnection();
            }
            /*purchaseHistoryList = null;
            currPurchase = null;
            playStoreSkuDetailsList.clear();*/
        }
    }

    @Override
    public void onDestroy() {
        if(mBillingClient != null){
            mBillingClient.endConnection();
        }
        super.onDestroy();
    }

    public void handlePurchase(Purchase purchase) {
        handleConsumableProduct(purchase);
    }
    public void handleConsumableProduct(Purchase purchase) {

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {

                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            // doPurchaseNextStep(purchase);
                            isBillingReady = false;
                            if(mBillingClient != null){
                                mBillingClient.endConnection();
                            }
                            onPurchaseDone();
                            //Toast.makeText(requireActivity(), "doPurchaseNextStep", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
            }else {
                onPurchaseDone();
            }
        }else {
            Toast.makeText(this, "Failed : "+ purchase.getPurchaseState(), Toast.LENGTH_SHORT).show();
        }

    }

}