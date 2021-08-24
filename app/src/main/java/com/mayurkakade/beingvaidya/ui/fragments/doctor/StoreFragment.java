package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.local.LocalStore;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.StoreAdapter;
import com.mayurkakade.beingvaidya.data.models.FeedModel;
import com.mayurkakade.beingvaidya.data.models.LearningModel;
import com.mayurkakade.beingvaidya.data.models.LocalStoreModel;
import com.mayurkakade.beingvaidya.data.models.StoreModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StoreFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<LocalStoreModel> itemList;
    private StoreAdapter adapter;
    EditText search;
    public static final String TAG = "StoreFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        itemList = new ArrayList<>();
        adapter = new StoreAdapter(requireContext(),itemList);
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
                    adapter.filterByQuery(itemList);
                }

            }
        });

        if (getArguments()!=null) {
            String itemId = getArguments().getString("itemId");
            Log.d(TAG, "onCreateView: " + itemId);
            if (itemId != null) {
                filterItemById(itemId);
            } else {
                getAllPostsBlog(adapter,itemList);
            }
        } else {
            getAllPostsBlog(adapter,itemList);
        }

        return view;
    }

    private void filterResults(String query) {
        List<LocalStoreModel> filteredList = new ArrayList<>();
        for (LocalStoreModel model : itemList) {
            if (model.getStoreModel().getTags() != null) {
                if (model.getStoreModel().getTags().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(model);
                }
            }
        }
        adapter.filterByQuery(filteredList);
    }

    private MyViewModel mViewModel;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new MyViewModel();
    }


    private void filterItemById(String itemId) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("StoreItems").document(itemId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                if (task.getResult().exists()) {
                                    StoreModel storeModel = Objects.requireNonNull(task.getResult().toObject(StoreModel.class)).withId(task.getResult().getId());
                                    itemList.add(new LocalStoreModel(storeModel,false));
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
    }

    public void getAllPostsBlog(StoreAdapter adapter, List<LocalStoreModel> itemList) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("StoreItems").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        if (!task.getResult().isEmpty()) {

                            for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                StoreModel storeModel = doc.getDocument().toObject(StoreModel.class).withId(doc.getDocument().getId());
                                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("StoreWishlistItems", Context.MODE_PRIVATE);
                                if (sharedPreferences.getBoolean(doc.getDocument().getId(), false)) {
                                    itemList.add(new LocalStoreModel(storeModel, true));
                                } else {
                                    itemList.add(new LocalStoreModel(storeModel, false));
                                }
                            }
                            adapter.notifyDataSetChanged();

                        }
                    }
                }
            }
        });
    }
}