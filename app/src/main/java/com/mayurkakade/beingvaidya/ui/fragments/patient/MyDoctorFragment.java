package com.mayurkakade.beingvaidya.ui.fragments.patient;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class MyDoctorFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private TextView tv_name,tv_qualification,tv_phone_no,tv_mail,tv_availability,tv_review_date,tv_remaining_time;
    private CardView bt_whatsapp_doctor;
    public static final String TAG = "MyDoctorDebug";
    private ImageView iv_profile;
    private SwitchCompat switchCompat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_doctor_new, container, false);
        initViews(view);
        getDoctorData(container.getContext());

        bt_whatsapp_doctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whatsappMyDoctor();
            }
        });

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setAlarm();
                } else {
                    cancelAlarm();
                }
            }
        });

        return view;
    }



    private void setAlarm(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LOCAL_AUTH",Context.MODE_PRIVATE);
        String doc_id = sharedPreferences.getString("doctor_unique_id","no id");

        firebaseFirestore.collection("Doctors/" + doc_id + "/Patients").document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {


                        String dayOfMonth = String.valueOf(task.getResult().getLong("dayOfMonth"));
                        String monthOfYear = String.valueOf(task.getResult().getLong("monthOfYear") - 1 );
                        String year = String.valueOf(task.getResult().getLong("year"));

                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                requireContext(), MyDoctorFragment.this, Integer.parseInt(year), Integer.parseInt(monthOfYear), Integer.parseInt(dayOfMonth));

                        datePickerDialog.show();

                        Calendar c = Calendar.getInstance();
                        c.set(Integer.parseInt(year),Integer.parseInt(monthOfYear),Integer.parseInt(dayOfMonth));



                        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/dd/MM");

                        try {
                            Date date1 = simpleDateFormat.parse(year + "/" + dayOfMonth + "/" + monthOfYear);
                            Date date2 = Calendar.getInstance().getTime();

                            tv_remaining_time.setText(String.valueOf(printDifference(date2,date1)) + " Days");

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                    }
                } else {
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
        Intent intent = new Intent(requireContext(), AlarmReceiver.class);

    }


    private void getDoctorProfileImage(String doctor_id){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Doctors").document(doctor_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                DoctorModel doctorModel = task.getResult().toObject(DoctorModel.class);
                                if (doctorModel!= null) {
                                    if (doctorModel.getPhone_no() != null) {
                                        if (!doctorModel.getPhone_no().equals("") || !doctorModel.getPhone_no().equals("no_profile") ) {
                                            Log.d(TAG + "profile", "onComplete: " + doctorModel.getProfile_url());
                                            Glide.with(requireContext()).load(doctorModel.getProfile_url()).into(iv_profile);
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void getDoctorData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LOCAL_AUTH",Context.MODE_PRIVATE);
        String doc_id = sharedPreferences.getString("doctor_unique_id","no id");

        getDoctorProfileImage(doc_id);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("Doctors/"+doc_id+"/availability").document("availability").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                if (task.getResult().exists()) {
                                    tv_availability.setText(task.getResult().getString("availability"));
                                }
                            }
                        } else {
                            Log.d(TAG, "onComplete: " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore.collection("Doctors").document(doc_id).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null) {
                                    DoctorModel doctorModel = task.getResult().toObject(DoctorModel.class);
                                    if (doctorModel != null) {
                                        tv_name.setText(doctorModel.getName());
                                        tv_qualification.setText(doctorModel.getQualification());
                                        tv_phone_no.setText(doctorModel.getPhone_no());
                                        tv_mail.setText(doctorModel.getEmail());
                                    } else {
                                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } else {
                                Log.d(TAG, "onComplete: unsuccessful" + task.getException().getMessage());
                                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }

        firebaseFirestore.collection("Doctors/" + doc_id + "/Patients").document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String dayOfMonth = String.valueOf(task.getResult().getLong("dayOfMonth"));
                        String monthOfYear = String.valueOf(task.getResult().getLong("monthOfYear"));
                        String year = String.valueOf(task.getResult().getLong("year"));

                        tv_review_date.setText(year + " / " + dayOfMonth + " / " + monthOfYear);


                        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/dd/MM");

                        try {
                            Date date1 = simpleDateFormat.parse(year + "/" + dayOfMonth + "/" + monthOfYear);
                            Date date2 = Calendar.getInstance().getTime();

                            tv_remaining_time.setText(String.valueOf(printDifference(date2,date1)) + " Days");

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                    }
                } else {
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }

    public long printDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

//        System.out.printf(
//                "%d days, %d hours, %d minutes, %d seconds%n",
//                elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);

        return elapsedDays;
    }

    private void initViews(View view) {
        tv_name = view.findViewById(R.id.tv_name);
        tv_qualification = view.findViewById(R.id.tv_qualification);
        tv_phone_no = view.findViewById(R.id.tv_phone_no);
        tv_mail = view.findViewById(R.id.tv_mail);
        tv_availability = view.findViewById(R.id.tv_availability);
        tv_review_date = view.findViewById(R.id.tv_review_date);
        tv_remaining_time = view.findViewById(R.id.tv_remaining_time);
        bt_whatsapp_doctor = view.findViewById(R.id.bt_whatsapp_doctor);
        iv_profile = view.findViewById(R.id.profile_image);
        switchCompat = view.findViewById(R.id.switch_reminder);
    }

    private void whatsappMyDoctor() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Patients").document(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String doctorPhoneNo = task.getResult().getString("doctor_unique_id");
                        onClickWhatsApp("Hello Doctor, My name is "+ task.getResult().getString("name") + ". My Age is "+ task.getResult().getLong("age") , doctorPhoneNo);
                    }
                } else {
                    Log.d("ActivityPatient", "onComplete: " + task.getException().getMessage());
                }
            }
        });

    }


    public void onClickWhatsApp(String message, String number) {
        Uri uri = Uri.parse("smsto:" + number);
        PackageManager pm=requireContext().getPackageManager();
        try {
            Intent waIntent = new Intent(Intent.ACTION_SENDTO,uri);
            PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            waIntent.setPackage("com.whatsapp");
            waIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(waIntent, "Share With Doctor"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(requireContext(), "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);




        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute);
                        c.set(Calendar.SECOND, 0);

                        startAlarm(c);
                    }
                }, 10, 0, false);
        timePickerDialog.show();


    }

    private void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 1, intent, 0);
        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        }
    }
    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 1, intent, 0);
        alarmManager.cancel(pendingIntent);
    }
}