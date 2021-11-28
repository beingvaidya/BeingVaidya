package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.SavedPostsAdapter;
import com.mayurkakade.beingvaidya.data.models.FeedModel;
import com.mayurkakade.beingvaidya.listener.UnSaveCalled;
import com.mayurkakade.beingvaidya.ui.ProgressUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavedPostsFragment extends Fragment {

    FloatingActionButton fab_upload;
    List<FeedModel> fList;
    RecyclerView recyclerView;
    SavedPostsAdapter adapter;
    ProgressBar progress_loader;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_posts, container, false);
        fab_upload = view.findViewById(R.id.fab_upload);
        progress_loader = view.findViewById(R.id.progress_loader);
        recyclerView = view.findViewById(R.id.recyclerView);
        progress_loader.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        fList = new ArrayList<>();

        adapter = new SavedPostsAdapter(container.getContext(),fList, new UnSaveCalled() {
            @Override
            public void onUnsave(String docId, int position) {
                unsavePost(docId, position);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }

    private MyViewModel mViewModel;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new MyViewModel();
        mViewModel.getSavedPostsData(adapter,fList,progress_loader,recyclerView);

    }

    private void unsavePost(String docId, int position) {
        ProgressUtils progressUtils = ProgressUtils.getInstance(requireContext());
        progressUtils.showProgress("Please wait", "unsaving");
        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> params = new HashMap<>();
        params.put("docId",docId);
        params.put("timestamp",currentTime);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore.collection("Doctors/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + "/savedPosts").document(docId)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            firebaseFirestore.collection("Doctors/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + "/savedPosts").document(docId).delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressUtils.hideProgress();
                                                fList.clear();
                                                mViewModel.getSavedPostsData(adapter,fList,progress_loader,recyclerView);
//                                                fList.remove(position);
//                                                adapter.notifyItemRemoved(position);
//                                                adapter.notifyItemRangeChanged(position, fList.size()-position);
                                            }
                                        }
                                    });
                        }
                    }
                }
            });
        }
    }

}