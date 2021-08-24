package com.mayurkakade.beingvaidya.data.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mayurkakade.beingvaidya.data.models.PatientModel;
import com.mayurkakade.beingvaidya.ui.fragments.doctor.NonStarredPatientsFragment;
import com.mayurkakade.beingvaidya.ui.fragments.doctor.StarredPatientsFragment;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PatientsPagerAdapter extends FragmentStateAdapter{

    private final List<Fragment> fragmentList;

    public PatientsPagerAdapter(@NonNull @NotNull FragmentManager fragmentManager, @NonNull @NotNull Lifecycle lifecycle, List<Fragment> fragmentList) {
        super(fragmentManager, lifecycle);
        this.fragmentList = fragmentList;
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }


    @Override
    public int getItemCount() {
        return fragmentList.size();
    }
}
