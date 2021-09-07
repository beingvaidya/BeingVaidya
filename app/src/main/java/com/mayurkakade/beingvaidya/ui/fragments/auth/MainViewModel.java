package com.mayurkakade.beingvaidya.ui.fragments.auth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mayurkakade.beingvaidya.Config;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.CheckUserExist;
import com.mayurkakade.beingvaidya.data.Repository;
import com.mayurkakade.beingvaidya.data.OnQueryDataListener;
import com.mayurkakade.beingvaidya.data.UploadToStorageInterface;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;
import com.mayurkakade.beingvaidya.data.models.PatientModel;
import com.mayurkakade.beingvaidya.data.models.PatientsCommunityImageModel;
import com.mayurkakade.beingvaidya.notification.MessagingUtils;
import com.mayurkakade.beingvaidya.ui.ProgressUtils;
import com.mayurkakade.beingvaidya.ui.activities.ActivityDoctor;
import com.mayurkakade.beingvaidya.ui.activities.ActivityPatient;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import in.aabhasjindal.otptextview.OTPListener;
import in.aabhasjindal.otptextview.OtpTextView;

public class MainViewModel extends ViewModel {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static final String TAG = "viewModelDebug";
    String mVerificationId = null;
    PhoneAuthProvider.ForceResendingToken mResendToken = null;
    PhoneAuthCredential phoneAuthCredential = null;

    private void signUpWithPhoneAuthCredential(PhoneAuthCredential credential, FragmentActivity activity, boolean isDoctor) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (alertDialog != null) {
                                alertDialog.dismiss();
                            }
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = Objects.requireNonNull(task.getResult()).getUser();
                            assert user != null;
                            Log.d(TAG, "onComplete: " + user.getPhoneNumber());

