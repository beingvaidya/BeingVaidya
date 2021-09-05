package com.mayurkakade.beingvaidya.ui.fragments.auth;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.mayurkakade.beingvaidya.Config;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.CheckUserExist;

public class SignUpFragment extends Fragment {


    private MainViewModel mViewModel;
    private View view;


    private Button bt_send_otp;
    private EditText et_phone_no;
    private TextView cb_terms;
    private SwitchCompat switchPatientOrDoc;

    public static final String TAG = "signupFragmentDebug";
    public static final String COUNTRY_CODE = "+91";

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_signup, container, false);
        initSignupViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        bt_send_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    if (switchPatientOrDoc.isChecked()) {
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
                        navController.navigate(R.id.action_signUpFragment_to_doctorRegistrationFragment);
                    } else {
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
                        navController.navigate(R.id.action_signUpFragment_to_patientRegistrationFragment);
                    }
                } else {
                        if (!TextUtils.isEmpty(et_phone_no.getText())) {
                            CheckUserExist onUserExistChecked = new CheckUserExist() {
                                @Override
                                public void onStart() {

                                }

                                @Override
                                public void onExist() {
                                    Toast.makeText(getContext(), "User Already Exist !", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure() {
                                    mViewModel.sendOtpRegistraion(COUNTRY_CODE + et_phone_no.getText().toString(), requireActivity(), switchPatientOrDoc.isChecked());
                                }
                            };

                            mViewModel.checkIfUserExists(onUserExistChecked,COUNTRY_CODE+et_phone_no.getText().toString(),requireActivity());

                        } else {
                            Snackbar.make(view, "Please enter valid phone number", BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                }
            }
        });
    }

    public void initSignupViews(View view) {
        bt_send_otp = view.findViewById(R.id.bt_send_otp);
        et_phone_no = view.findViewById(R.id.et_phone_no);
        et_phone_no.setText("9898935606");
        cb_terms = view.findViewById(R.id.cb_terms);
        switchPatientOrDoc = view.findViewById(R.id.switch_patient_or_doc);
        iv_background = view.findViewById(R.id.iv_background);

        Bitmap bitmapLocal = Config.decodeSampledBitmapFromResource(getResources(), R.drawable.register_bg, 500, 500);
        if (bitmapLocal != null && iv_background != null) {
            iv_background.setImageBitmap(bitmapLocal);
        }
    }
    ImageView iv_background;
}