package com.mayurkakade.beingvaidya.ui.fragments.onboarding;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mayurkakade.beingvaidya.R;

public class ImageFragment extends Fragment {

    private int drawableId;

    public ImageFragment(int id) {
        this.drawableId = id;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        ImageView iv_image = view.findViewById(R.id.imageView);
        iv_image.setImageDrawable(getResources().getDrawable(drawableId));
        return view;
    }
}