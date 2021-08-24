package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
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
import com.mayurkakade.beingvaidya.data.models.FeedModel;
import com.mayurkakade.beingvaidya.data.models.LearningModel;
import com.mayurkakade.beingvaidya.data.models.LocalLearningModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LearningFragment extends Fragment implements BillingProcessor.IBillingHandler {

    private RecyclerView recyclerView;
    private List<LocalLearningModel> localLearningList;
    private LearningAdapter adapter;
    private EditText search;

    BillingProcessor bp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learning, container, false);

        bp = new BillingProcessor(requireContext(), getString(R.string.google_play_license_key), this);
        bp.initialize();

        recyclerView = view.findViewById(R.id.recyclerView);
        localLearningList = new ArrayList<>();
        adapter = new LearningAdapter(requireContext(), localLearningList, bp,this);
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

        if (getArguments()!=null) {
            String itemId = getArguments().getString("itemId");
            Log.d(TAG, "onCreateView: " + itemId);
            if (itemId != null) {
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
                                    localLearningList.add(new LocalLearningModel(learningModel,false));
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
    }

    private void filterResults(String query) {
        List<LocalLearningModel> filteredList = new ArrayList<>();
        for (LocalLearningModel model : localLearningList) {
            if (model.getLearningModel().getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(model);
            } else {
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                firebaseFirestore.collection("AdminPdfs/"+model.getLearningModel().DocId+"/buyers")
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

    public static final String TAG = "learningFrag";

    private void getPdfData() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("AdminPdfs").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!Objects.requireNonNull(task.getResult()).isEmpty()) {
                        for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("StarLearningItems", Context.MODE_PRIVATE);
                            LearningModel learningModel = doc.getDocument().toObject(LearningModel.class).withId(doc.getDocument().getId());
                            if (sharedPreferences.getBoolean(doc.getDocument().getId(), false)) {
                                localLearningList.add(new LocalLearningModel(learningModel,true));
                            } else {
                                localLearningList.add(new LocalLearningModel(learningModel,false));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Log.d(TAG, "onComplete: " + Objects.requireNonNull(task.getException()).getMessage());
                }
            }
        });
    }

    private MyViewModel mViewModel;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new MyViewModel();
    }

    @Override
    public void onProductPurchased(@NotNull String productId, TransactionDetails details) {
        if (details != null) {
            addProductToUser(productId, details.purchaseInfo.purchaseData.developerPayload);
        }
    }

    public void addProductToUser(String productId, String developerPayload) {
        Map<String,Object> params = new HashMap<>();
        params.put("userId",Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()));

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("AdminPdfs/"+developerPayload+"/buyers").document(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()))
                .set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Map<String, Object> pdfData = new HashMap<>();
                    pdfData.put("productId",productId);
                    pdfData.put("pdfId",developerPayload);

                    firebaseFirestore.collection("Doctors/"+FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/MyPdfs").add(pdfData)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    Toast.makeText(requireContext(), "Purchase Successfully !", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(requireContext(), "Error in purchase!", Toast.LENGTH_SHORT).show();
                    params.put("product_id",productId);
                    firebaseFirestore.collection("failedPurchases").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
                            .set(params);
                }
            }
        });

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {
    }
}