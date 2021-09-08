package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.MyPostsAdapter;
import com.mayurkakade.beingvaidya.data.models.FeedModel;

import java.util.ArrayList;
import java.util.List;

public class MyPostsFragment extends Fragment {

    FloatingActionButton fab_upload;
    List<FeedModel> fList;
    RecyclerView recyclerView;
    MyPostsAdapter adapter;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_posts, container, false);

        fab_upload = view.findViewById(R.id.fab_upload);
        fList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        adapter = new MyPostsAdapter(container.getContext(),fList);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }

    private MyViewModel mViewModel;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new MyViewModel();
        mViewModel.getMyPostsData(adapter,fList ,progressBar);

    }
}