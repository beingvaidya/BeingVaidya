package com.mayurkakade.beingvaidya.ui.fragments.patient;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mayurkakade.beingvaidya.R;

public class BlogDetailsFragment extends Fragment {

    String img_url,title,content;

    TextView tv_content,tv_title;
    ImageView image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blog_details, container, false);

        tv_content = view.findViewById(R.id.tv_content);
        tv_title = view.findViewById(R.id.tv_title);
        image = view.findViewById(R.id.image);

        if (getArguments() != null) {
            img_url = getArguments().getString("arg_img");
            title = getArguments().getString("arg_title");
            content = getArguments().getString("arg_content");
            tv_title.setText(title);
            tv_content.setText(content);
            Glide.with(requireContext()).load(img_url).into(image);
        }

        return view;
    }
}