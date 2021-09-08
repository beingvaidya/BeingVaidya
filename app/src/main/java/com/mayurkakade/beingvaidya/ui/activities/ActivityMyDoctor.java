package com.mayurkakade.beingvaidya.ui.activities;

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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.data.adapters.NotificationAdapter;
import com.mayurkakade.beingvaidya.data.models.DoctorModel;
import com.mayurkakade.beingvaidya.data.models.NotificationModel;
import com.mayurkakade.beingvaidya.data.models.PatientModel;
import com.mayurkakade.beingvaidya.ui.fragments.patient.AlarmReceiver;
import com.mayurkakade.beingvaidya.ui.fragments.patient.MyDoctorFragment;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ActivityMyDoctor extends AppCompatActivity implements DatePickerDialog.OnDateSetListener  {

    private TextView tv_name,tv_qualification,tv_phone_no,tv_mail,tv_availability,tv_review_date,tv_remaining_time;
    private CardView bt_whatsapp_doctor;
    public static final String TAG = "MyDoctorDebug";
    private ImageView iv_profile;
    private SwitchCompat switchCompat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_doctor);
        initViews();
        getDoctorData(this);

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



    }
    private void initViews() {
        tv_name = findViewById(R.id.tv_name);
        tv_qualification = findViewById(R.id.tv_qualification);
        tv_phone_no = findViewById(R.id.tv_phone_no);
        tv_mail = findViewById(R.id.tv_mail);
        tv_availability = findViewById(R.id.tv_availability);
        tv_review_date = findViewById(R.id.tv_review_date);
        tv_remaining_time = findViewById(R.id.tv_remaining_time);
        bt_whatsapp_doctor = findViewById(R.id.bt_whatsapp_doctor);
        iv_profile = findViewById(R.id.profile_image);
        switchCompat = findViewById(R.id.switch_reminder);
    }

    private void setAlarm(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("LOCAL_AUTH", Context.MODE_PRIVATE);
        String doc_id = sharedPreferences.getString("doctor_unique_id","no id");

        firebaseFirestore.collection("Doctors/" + doc_id + "/Patients").document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {


                        if(task.getResult().getLong("dayOfMonth") != null &&
                                task.getResult().getLong("monthOfYear") != null &&
                                task.getResult().getLong("year") != null    ) {

                            String dayOfMonth = String.valueOf(task.getResult().getLong("dayOfMonth"));
                            String monthOfYear = String.valueOf(task.getResult().getLong("monthOfYear") - 1);
                            String year = String.valueOf(task.getResult().getLong("year"));

                            DatePickerDialog datePickerDialog = new DatePickerDialog(
                                    ActivityMyDoctor.this, ActivityMyDoctor.this, Integer.parseInt(year), Integer.parseInt(monthOfYear), Integer.parseInt(dayOfMonth));

                            datePickerDialog.show();

                            Calendar c = Calendar.getInstance();
                            c.set(Integer.parseInt(year), Integer.parseInt(monthOfYear), Integer.parseInt(dayOfMonth));


                            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

                            try {
                                Date date1 = simpleDateFormat.parse( dayOfMonth + "/" + monthOfYear + "/" +year);
                                Date date2 = Calendar.getInstance().getTime();

                                tv_remaining_time.setText(String.valueOf(printDifference(date2, date1)) + " Days");

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }


                    }
                } else {
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
        Intent intent = new Intent(ActivityMyDoctor.this, AlarmReceiver.class);

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
                                            Glide.with(ActivityMyDoctor.this).load(doctorModel.getProfile_url()).into(iv_profile);
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

                        if(!dayOfMonth.equalsIgnoreCase("null")  && !monthOfYear.equalsIgnoreCase("null") &&  !year.equalsIgnoreCase("null")){
                            tv_review_date.setText( dayOfMonth + " / " + monthOfYear+ "/"+year);
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

                            try {
                                Date date1 = simpleDateFormat.parse( dayOfMonth + "/" + monthOfYear+ "/" + year);
                                Date date2 = Calendar.getInstance().getTime();

                                tv_remaining_time.setText(String.valueOf(printDifference(date2,date1)) + " Days");

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }


                        }else {
                            tv_review_date.setText("");
                            tv_remaining_time.setText("");
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
        PackageManager pm= getPackageManager();
        try {
            Intent waIntent = new Intent(Intent.ACTION_SENDTO,uri);
            PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            waIntent.setPackage("com.whatsapp");
            waIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(waIntent, "Share With Doctor"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(ActivityMyDoctor.this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
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
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
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
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ActivityMyDoctor.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ActivityMyDoctor.this, 1, intent, 0);
        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        }
    }
    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ActivityMyDoctor.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ActivityMyDoctor.this, 1, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

}