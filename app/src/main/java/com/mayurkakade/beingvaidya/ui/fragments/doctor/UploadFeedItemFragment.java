package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.UploadMultipleImagesInterface;
import com.mayurkakade.beingvaidya.data.adapters.SliderAdapterFeed;
import com.mayurkakade.beingvaidya.data.models.SliderItem;
import com.mayurkakade.beingvaidya.ui.ProgressUtils;
import com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;

public class UploadFeedItemFragment extends Fragment {

    EditText et_name,et_details;
    Button bt_upload,bt_select;
    CardView cardView;
    UploadMultipleImagesInterface uploadToStorageInterface;
//    Uri image_uri = null;
    List<SliderItem> images_uri;

    public static final String TAG = "UPLOADFEED";
    SliderView sliderView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_feed_item, container, false);

        images_uri = new ArrayList<>();
        sliderView = view.findViewById(R.id.imageSlider);

        et_name = view.findViewById(R.id.et_disease_name);
        et_details = view.findViewById(R.id.et_caption);
        bt_upload = view.findViewById(R.id.bt_upload);
        bt_select = view.findViewById(R.id.bt_select_image);
        cardView = view.findViewById(R.id.image);


        return view;
    }

    private MyViewModel mViewModel;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new MyViewModel();

        List<String> urlList = new ArrayList<>();

        final ProgressUtils progressUtils = ProgressUtils.getInstance(requireContext());
        uploadToStorageInterface = new UploadMultipleImagesInterface() {
            @Override
            public void onStart() {
                Log.d(TAG, "onStart: ");
            }

            @Override
            public void onProgress(int progress) {
                Log.d(TAG, "onProgress: " + (progress+1) + " of " + numOfImages);
            }

            @Override
            public void onSuccess(List<String> downloadUrls, String field) {
                Log.d(TAG, "onSuccess: size :" + downloadUrls.size());


            }

            @Override
            public void addUrl(String url, int size) {
                Log.d("uploadTimer", "addUrl: " + url);
                urlList.add(url);
                if (images_uri.size() == urlList.size()) {
                    Log.d("uploadTimer", "finalUpdate: ");
                    mViewModel.addMultipleFeedImages(et_name.getText().toString(), et_details.getText().toString(), urlList);
                    progressUtils.hideProgress();
                    requireActivity().onBackPressed();
                  //  Navigation.findNavController(requireActivity(),R.id.doctors_nav_host).navigateUp();
                }
            }


            @Override
            public void onFailure() {
                progressUtils.hideProgress();
                Toast.makeText(requireContext(), "Upload Unsuccessful", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: " + "failed");
            }
        };




        bt_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(et_name.getText())) {
                    if (!TextUtils.isEmpty(et_details.getText())) {
                        mViewModel.getImageFeed(UploadFeedItemFragment.this,requireActivity());
                    } else {
                        Toast.makeText(requireContext(), "Please Enter Details", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Please Enter Title", Toast.LENGTH_SHORT).show();
                }
            }
        });


        bt_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (images_uri != null) {
                    if (images_uri.size() > 0) {
                        progressUtils.showProgress("Please wait", "uploading Image");
                        mViewModel.uploadImages(images_uri, uploadToStorageInterface, "feedItems", null, requireContext());
                    }
                } else {
                    mViewModel.addImageToFeedDirectory(et_name.getText().toString(), et_details.getText().toString(),"no_image");
//                    NavController navController = Navigation.findNavController(requireActivity(), R.id.doctors_nav_host);
//                    navController.navigateUp();
                }
            }
        });
    }



    int numOfImages = 0;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == ActivityDoctor.CAMERA_REQUEST_FEED) {
//            CropImage.ActivityResult result = CropImage.INSTANCE.getActivityResult(data);
//            if (resultCode == RESULT_OK) {
//                Uri resultUri = null;
//                if (result != null) {
//                    resultUri = result.getUriContent();
//                    image_uri = resultUri;
//                    iv_image.setImageBitmap(result.getOriginalBitmap());
//                }
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                Exception error = result.getError();
//            }
//        }

        if (requestCode == ActivityDoctor.CAMERA_REQUEST_FEED) {
            // Get the Image from data
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                numOfImages = count;
                for (int i = 0; i < count; i++) {
                    Uri imageurl = data.getClipData().getItemAt(i).getUri();
                    images_uri.add(new SliderItem(imageurl));
                }
            } else {
                Uri imageurl = data.getData();
                images_uri.add(new SliderItem(imageurl));
            }
            SliderAdapterFeed sliderAdapterFeed = new SliderAdapterFeed(requireContext());
            sliderView.setSliderAdapter(sliderAdapterFeed);
            sliderAdapterFeed.renewItems(images_uri);

        }

    }

}