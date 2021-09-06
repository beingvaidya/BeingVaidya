package com.mayurkakade.beingvaidya.ui.fragments.auth;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.CheckUserExist;
import com.mayurkakade.beingvaidya.ui.ProgressUtils;

public class LoginFragment extends Fragment {

    private MainViewModel mViewModel;
    private View view;

    private Button bt_send_otp;
    private EditText et_phone_no;
    private TextView cb_terms;

    public static final String TAG = "viewModelDebug";
    public static final String COUNTRY_CODE = "+91";

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);
        this.view = view;
        initLoginViews(view);


        TextView tv_signup = view.findViewById(R.id.tv_signup);
        tv_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
                navController.navigate(R.id.action_loginFragment_to_signUpFragment);
            }
        });

        return view;


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        bt_send_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(et_phone_no.getText())) {
                        CheckUserExist onUserExistChecked = new CheckUserExist() {
                            @Override
                            public void onStart() {
                                Log.d(TAG, "onStart: ");
                            }

                            @Override
                            public void onExist() {
                                Log.d(TAG, "callback onSuccess: ");
                                mViewModel.sendOtpLogin(COUNTRY_CODE + et_phone_no.getText().toString(), requireActivity());
                            }

                            @Override
                            public void onFailure() {
                                Log.d(TAG, "callback onFailure: ");
                                Toast.makeText(getContext(), "User Does not exist in our database !", Toast.LENGTH_SHORT).show();
                            }
                        };

                        mViewModel.checkIfUserExists(onUserExistChecked,COUNTRY_CODE+et_phone_no.getText().toString(),requireActivity());

                } else {
                    Log.d(TAG, "onClick: 102");
                    Snackbar.make(view,"Please enter valid phone number", BaseTransientBottomBar.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void initLoginViews(View view) {
        bt_send_otp = view.findViewById(R.id.bt_send_otp);
        et_phone_no = view.findViewById(R.id.et_phone_no);
//        et_phone_no.setText("9898935606");
        cb_terms = view.findViewById(R.id.cb_terms);
    }

}