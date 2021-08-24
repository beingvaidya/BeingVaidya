package com.mayurkakade.beingvaidya.ui.fragments.patient;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.BlogAdapter;
import com.mayurkakade.beingvaidya.data.models.BlogModel;

import java.util.ArrayList;
import java.util.List;

public class BlogsFragment extends Fragment {

    List<BlogModel> bList;
    RecyclerView recyclerView;
    BlogAdapter adapter;
    Button bt_lifestyle,bt_disease,bt_remedy;
    boolean bool_Lifestyle = false, bool_Disease = false, bool_Remedy = false;

    public static final String TAG = "BLOGSDEBUG";

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart: ");

    }


    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blogs, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        bList = new ArrayList<>();
        adapter = new BlogAdapter(requireContext(),bList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        bt_lifestyle = view.findViewById(R.id.bt_lifestyle);
        bt_disease = view.findViewById(R.id.bt_disease);
        bt_remedy = view.findViewById(R.id.bt_remedy);

        setBlogTags();
        getAllPostsBlog(adapter,bList);

        return view;
    }

    private void initLifecycleBlogs() {

        bool_Lifestyle = true;
        bool_Disease = false;
        bool_Remedy = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bt_lifestyle.setBackgroundColor(requireContext().getColor(R.color.green_700));
        } else {
            bt_lifestyle.setBackgroundColor(Color.GREEN);
        }
        bt_disease.setBackgroundColor(Color.GRAY);
        bt_remedy.setBackgroundColor(Color.GRAY);

        filterResults();
    }


    private void filterResults() {
        List<BlogModel> filteredList = new ArrayList<>();
        String tag = "general";
        if (bool_Lifestyle) {
            tag = bt_lifestyle.getText().toString();
        } else if (bool_Disease) {
            tag = bt_disease.getText().toString();
        } else if (bool_Remedy) {
            tag = bt_remedy.getText().toString();
        }

        Log.d("Tag_Blogs", "filterResults: " + tag);
        for (BlogModel model : bList) {
            if (model.getTag() != null) {



                if (model.getTag().equals(tag)) {
                    filteredList.add(model);
                }


            }
        }
        if (tag.equals("general")) {
            adapter.filterByQuery(bList);
        } else {
            adapter.filterByQuery(filteredList);
        }
    }

    private void setBlogTags() {
        bt_lifestyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bool_Lifestyle = !bool_Lifestyle;
                bool_Disease = false;
                bool_Remedy = false;

                if (bool_Lifestyle) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        bt_lifestyle.setBackgroundColor(requireContext().getColor(R.color.green_700));
                    } else {
                        bt_lifestyle.setBackgroundColor(Color.GREEN);
                    }
                } else {
                    bt_lifestyle.setBackgroundColor(Color.GRAY);
                }
                bt_disease.setBackgroundColor(Color.GRAY);
                bt_remedy.setBackgroundColor(Color.GRAY);

                filterResults();
            }
        });

        bt_disease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bool_Lifestyle = false;
                bool_Disease = !bool_Disease;
                bool_Remedy = false;

                bt_lifestyle.setBackgroundColor(Color.GRAY);
                if (bool_Disease) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        bt_disease.setBackgroundColor(requireContext().getColor(R.color.green_700));
                    } else {
                        bt_disease.setBackgroundColor(Color.GREEN);
                    }
                } else {
                    bt_disease.setBackgroundColor(Color.GRAY);
                }
                bt_remedy.setBackgroundColor(Color.GRAY);

                filterResults();
            }
        });

        bt_remedy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bool_Lifestyle = false;
                bool_Disease = false;
                bool_Remedy = !bool_Remedy;

                bt_lifestyle.setBackgroundColor(Color.GRAY);
                bt_disease.setBackgroundColor(Color.GRAY);
                if (bool_Remedy) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        bt_remedy.setBackgroundColor(requireContext().getColor(R.color.green_700));
                    } else {
                        bt_remedy.setBackgroundColor(Color.GREEN);
                    }
                } else {
                    bt_remedy.setBackgroundColor(Color.GRAY);
                }

                filterResults();
            }
        });
    }

    public void getAllPostsBlog(BlogAdapter adapter, List<BlogModel> bList) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("AdminBlog").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        if (!task.getResult().isEmpty()) {

                            for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                BlogModel feedModel = doc.getDocument().toObject(BlogModel.class).withId(doc.getDocument().getId());
                                bList.add(feedModel);
                            }
                            adapter.notifyDataSetChanged();
                            initLifecycleBlogs();
                        }
                    }
                } else {
                }
            }
        });
    }
}