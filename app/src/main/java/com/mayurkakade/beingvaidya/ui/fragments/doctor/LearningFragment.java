package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.app.Activity;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseInfo;
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
import com.mayurkakade.beingvaidya.ui.activities.PDFSubscriptionsActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LearningFragment extends Fragment implements BillingProcessor.IBillingHandler {

    public static final String TAG = "learningFrag";
    BillingProcessor bp;
    boolean handler;
    String productId;
    String developerPayload;
    private RecyclerView recyclerView;
    private List<LocalLearningModel> localLearningList;
    private LearningAdapter adapter;
    private EditText search;
    private ProgressBar progress_loader;
    ActivityResultLauncher<Intent> purchaseResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        addProductToUser(productId, developerPayload);
                    }
                }
            });
    private MyViewModel mViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learning, container, false);

//        bp = new BillingProcessor(, getString(R.string.google_play_license_key), this);
//        bp.initialize();

        bp = new BillingProcessor(requireContext(), getString(R.string.google_play_license_key), this);
        bp.initialize();

        /*try {
            List<BillingHistoryRecord> purchaseHistory = bp.getPurchaseHistory(Constants.PRODUCT_TYPE_SUBSCRIPTION, null);

        } catch (BillingCommunicationException e) {
            e.printStackTrace();
        }*/

        recyclerView = view.findViewById(R.id.recyclerView);
        progress_loader = view.findViewById(R.id.progress_loader);
        progress_loader.setVisibility(View.VISIBLE);
        localLearningList = new ArrayList<>();
        adapter = new LearningAdapter(requireContext(), localLearningList, bp, this);
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

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }*/

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

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
        localLearningList.clear();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("AdminPdfs").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new MyViewModel();
    }


    public void addToSub(String mproductId, String mdeveloperPayload) {
        developerPayload = mdeveloperPayload;
        productId = mproductId;

        Intent intent = new Intent(requireContext(), PDFSubscriptionsActivity.class);
        intent.putExtra("code", mproductId);
        purchaseResultLauncher.launch(intent);
//        addProductToUser(productId, developerPayload);

    }

    public void addProductToUser(String productId, String developerPayload) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()));

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("AdminPdfs/" + developerPayload + "/buyers").document(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()))
                .set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
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

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable PurchaseInfo details) {
        Log.d(TAG, "onProductPurchased: ");
        if (details != null) {
            addProductToUser(productId, details.purchaseData.developerPayload);
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {
        Log.d(TAG, "onPurchaseHistoryRestored: ");
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.d(TAG, "onBillingError: ");
        getPdfData();
    }

    @Override
    public void onBillingInitialized() {
        Log.d(TAG, "onBillingInitialized: ");
    }
}