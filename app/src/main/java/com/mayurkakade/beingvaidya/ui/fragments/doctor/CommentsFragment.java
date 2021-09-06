package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.Config;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.AddCommentListener;
import com.mayurkakade.beingvaidya.data.adapters.CommentsAdapter;
import com.mayurkakade.beingvaidya.data.models.CommentModel;
import com.mayurkakade.beingvaidya.notification.MessagingUtils;
import com.mayurkakade.beingvaidya.ui.ProgressUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommentsFragment extends BottomSheetDialogFragment {


    public static final String TAG = "CommentsFragment";
    RecyclerView recyclerView;
    List<CommentModel> cList;
    CommentsAdapter adapter;
    EditText et_comment;
    ImageView iv_send;
    ProgressUtils progressUtils;
    private MyViewModel mViewModel;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        et_comment = view.findViewById(R.id.et_comment);
        iv_send = view.findViewById(R.id.iv_send);
        cList = new ArrayList<>();
        adapter = new CommentsAdapter(getContext(), cList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            Log.d(TAG, "onCreateView: " + getArguments().getString("feedId"));
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new MyViewModel();

        AddCommentListener addCommentListener = new AddCommentListener() {
            @Override
            public void onStart() {
                progressUtils = ProgressUtils.getInstance(getContext());
                progressUtils.showProgress("Please Wait", "Adding Comment");
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: successful");
                cList.add(0, new CommentModel(getArguments().getString("feedId"), et_comment.getText().toString(), Timestamp.now()));
                adapter.notifyDataSetChanged();
                et_comment.getText().clear();
                MessagingUtils messagingUtils = MessagingUtils.getInstance();
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                firebaseFirestore.collection("DoctorsFeed").document(getArguments().getString("feedId")).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().exists()) {
                                        messagingUtils.sendCloudNotification(task.getResult().getString("doctor_id"), FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(), "Your Post Received a comment", true, Config.NOTIFICATION_TYPE_COMMENT, getArguments().getString("feedId"));
                                    }
                                }
                            }
                        });


                progressUtils.hideProgress();
            }

            @Override
            public void onFailure() {
                progressUtils.hideProgress();
            }
        };

        if (getArguments() != null) {
            mViewModel.getCommentsFromServer(adapter, cList, getArguments().getString("feedId"),progressBar);
        }

        iv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(et_comment.getText())) {
                    mViewModel.addFeedComment(getArguments().getString("feedId"), et_comment.getText().toString(), addCommentListener);
                } else {
                    Toast.makeText(getContext(), "Cannot send empty comment !", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}