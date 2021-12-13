package com.mayurkakade.beingvaidya.ui.activities;

import static com.android.billingclient.api.BillingClient.BillingResponseCode.OK;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class BaseActivity extends AppCompatActivity implements PurchasesUpdatedListener {
    private static final String TAG = "BaseActivity";
    public Boolean isBillingReady = false;
    public List<SkuDetails> playStoreSkuDetailsList = new ArrayList<>();
    public BillingClient mBillingClient;
    public List<PurchaseHistoryRecord> purchaseHistoryList;
    public PurchaseHistoryRecord currPurchase;
    public BaseActivity activity;

    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        this.activity = this;
    }

   /* public void viewPurchaseDialog() {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment previous = getSupportFragmentManager().findFragmentByTag(PurchaseSelection.class.getName());
        if (previous != null) {
            fragmentTransaction.remove(previous);
        }
        fragmentTransaction.addToBackStack(null);

        PurchaseSelection dialog = new PurchaseSelection();

//        dialog.getActivity().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.show(fragmentTransaction, PurchaseSelection.class.getName());

    }*/

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

    public void BillingManager() {
        mBillingClient = BillingClient.newBuilder(activity)
                .enablePendingPurchases()
                .setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == OK) {
                    Log.i("TAG", "onBillingSetupFinished() response: " + billingResult.getResponseCode());
                    isBillingReady = true;
                    getSkuDetails();
                    getHistory();
                } else {
                    isBillingReady = false;
                    Log.w("TAG", "onBillingSetupFinished() error code: " + billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                isBillingReady = false;
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

    public String getDate(long milliSeconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
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

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == OK && purchases != null && purchases.size() > 0) {
            Log.d("TAG", "onPurchasesUpdated() response: " + billingResult.getResponseCode());
            for (int p = 0; p < purchases.size(); p++) {
                handlePurchase(purchases.get(p));
            }
        } else { // Failed payment or cancel payment
            if (billingResult.getResponseCode() == 7) {
                Toast.makeText(BaseActivity.this, "You are already subscribed with another user. Please login with the subscribed user.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(BaseActivity.this, !TextUtils.isEmpty(billingResult.getDebugMessage()) ? billingResult.getDebugMessage() : "Subscription Failed, Try again.", Toast.LENGTH_SHORT).show();
            }
        }
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
                        }
                    }

                });
            }
        }

    }

  /*  private void doPurchaseNextStep(Purchase purchase) {
        if (purchase.getSkus().get(0).equalsIgnoreCase(Constants.SUBSCRIPTION_SKU_ADS_FREE)) {
//            UserHelperApp.setSubscription(Constants.SUBSCRIPTION_SKU_ADS_FREE);
            add_app_purchase(purchase.getOrderId(), purchase.getPurchaseToken());
        } else {
//            UserHelperApp.setSubscription(ConstantsApp.SUBSCRIPTION_SKU_BASIC);
        }

    }*/

    public void add_app_purchase(String in_app_purchase_id, String purchasetoken) {
        /*MyApplicationClass.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, UserHelperApp.getBase() + "add_app_purchase", str -> {
            try {
                JSONObject obj = new JSONObject(str);
                AppResponseApi appResponseApi = ResponseParser.parseResponseModel(obj);
                Log.e("add_app_purchase: ", str);
                switch (ResponseParser.parseResult(appResponseApi.getStatus())) {
                    case ResponseParser.FAILURE:
                        break;

                    case ResponseParser.SUCCESS:
                        restartApp();
                        break;
                }

            } catch (Throwable t) {
                String sb = "Error: " +
                        t.getMessage();
                Log.e("add_app_purchase: ", sb);
            }


        }, volleyError -> {
            String sb = "Error: " +
                    volleyError.getMessage();
            Log.e("add_app_purchase: ", sb);
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("user_id", "" + UserHelperApp.getUserID());
                hashMap.put("in_app_purchase_id", in_app_purchase_id);
                hashMap.put("purchasetoken", purchasetoken);
                return hashMap;
            }
        });
*/
    }


}
