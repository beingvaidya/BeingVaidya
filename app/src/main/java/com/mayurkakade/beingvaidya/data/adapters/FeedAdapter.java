package com.mayurkakade.beingvaidya.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.BuildConfig;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;
import com.mayurkakade.beingvaidya.data.models.FeedModel;
import com.mayurkakade.beingvaidya.data.models.SliderItem;
import com.mayurkakade.beingvaidya.ui.ProgressUtils;
import com.smarteist.autoimageslider.SliderView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {
    Context context;
    List<FeedModel> fList;
    ProgressUtils progressUtils;


    public FeedAdapter(Context context, List<FeedModel> fList) {
        this.context = context;
        this.fList = fList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_feed_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (fList.get(position).isBanner()) {
            holder.bannerSliderView.setVisibility(View.VISIBLE);
            holder.ll_feed.setVisibility(View.GONE);
            firebaseFirestore.collection("BannerImages").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                SliderAdapterFeed sliderAdapterFeed = new SliderAdapterFeed(context);
                                sliderAdapterFeed.renewItems(holder.images_uri);
                                for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                    sliderAdapterFeed.addItem(new SliderItem(doc.getDocument().getString("img_url")));
                                }
                                holder.bannerSliderView.setSliderAdapter(sliderAdapterFeed);
                                holder.bannerSliderView.setInfiniteAdapterEnabled(true);
                            }
                        }
                    });




            /*if(fList.get(position).getmSliderItems().size() == 0){
                holder.bannerSliderView.setVisibility(View.GONE);
            }else {*/
//                holder.bannerSliderView.setVisibility(View.VISIBLE);
//                SliderAdapterFeed sliderAdapterFeed = new SliderAdapterFeed(context);
//                sliderAdapterFeed.renewItems(holder.images_uri);
            /*for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                sliderAdapterFeed.addItem(new SliderItem(doc.getDocument().getString("img_url")));
            }*/
//                sliderAdapterFeed.addItemAll(fList.get(position).getmSliderItems());
//                holder.bannerSliderView.setSliderAdapter(sliderAdapterFeed);
//                holder.bannerSliderView.setInfiniteAdapterEnabled(true);
//            }




        } else {
            holder.bannerSliderView.setVisibility(View.GONE);
            holder.ll_feed.setVisibility(View.VISIBLE);
//            getDoctorName(fList.get(position).getDoctor_id(), holder);
            holder.tv_doctor_name.setText(fList.get(position).getDoctorName());
            holder.tv_title.setText(fList.get(position).getTitle());
//            getNumberOfComments(fList.get(position).DocId, holder);
            holder.tv_comments_show.setText(fList.get(position).getComment_show());
//            getNumberOfViews(fList.get(position).DocId, holder);
            holder.tv_views.setText(fList.get(position).getView_show());


            holder.tv_caption.setText(fList.get(position).getCaption());
            holder.civ_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavController navController = Navigation.findNavController((Activity) context, R.id.doctors_nav_host);
                    Bundle args = new Bundle();
                    args.putString("from", "others");
                    args.putString("doc_id", fList.get(position).getDoctor_id());
                    args.putBoolean("hide_number", true);
                    navController.navigate(R.id.action_feedFragment_to_doctorsProfileShowFragment, args);
                }
            });
/*
            firebaseFirestore.collection("Doctors").document(fList.get(position).getDoctor_id()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null) {
                                    DoctorModel doctorModel = task.getResult().toObject(DoctorModel.class);
                                    if (doctorModel != null) {

                                        if (doctorModel.getPhone_no() != null) {
                                            if (!doctorModel.getPhone_no().equals("") || !doctorModel.getPhone_no().equals("no_profile")) {
                                                Glide.with(context).load(doctorModel.getProfile_url()).into(holder.civ_profile);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });*/

            Glide.with(context).load(fList.get(position).getDoctor_profile_photo()).diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade()).into(holder.civ_profile);






