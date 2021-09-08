package com.mayurkakade.beingvaidya.ui.fragments.doctor;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.OnQueryDataListener;
import com.mayurkakade.beingvaidya.data.adapters.PatientsPagerAdapter;
import com.mayurkakade.beingvaidya.data.models.PatientModel;
import com.mayurkakade.beingvaidya.data.models.PatientsCommunityImageModel;

import java.util.ArrayList;
import java.util.List;


public class PatientsFragment extends Fragment {

    public static final String TAG = "Patients_Frag";
    StarredPatientsFragment starredPatientsFragment;
    NonStarredPatientsFragment nonStarredPatientsFragment;
    ProgressBar progressBar;
    boolean handler;
    private List<PatientModel> pListStarred;
    private List<PatientModel> pListNonStarred;
    private List<Fragment> fragmentList;
    private PatientsPagerAdapter pagerAdapter;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private MyViewModel mViewModel;
    private OnQueryDataListener onQueryDataListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_patients, container, false);

        pListStarred = new ArrayList<>();
        pListNonStarred = new ArrayList<>();
        fragmentList = new ArrayList<>();
        viewPager = view.findViewById(R.id.patients_pager);
        tabLayout = view.findViewById(R.id.tabLayout);
        progressBar = view.findViewById(R.id.progressBar);


        mViewModel = new MyViewModel();
        Log.d(TAG, "onStart: " + "onStart Called");

        starredPatientsFragment = new StarredPatientsFragment(pListStarred);
        nonStarredPatientsFragment = new NonStarredPatientsFragment(pListNonStarred);
        onQueryDataListener = new OnQueryDataListener() {
            @Override
            public void onSuccess() {
                hideProgress();
                starredPatientsFragment.refreshPatientsList();
                nonStarredPatientsFragment.refreshPatientsList();
            }

            @Override
            public void onSuccessPatientsCommunity(PatientsCommunityImageModel patientsCommunityImageModel) {

            }

            @Override
            public void onStart() {
                showProgress();
            }

            @Override
            public void onFailure(String exception) {
                hideProgress();
            }
        };

        mViewModel.getDataFromServer(pListStarred, pListNonStarred, onQueryDataListener);
        fragmentList.add(starredPatientsFragment);
        fragmentList.add(nonStarredPatientsFragment);
        pagerAdapter = new PatientsPagerAdapter(getChildFragmentManager(), getLifecycle(), fragmentList);
        viewPager.setAdapter(pagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(getTabTitle(position))).attach();
        return view;
    }

    public void hideProgress() {
        handler = new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, 1000);
    }

    public void showProgress() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private String getTabTitle(int position) {
        switch (position) {
            case 0:
                return "Starred Patients";
            case 1:
                return "Non Starred Patients";
            default:
                return null;
        }
    }


}