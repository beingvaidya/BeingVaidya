package com.mayurkakade.beingvaidya.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.MyViewPagerAdapter;

import java.util.ArrayList;

public class ImageViewPagerActivity extends AppCompatActivity {
    public ViewPager2 viewpager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        viewpager = findViewById(R.id.viewpager);
        ArrayList<String> images = getIntent().getStringArrayListExtra("List");
        MyViewPagerAdapter adapter = new MyViewPagerAdapter(ImageViewPagerActivity.this, images);
        viewpager.setAdapter(adapter);
        viewpager.setCurrentItem(getIntent().getIntExtra("Position", 0));
    }
}