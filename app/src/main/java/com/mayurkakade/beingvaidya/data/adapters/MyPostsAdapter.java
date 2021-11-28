package com.mayurkakade.beingvaidya.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.ViewHolder> {
    Context context;
    List<FeedModel> fList;
    ProgressUtils progressUtils;
    public MyPostsAdapter(Context context, List<FeedModel> fList) {
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
        holder.tv_title.setText(fList.get(position).getTitle());

//        getDoctorName(fList.get(position).getDoctor_id(),holder);
        holder.tv_doctor_name.setText(fList.get(position).getDoctorName());
//        getNumberOfComments(fList.get(position).DocId,holder);
//        getNumberOfViews(fList.get(position).DocId,holder);
        holder.tv_comments_show.setText(fList.get(position).getComment_show());
        holder.tv_views.setText(fList.get(position).getView_show());

        holder.tv_caption.setText(fList.get(position).getCaption());


        if (fList.get(position).getDoctor_id().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
            holder.iv_options.setVisibility(View.VISIBLE);
            holder.iv_options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    popupMenu.inflate(R.menu.my_posts_more);
                    popupMenu.show();

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menu_delete:
                                    showDeleteDialog(fList.get(position), position);
                                    break;
                            }
                            return true;
                        }
                    });
                }
            });

        } else {
            holder.iv_options.setVisibility(View.GONE);
        }
      /*  FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors").document(fList.get(position).getDoctor_id()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                DoctorModel doctorModel = task.getResult().toObject(DoctorModel.class);
                                if (doctorModel!= null) {
                                    if (doctorModel.getPhone_no() != null) {
                                        if (!doctorModel.getPhone_no().equals("") || !doctorModel.getPhone_no().equals("no_profile") ) {
                                            Glide.with(context).load(doctorModel.getProfile_url()).centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade()).into(holder.civ_profile);

                                        }
                                    }
                                }
                            }
                        }
                    }
                });
*/
        Glide.with(context).load(fList.get(position).getDoctor_profile_photo()).diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade()).into(holder.civ_profile);


       /* firebaseFirestore.collection("DoctorsFeed/"+fList.get(position).DocId+"/images").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                holder.images_uri.add(new SliderItem(doc.getDocument().getString("img_url")));
                            }
                            SliderAdapterMyPosts sliderAdapterFeed = new SliderAdapterMyPosts(context);
                            holder.sliderView.setSliderAdapter(sliderAdapterFeed);
                            sliderAdapterFeed.renewItems(holder.images_uri);
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


        if (fList.get(position).getImg_url() != null) {
            if (!fList.get(position).getImg_url().equals("no_image")) {
                Glide.with(context).load(fList.get(position).getImg_url()).diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade()).into(holder.photoView);

            } else {
                holder.photoView.setVisibility(View.GONE);
                holder.circularProgressIndicator.setVisibility(View.GONE);
            }
        } else {
            holder.photoView.setVisibility(View.GONE);
            holder.circularProgressIndicator.setVisibility(View.GONE);
        }

        holder.photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController((Activity)context, R.id.doctors_nav_host);
                Bundle args = new Bundle();
                args.putString("imgUrl",fList.get(position).getImg_url());
                registerView(fList.get(position).DocId);
                navController.navigate(R.id.action_myPostsFragment_to_fullScreenImageFragment,args);
            }
        });

        holder.tv_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController((Activity)context, R.id.doctors_nav_host);
                Bundle args = new Bundle();
                args.putString("feedId",fList.get(position).DocId);
                navController.navigate(R.id.action_myPostsFragment_to_commentsFragment,args);
            }
        });


        holder.tv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerShare(fList.get(position),fList.get(position).DocId);
            }
        });

        holder.tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePost(fList.get(position).DocId);
            }
        });

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
                                    String pathImage = "";
                                    if(feedModel.getImg_url() == null || feedModel.getImg_url().equalsIgnoreCase("no_image")){
                                        pathImage = feedModel.getmSliderItemsDoctor().get(0).getUrl();
                                    }else {
                                        pathImage = feedModel.getImg_url();
                                    }
                                    URL url = new URL(pathImage);
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



                      /*  new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
//                                    URL url = new URL(feedModel.getImg_url());
                                    String pathImage = "";
                                    if(feedModel.getImg_url() == null || feedModel.getImg_url().equalsIgnoreCase("no_image")){
                                        pathImage = feedModel.getmSliderItemsDoctor().get(0).getUrl();
                                    }else {
                                        pathImage = feedModel.getImg_url();
                                    }
                                    URL url = new URL(pathImage);
                                    Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                    String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image", null);
                                    Uri imageUri = Uri.parse(path);

                                    Intent waIntent = new Intent(Intent.ACTION_SEND);
                                    waIntent.setType("image/*");
                                    waIntent.putExtra(android.content.Intent.EXTRA_STREAM, imageUri);
                                    waIntent.putExtra(Intent.EXTRA_TEXT, feedModel.getCaption());
                                    context.startActivity(Intent.createChooser(waIntent, "Share with"));


                                } catch(IOException e) {
                                    System.out.println(e);
                                }
                            }
                        }).start();

                        progressUtils.hideProgress();*/
                    } else {
                        Log.d(TAG, "onComplete: " + task.getException().getMessage());
                    }
                }
            });
        }
    }


    public static final String TAG = "FEEDADAPTER";

   /* private void getDoctorName(String doctor_id, ViewHolder holder) {
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
*/
    private void showDeleteDialog(FeedModel feedModel, int position) {
        ProgressUtils progressUtils = ProgressUtils.getInstance(context);

        AlertDialog deleteDialog = new AlertDialog.Builder(context)
                // set message, title, and icon
                .setTitle("Delete")
                .setMessage("Are you sure want to Delete ?")

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        progressUtils.showProgress("Please wait", "Removing");
                        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                        firebaseFirestore.collection("DoctorsFeed").document(feedModel.DocId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    firebaseFirestore.collection("Doctors/"+ Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()+"/MyFeeds").document(feedModel.DocId).delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        progressUtils.hideProgress();
                                                        fList.remove(position);
                                                        notifyItemRemoved(position);
                                                        notifyItemRangeChanged(position, fList.size());
                                                    }
                                                }
                                            });
                                }
                            }
                        });

                        dialog.dismiss();
                    }

                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .create();

        deleteDialog.show();
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
        ImageView iv_options;
        SliderView sliderView;
        List<SliderItem> images_uri;
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
            iv_options = itemView.findViewById(R.id.iv_options);
            sliderView = itemView.findViewById(R.id.imageSlider);
            images_uri = new ArrayList<>();

//            iv_feed = itemView.findViewById(R.id.iv_feed);
            photoView = itemView.findViewById(R.id.iv_feed);
            circularProgressIndicator = itemView.findViewById(R.id.c_progress);

        }
    }
}
