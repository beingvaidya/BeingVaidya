package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import static com.android.billingclient.api.BillingClient.BillingResponseCode.OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.github.barteksc.pdfviewer.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.LearningAdapter;
import com.mayurkakade.beingvaidya.data.models.LearningModel;
import com.mayurkakade.beingvaidya.data.models.LocalLearningModel;
import com.mayurkakade.beingvaidya.ui.activities.ActivityAuthentication;
import com.mayurkakade.beingvaidya.ui.activities.BaseActivity;
import com.mayurkakade.beingvaidya.ui.activities.SplashScreenActivity;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LearningFragment extends Fragment /*implements BillingProcessor.IBillingHandler*/ implements PurchasesUpdatedListener {
    public static final String TAG = "learningFrag";
    //    BillingProcessor bp;
    boolean handler;
    String productId;
    String developerPayload;
    private RecyclerView recyclerView;
    private List<LocalLearningModel> localLearningList;
    private LearningAdapter adapter;
    private EditText search;
    private ProgressBar progress_loader;


    /* @Override
     public void onDestroy() {
         if (bp != null) {
             bp.release();
         }
         super.onDestroy();
     }
 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learning, container, false);

//        bp = new BillingProcessor(, getString(R.string.google_play_license_key), this);
//        bp.initialize();

//        bp = BillingProcessor.newBillingProcessor(requireContext(), requireActivity().getString(R.string.google_play_license_key), this);
//        bp.initialize();

        /*try {
            List<BillingHistoryRecord> purchaseHistory = bp.getPurchaseHistory(Constants.PRODUCT_TYPE_SUBSCRIPTION, null);

        } catch (BillingCommunicationException e) {
            e.printStackTrace();
        }*/

        recyclerView = view.findViewById(R.id.recyclerView);
        progress_loader = view.findViewById(R.id.progress_loader);
        progress_loader.setVisibility(View.VISIBLE);
        localLearningList = new ArrayList<>();
        adapter = new LearningAdapter(requireContext(), localLearningList, (mproductId, mdeveloperPayload) -> {
            progress_loader.setVisibility(View.VISIBLE);
            developerPayload = mdeveloperPayload;
            productId = mproductId;
            Constants.SUBSCRIPTION_SKU_ADS_FREE = productId ;
            BillingManager();
//            if (((ActivityDoctor)getActivity()).getBillingProcessor().isSubscriptionUpdateSupported()) {
            /*if (bp.isSubscriptionUpdateSupported()) {
                bp.purchase(requireActivity(), productId);
            }*/


//            Intent intent = new Intent(requireContext(), PDFSubscriptionsActivity.class);
//            intent.putExtra("code", mproductId);
//            purchaseResultLauncher.launch(intent);



        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);


        search = view.findViewById(R.id.et_search);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    new Handler(requireActivity().getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String query = s.toString();
                            filterResults(query);
                        }
                    }, 100);
                } else {
                    adapter.filterByQuery(localLearningList);
                }

            }
        });

        if (getArguments() != null) {
            String itemId = getArguments().getString("itemId");
            Log.d(TAG, "onCreateView: " + itemId);
            if (itemId != null) {
                progress_loader.setVisibility(View.GONE);
                filterItemById(itemId);
            } else {
                getPdfData();
            }
        } else {
            getPdfData();
        }

        return view;
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
            mBillingClient.launchBillingFlow(requireActivity(), billingFlowParams);
        }
    }



    private void filterItemById(String itemId) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("AdminPdfs").document(itemId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                if (task.getResult().exists()) {
                                    LearningModel learningModel = Objects.requireNonNull(task.getResult().toObject(LearningModel.class)).withId(task.getResult().getId());
                                    localLearningList.add(new LocalLearningModel(learningModel, false));
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
    }


    /*ActivityResultLauncher<Intent> purchaseResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        addProductToUser(productId, developerPayload);
                    }
                }
            });*/


    /*@Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }*/


    private void filterResults(String query) {
        List<LocalLearningModel> filteredList = new ArrayList<>();
        for (LocalLearningModel model : localLearningList) {
            if (model.getLearningModel().getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(model);
            } else {
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                firebaseFirestore.collection("AdminPdfs/" + model.getLearningModel().DocId + "/buyers")
                        .document(model.getLearningModel().DocId)
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                if (task.getResult().exists()) {
                                    filteredList.add(model);
                                }
                            }
                        }
                    }
                });
            }
        }
        adapter.filterByQuery(filteredList);
    }

    /*public void newMethodPDF(){

        addProductToUser(productId, details.purchaseInfo.purchaseData.developerPayload);

    }*/

    private void getPdfData() {
        progress_loader.setVisibility(View.VISIBLE);
        localLearningList.clear();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("AdminPdfs").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                localLearningList.clear();
                if (isAdded() && getContext() != null) {
                    if (task.isSuccessful()) {
                        if (!Objects.requireNonNull(task.getResult()).isEmpty()) {
                            for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("StarLearningItems", Context.MODE_PRIVATE);
                                LearningModel learningModel = doc.getDocument().toObject(LearningModel.class).withId(doc.getDocument().getId());
                                LocalLearningModel localLearningModel = new LocalLearningModel();

                                if (sharedPreferences.getBoolean(doc.getDocument().getId(), false)) {

                                    localLearningModel = new LocalLearningModel(learningModel, true);
                                    localLearningList.add(0, localLearningModel);
                                } else {
                                    localLearningModel = new LocalLearningModel(learningModel, false);
                                    localLearningList.add(localLearningModel);
                                }

                                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                                LocalLearningModel finalLocalLearningModel = localLearningModel;
                                firebaseFirestore.collection("AdminPdfs/" + localLearningModel.getLearningModel().DocId + "/buyers")
                                        .document(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()))
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult() != null) {
                                                if (task.getResult().exists()) {
                                                    finalLocalLearningModel.setPurchase(true);
                                                } else {
                                                    finalLocalLearningModel.setPurchase(false);
                                                }
                                            } else {
                                                finalLocalLearningModel.setPurchase(false);
                                            }
                                        }
                                    }
                                });


                            }


                            handler = new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (adapter != null && progress_loader != null) {
                                        progress_loader.setVisibility(View.GONE);

                                        Collections.sort(localLearningList, new Comparator() {
                                            @Override
                                            public int compare(Object softDrinkOne, Object softDrinkTwo) {
                                                //use instanceof to verify the references are indeed of the type in question
                                                return ((LocalLearningModel) softDrinkOne).getLearningModel().getTitle().toLowerCase().compareTo(((LocalLearningModel) softDrinkTwo).getLearningModel().getTitle().toLowerCase());
                                            }
                                        });

                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }, 1000);

                        }
                    } else {
                        progress_loader.setVisibility(View.GONE);
                        Log.d(TAG, "onComplete: " + Objects.requireNonNull(task.getException()).getMessage());
                    }
                }
            }
        });
    }


    public void addProductToUser(String productId, String developerPayload) {
        progress_loader.setVisibility(View.VISIBLE);
        Map<String, Object> params = new HashMap<>();
        params.put("userId", Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()));

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("AdminPdfs/" + developerPayload + "/buyers").document(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()))
                .set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progress_loader.setVisibility(View.GONE);
                if (task.isSuccessful()) {

                    Map<String, Object> pdfData = new HashMap<>();
                    pdfData.put("productId", productId);
                    pdfData.put("pdfId", developerPayload);

                    firebaseFirestore.collection("Doctors/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + "/MyPdfs").add(pdfData)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    Toast.makeText(requireContext(), "Purchase Successfully !", Toast.LENGTH_SHORT).show();
                                    getPdfData();
                                }
                            });
                } else {
                    Toast.makeText(requireContext(), "Error in purchase!", Toast.LENGTH_SHORT).show();
                    params.put("product_id", productId);
                    firebaseFirestore.collection("failedPurchases").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
                            .set(params);
                }
            }
        });

    }

   /* @Override
    public void onProductPurchased(@NonNull String productId, @Nullable PurchaseInfo details) {
        Log.d(TAG, "onProductPurchased: ");
//        if (details != null) {
//            addProductToUser(productId, details.purchaseData.developerPayload);
        addProductToUser(productId, developerPayload);
//        }
    }*/

  /*  @Override
    public void onPurchaseHistoryRestored() {
        Log.d(TAG, "onPurchaseHistoryRestored: ");
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.d(TAG, "onBillingError:");
//        getPdfData();
    }

    @Override
    public void onBillingInitialized() {
        Log.d(TAG, "onBillingInitialized: ");
    }*/
  public Boolean isBillingReady = false;
    public List<SkuDetails> playStoreSkuDetailsList = new ArrayList<>();
    public BillingClient mBillingClient;
    public List<PurchaseHistoryRecord> purchaseHistoryList;
    public PurchaseHistoryRecord currPurchase;
    public void BillingManager() {
        progress_loader.setVisibility(View.VISIBLE);
        mBillingClient = BillingClient.newBuilder(requireActivity())
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

                    purchaseProAccount();
                   new Handler(requireActivity().getMainLooper()).postDelayed(new Runnable() {
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
        mBillingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS, purchaseHistoryListener);
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
        } else { // Failed payment or cancel payment
           /* if (billingResult.getResponseCode() == 7) {
                Toast.makeText(requireActivity(), "You are already subscribed with another user. Please login with the subscribed user.", Toast.LENGTH_SHORT).show();
            } else {*/
                Toast.makeText(requireActivity(), !TextUtils.isEmpty(billingResult.getDebugMessage()) ? billingResult.getDebugMessage() : "Subscription Failed, Try again.", Toast.LENGTH_SHORT).show();
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
                            addProductToUser(productId, developerPayload);
                            //Toast.makeText(requireActivity(), "doPurchaseNextStep", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
            }else {
                isBillingReady = false;
                if(mBillingClient != null){
                    mBillingClient.endConnection();
                }
                addProductToUser(productId, developerPayload);
            }


        }else {
            Toast.makeText(requireActivity(), "Failed : "+ purchase.getPurchaseState(), Toast.LENGTH_SHORT).show();
        }

    }




}