package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.FeedAdapter;
import com.mayurkakade.beingvaidya.data.models.FeedModel;
import com.mayurkakade.beingvaidya.data.models.StoreModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FeedFragment extends Fragment {

    FloatingActionButton fab_upload;
    List<FeedModel> fList;
    RecyclerView recyclerView;
    FeedAdapter adapter;
    EditText search;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        fab_upload = view.findViewById(R.id.fab_upload);
        fList = new ArrayList<>();
        fList.add(new FeedModel("https://homepages.cae.wisc.edu/~ece533/images/arctichare.png",true));
        recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new FeedAdapter(container.getContext(),fList);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(adapter);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        search = view.findViewById(R.id.et_search);


        fab_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.doctors_nav_host);
                navController.navigate(R.id.action_feedFragment_to_uploadFeedItemFragment);
            }
        });

        return view;
    }

    private void filterResults(String query) {
        List<FeedModel> filteredList = new ArrayList<>();
        for (FeedModel model : fList) {
            if (model.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(model);
            }
        }
        adapter.filterByQuery(filteredList);
    }


    private void filterItemById(String itemId) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("DoctorsFeed").document(itemId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                if (task.getResult().exists()) {
                                    FeedModel feedModel = Objects.requireNonNull(task.getResult().toObject(FeedModel.class)).withId(task.getResult().getId());
                                    fList.add(feedModel);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
    }

    public static final String TAG = "FeedFragment";

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MyViewModel mViewModel = new MyViewModel();



        if (getArguments()!=null) {
            String itemId = getArguments().getString("itemId");
            Log.d(TAG, "onCreateView: " + itemId);
            if (itemId != null) {
                filterItemById(itemId);
            } else {
                mViewModel.getAllPostsFeed(adapter,fList);
            }
        } else {
            mViewModel.getAllPostsFeed(adapter,fList);
        }



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
                    adapter.filterByQuery(fList);
                }

            }
        });
    }

}