/*
            firebaseFirestore.collection("DoctorsFeed/" + fList.get(position).DocId + "/images").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                SliderAdapterFeed sliderAdapterFeed = new SliderAdapterFeed(context);
                                sliderAdapterFeed.renewItems(holder.images_uri);
                                for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                    sliderAdapterFeed.addItem(new SliderItem(doc.getDocument().getString("img_url")));
                                }
                                holder.sliderView.setSliderAdapter(sliderAdapterFeed);
                                holder.sliderView.setInfiniteAdapterEnabled(true);
                            }
                        }
                    });*/


            if(fList.get(position).getmSliderItemsDoctor().size() == 0){
                holder.sliderView.setVisibility(View.GONE);
            }else {
                holder.sliderView.setVisibility(View.VISIBLE);
                SliderAdapterFeed sliderAdapterFeed = new SliderAdapterFeed(context);
                sliderAdapterFeed.renewItems(holder.images_uri);
                sliderAdapterFeed.addItemAll(fList.get(position).getmSliderItemsDoctor());
                holder.sliderView.setSliderAdapter(sliderAdapterFeed);
                holder.sliderView.setInfiniteAdapterEnabled(true);
            }



            holder.photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavController navController = Navigation.findNavController((Activity) context, R.id.doctors_nav_host);
                    Bundle args = new Bundle();
                    args.putString("imgUrl", fList.get(position).getImg_url());
                    registerView(fList.get(position).DocId);
                    navController.navigate(R.id.action_feedFragment_to_fullScreenImageFragment, args);
                }
            });

            holder.tv_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavController navController = Navigation.findNavController((Activity) context, R.id.doctors_nav_host);
                    Bundle args = new Bundle();
                    args.putString("feedId", fList.get(position).DocId);
                    navController.navigate(R.id.action_feedFragment_to_commentsFragment, args);
                }
            });


            holder.tv_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    registerShare(fList.get(position), fList.get(position).DocId);
                }
            });

            holder.tv_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    savePost(fList.get(position).DocId);
                }
            });
        }
    }

    private void savePost(String docId) {
        progressUtils = ProgressUtils.getInstance(context);
        progressUtils.showProgress("Please wait", "Saving Post ...");
        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> params = new HashMap<>();
        params.put("docId",docId);
        params.put("timestamp",currentTime);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore.collection("Doctors/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + "/savedPosts").document(docId)
                    .set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressUtils.hideProgress();
                    } else {
                        Log.d(TAG, "onComplete: " + task.getException().getMessage());
                    }
                }
            });
        }
    }

    private void registerView(String docId) {
        progressUtils = ProgressUtils.getInstance(context);
        progressUtils.showProgress("Please wait", "loading");
        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> params = new HashMap<>();
        params.put("docId",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        params.put("timestamp",currentTime);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore.collection("DoctorsFeed/" + docId + "/views").document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                    .set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressUtils.hideProgress();
                    } else {
                        Log.d(TAG, "onComplete: " + task.getException().getMessage());
                    }
                }
            });
        }
    }

    private void registerShare(FeedModel feedModel, String docId) {
        progressUtils = ProgressUtils.getInstance(context);
        progressUtils.showProgress("Please wait", "Sharing");
        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> params = new HashMap<>();
        params.put("docId",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        params.put("timestamp",currentTime);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore.collection("DoctorsFeed/" + docId + "/shares").document()
                    .set(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    URL url = new URL(feedModel.getImg_url());
                                    Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                    String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image", null);
                                    Uri imageUri = Uri.parse(path);

                                    Intent waIntent = new Intent(Intent.ACTION_SEND);
                                    waIntent.setType("image/*");
                                    waIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                    waIntent.putExtra(Intent.EXTRA_TEXT, "Hey, check this awesome application ... \n\n https://play.google.com/store/apps/details?id="+ BuildConfig.APPLICATION_ID + "\n\n"+feedModel.getCaption());
                                    context.startActivity(Intent.createChooser(waIntent, "Share with"));
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressUtils.hideProgress();
                                        }
                                    });


                                } catch(IOException e) {
                                    Log.e("Error FeedAdapter: " , ""+e.getMessage());
                                    System.out.println(e);
//                                    Toast.makeText(requi, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressUtils.hideProgress();
                                }
                            }
                        }).start();


                    } else {
                        Log.d(TAG, "onComplete: " + task.getException().getMessage());
                    }
                }
            });
        }
    }


    public static final String TAG = "FEEDADAPTER";

    private void getDoctorName(String doctor_id, ViewHolder holder) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors").document(doctor_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                if (task.getResult().exists()) {
                                    holder.tv_doctor_name.setText(task.getResult().getString("name"));
                                }
                            }
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void getNumberOfComments(String feedId, ViewHolder holder) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("DoctorsFeed/"+feedId+"/comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        holder.tv_comments_show.setText(String.valueOf(task.getResult().size()) + " Comments");
                    } else {
                        holder.tv_comments_show.setText("0 Comments");
                    }
                }
            }
        });
    }

    private void getNumberOfViews(String feedId, ViewHolder holder) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("DoctorsFeed/"+feedId+"/views").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        holder.tv_views.setText(String.valueOf(task.getResult().size()) + " Views");
                    } else {
                        holder.tv_views.setText("0 Views");
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return fList.size();
    }

    public void filterByQuery(List<FeedModel> list) {
        this.fList = list;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView photoView;
        public CircleImageView civ_profile;
        public ProgressBar circularProgressIndicator;
        TextView tv_doctor_name,tv_title,tv_caption, tv_comments_show,tv_views,tv_save,tv_comment,tv_share;
        SliderView sliderView;
        SliderView bannerSliderView;
        List<SliderItem> images_uri;
        List<SliderItem> banner_images_uri;
        LinearLayout ll_feed;
//        ImageView iv_feed;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_doctor_name = itemView.findViewById(R.id.tv_doctor_name);
            tv_title = itemView.findViewById(R.id.tv_disease);
            tv_caption = itemView.findViewById(R.id.tv_caption);
            tv_comments_show = itemView.findViewById(R.id.tv_comments);
            tv_views = itemView.findViewById(R.id.tv_views);
            tv_save = itemView.findViewById(R.id.tv_save);
            tv_comment = itemView.findViewById(R.id.tv_comment);
            tv_share = itemView.findViewById(R.id.tv_share);
            civ_profile = itemView.findViewById(R.id.civ_profile);
            sliderView = itemView.findViewById(R.id.imageSlider);
            bannerSliderView = itemView.findViewById(R.id.bannerSlider);
            ll_feed = itemView.findViewById(R.id.layout_feed);
            images_uri = new ArrayList<>();
            banner_images_uri = new ArrayList<>();

//            iv_feed = itemView.findViewById(R.id.iv_feed);
            photoView = itemView.findViewById(R.id.iv_feed);
            circularProgressIndicator = itemView.findViewById(R.id.c_progress);

        }
    }
}
