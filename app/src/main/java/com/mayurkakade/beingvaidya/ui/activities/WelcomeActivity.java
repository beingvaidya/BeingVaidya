package com.mayurkakade.beingvaidya.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import com.github.appintro.AppIntroPageTransformerType;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.ui.fragments.onboarding.ImageFragment;

public class WelcomeActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(new ImageFragment(R.drawable.one_onboarding));
        addSlide(new ImageFragment(R.drawable.two_onboarding));
        addSlide(new ImageFragment(R.drawable.three_onboarding));
        addSlide(new ImageFragment(R.drawable.four_onboarding));
        addSlide(new ImageFragment(R.drawable.five_onboarding));
//        setImmersiveMode();
        setTransformer(AppIntroPageTransformerType.Flow.INSTANCE);

    }

    @Override
    protected void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        SharedPreferences sharedPreferences = getSharedPreferences("OnBoarding",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isIntroShown", true);
        editor.apply();
        finish();
    }

    @Override
    protected void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SharedPreferences sharedPreferences = getSharedPreferences("OnBoarding",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isIntroShown", true);
        editor.apply();
        finish();
    }


}