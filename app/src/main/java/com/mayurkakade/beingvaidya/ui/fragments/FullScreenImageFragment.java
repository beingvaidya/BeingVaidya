package com.mayurkakade.beingvaidya.ui.fragments;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.mayurkakade.beingvaidya.R;

public class FullScreenImageFragment extends DialogFragment {

    String localUrl = null;
    ProgressBar circularProgressIndicator;

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_full_screen_image, container, false);
        circularProgressIndicator = view.findViewById(R.id.c_progress);
//        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);

        if (getArguments() != null) {
            localUrl = getArguments().getString("imgUrl");
            PhotoView photoView = (PhotoView) view.findViewById(R.id.photo_view);
//            Glide.with(requireContext()).load(localUrl).into(photoView);
            photoView.setImageResource(0);
            circularProgressIndicator.setVisibility(View.VISIBLE);
            Glide.with(photoView)
                    .load(localUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            circularProgressIndicator.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            circularProgressIndicator.setVisibility(View.GONE);
                            photoView.setImageDrawable(resource);
                            return false;
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade())
                    .into(photoView);
        }
        return view;
    }
}