                            if (isDoctor) {
                                NavController navController = Navigation.findNavController(activity, R.id.navHostFragment);
                                navController.navigate(R.id.action_signUpFragment_to_doctorRegistrationFragment);
                            } else {
                                NavController navController = Navigation.findNavController(activity, R.id.navHostFragment);
                                navController.navigate(R.id.action_signUpFragment_to_patientRegistrationFragment);
                            }
                            // Update UI
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    public void sendOtpRegistraion(String phone_no, FragmentActivity activity, boolean isDoctor) {
        ProgressUtils progress = ProgressUtils.getInstance(activity);
        progress.showProgress("Please wait","sending otp");
        Log.d(TAG, "sendOtp: ");
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NotNull PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:");
                phoneAuthCredential = credential;
                signUpWithPhoneAuthCredential(credential,activity, isDoctor);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                progress.hideProgress();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }
                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:");
                progress.hideProgress();
                dialogueEnterOtpRegistration(activity, isDoctor);
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(phone_no)       // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(activity)                 // Activity (for callback binding)
                            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);

    }

    public void dialogueEnterOtpRegistration(FragmentActivity activity, boolean isDoctor) {
        ViewGroup viewGroup = activity.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.enter_otp_dialog, viewGroup, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogView);
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        OtpTextView otpTextView = dialogView.findViewById(R.id.otp_view);
        otpTextView.setOtpListener(new OTPListener() {
            @Override
            public void onInteractionListener() {
            }
            @Override
            public void onOTPComplete(String otp) {
               /* PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                signUpWithPhoneAuthCredential(credential,activity, isDoctor);
                alertDialog.dismiss();*/
            }
        });

        Button bt_submit = dialogView.findViewById(R.id.bt_submit);
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(otpTextView.getOTP())) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otpTextView.getOTP());
                    signUpWithPhoneAuthCredential(credential,activity, isDoctor);
                    alertDialog.dismiss();
                }
            }
        });

        //finally creating the alert dialog and displaying it

        alertDialog.show();
    }

    public void storeDoctorToDb(String name, String degree, String phone_no, String email, String qualification, String university, String pincode,String profileUrl, Context context) {
        OnQueryDataListener onQueryDataListener = new OnQueryDataListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: User Created Successfully" );
                Intent intent = new Intent(context, ActivityDoctor.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

            @Override
            public void onSuccessPatientsCommunity(PatientsCommunityImageModel patientsCommunityImageModel) {

            }

            @Override
            public void onStart() {
                Log.d(TAG, "onStart: User Creation Started" );
            }

            @Override
            public void onFailure(String s) {
                Log.d(TAG," exception :" +  s);
            }
        };

        DoctorModel doctorModel = new DoctorModel(name,degree,phone_no,email,qualification,university,pincode,profileUrl);
        Repository.addDoctorToSharedPrefs(doctorModel, context);
        Repository.addDoctor(doctorModel,onQueryDataListener);

    }

    public void storePatientToDb(String name, int age, String phone_no, String email, String address, String doctor_unique_id,String profile_url, Context context) {

        OnQueryDataListener onQueryDataListener = new OnQueryDataListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: User Created Successfully" );
                Intent intent = new Intent(context, ActivityPatient.class);
                MessagingUtils messagingUtils = MessagingUtils.getInstance();
                messagingUtils.sendCloudNotification(doctor_unique_id, FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(), "New Patient Registered :" + name, true , Config.NOTIFICATION_TYPE_PATIENT_ADDED, FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

            @Override
            public void onSuccessPatientsCommunity(PatientsCommunityImageModel patientsCommunityImageModel) {

            }

            @Override
            public void onStart() {
                Log.d(TAG, "onStart: User Creation Started" );
            }

            @Override
            public void onFailure(String s) {
                Toast.makeText(context, ""+s, Toast.LENGTH_SHORT).show();
                Log.d(TAG," exception :" +  s);
            }
        };

        PatientModel patientModel = new PatientModel(name,phone_no,email,address,doctor_unique_id,profile_url,age);
        Repository.addPatientToSharedPrefs(patientModel,context);
        Repository.addPatient(patientModel,onQueryDataListener,doctor_unique_id);

    }

    public void sendOtpLogin(String phone_no, FragmentActivity activity) {
        ProgressUtils progress = ProgressUtils.getInstance(activity);
        progress.showProgress("Please Wait","Sending otp");
        Log.d(TAG, "sendOtp: ");
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NotNull PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:");
                phoneAuthCredential = credential;
                signInWithPhoneAuthCredential(credential,activity);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                progress.hideProgress();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }
                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:");
                progress.hideProgress();
                dialogueEnterOtpLogin(activity);
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone_no)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(activity)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, FragmentActivity activity) {

        ProgressUtils progress = ProgressUtils.getInstance(activity);
        progress.showProgress("Please Wait", "Logging in");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userPhoneNumber = task.getResult().getUser().getPhoneNumber();
                            loginUser(userPhoneNumber,activity,progress);
                        } else {
                            progress.hideProgress();
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    public void loginUser(String userPhoneNumber, FragmentActivity activity, ProgressUtils progress) {
        Log.d(TAG, "onComplete: " + "login USer");
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors").document(userPhoneNumber).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        progress.hideProgress();
                        if (alertDialog!= null) {
                            alertDialog.dismiss();
                        }
                        Log.d(TAG, "onComplete: getdoctor doc" );
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: " + "task successful");
                            if (alertDialog!= null) {
                                alertDialog.dismiss();
                            }
                            if (Objects.requireNonNull(task.getResult()).exists()) {

                                DoctorModel doctorModel = task.getResult().toObject(DoctorModel.class);
                                if (doctorModel != null) {
                                    Log.d(TAG, "onComplete: " + "doctor exist");
                                    Repository.addDoctorToSharedPrefs(doctorModel,activity);
                                    Intent intent = new Intent(activity, ActivityDoctor.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                    activity.startActivity(intent);
                                } else {
                                    Log.d(TAG, "onComplete: null doctor model"  );
                                }

                            } else {
                                firebaseFirestore.collection("Patients").document(userPhoneNumber).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "onComplete: " + "patient exist");
                                                    PatientModel patientModel = task.getResult().toObject(PatientModel.class);
                                                    if (patientModel == null) {
                                                        Toast.makeText(activity, "User does not exist in our database !", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Repository.addPatientToSharedPrefs(patientModel,activity);
                                                        Intent intent = new Intent(activity, ActivityPatient.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        activity.startActivity(intent);
                                                    }
                                                } else {
                                                    Log.d(TAG, "exception : " + task.getException().getMessage());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d(TAG, "exception: " + task.getException().getMessage() );
                        }
                    }
                });

        // Update UI

    }

    AlertDialog alertDialog;
    public void dialogueEnterOtpLogin(FragmentActivity activity) {
        ViewGroup viewGroup = activity.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.enter_otp_dialog, viewGroup, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogView);
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        OtpTextView otpTextView = dialogView.findViewById(R.id.otp_view);
        otpTextView.setOtpListener(new OTPListener() {
            @Override
            public void onInteractionListener() {
            }
            @Override
            public void onOTPComplete(String otp) {
              /*  PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                signInWithPhoneAuthCredential(credential,activity);
                alertDialog.dismiss();*/
            }
        });

        Button bt_submit = dialogView.findViewById(R.id.bt_submit);
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    alertDialog.dismiss();
                } else if (!TextUtils.isEmpty(otpTextView.getOTP())) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otpTextView.getOTP());
                    signInWithPhoneAuthCredential(credential,activity);
                    alertDialog.dismiss();
                }
            }
        });

        //finally creating the alert dialog and displaying it

        alertDialog.show();

    }


    public void checkIfUserExists(CheckUserExist onUserExistChecked,String userPhoneNumber, FragmentActivity activity) {
        Log.d(TAG, "checkIfUserExists: " + userPhoneNumber);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors").document(userPhoneNumber).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Log.d(TAG, "onComplete: getdoctor doc" );
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: " + "task successful");
                            if (Objects.requireNonNull(task.getResult()).exists()) {
                                DoctorModel doctorModel = task.getResult().toObject(DoctorModel.class);
                                onUserExistChecked.onExist();
                            } else {
                                firebaseFirestore.collection("Patients").document(userPhoneNumber).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    PatientModel patientModel = task.getResult().toObject(PatientModel.class);
                                                    if (patientModel == null) {
                                                        Log.d(TAG, "onComplete: " + "patient null");
                                                        onUserExistChecked.onFailure();
                                                    } else {
                                                        Log.d(TAG, "onComplete: " + "patient exist");
                                                        onUserExistChecked.onExist();
                                                    }
                                                } else {
                                                    Log.d(TAG, "exception : " + task.getException().getMessage());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d(TAG, "exception: " + task.getException().getMessage() );
                        }
                    }
                });

    }

    public void uploadProfileImage(Uri file, UploadToStorageInterface uploadToStorageInterface, String field, Context context) {
        final ProgressUtils progress = ProgressUtils.getInstance(context);
        progress.showProgress("Please wait", "Uploading Image");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        String filename = String.valueOf(Calendar.getInstance().getTimeInMillis());
        StorageReference profileReference = storageReference.child(field+"/"+filename+".jpg");

        uploadToStorageInterface.onStart();

        UploadTask uploadTask = profileReference.putFile(file);
        Task<Uri> urlTask = uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
            uploadToStorageInterface.onStart();
            if (!task.isSuccessful()) {
                uploadToStorageInterface.onFailure();
                progress.hideProgress();
                Log.d(TAG, "uploadImage: " + task.getException().getMessage());
            }
            return profileReference.getDownloadUrl();
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                progress.hideProgress();
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    uploadToStorageInterface.onSuccess(downloadUri,field);
                    Log.d(TAG, "onComplete: " + downloadUri.toString());
                } else {
                    uploadToStorageInterface.onFailure();
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }
}