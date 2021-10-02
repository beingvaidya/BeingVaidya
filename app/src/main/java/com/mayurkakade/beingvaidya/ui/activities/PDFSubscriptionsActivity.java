package com.mayurkakade.beingvaidya.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anjlab.android.iab.v3.BillingCommunicationException;
import com.anjlab.android.iab.v3.BillingHistoryRecord;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.mayurkakade.beingvaidya.R;

import java.util.List;

public class PDFSubscriptionsActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    public static final String TAG = "billing_PDF";
    private BillingProcessor bp;
     ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        progressBar =findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        bp = new BillingProcessor(this, getString(R.string.google_play_license_key), this);
        bp.initialize();

        try {
            List<BillingHistoryRecord> purchaseHistory = bp.getPurchaseHistory(Constants.PRODUCT_TYPE_SUBSCRIPTION, null);

        } catch (BillingCommunicationException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBillingInitialized() {
        Log.d(TAG, "onBillingInitialized: ");

        if (bp.isSubscriptionUpdateSupported()) {
            progressBar.setVisibility(View.GONE);
            bp.purchase(this, getIntent().getStringExtra("code"));
        }

    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Log.d(TAG, "onProductPurchased: ");


        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK,returnIntent);
        finish();


    }

    @Override
    public void onPurchaseHistoryRestored() {
        Log.d(TAG, "onPurchaseHistoryRestored: ");
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.d(TAG, "onBillingError: ");
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED,returnIntent);
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }
}