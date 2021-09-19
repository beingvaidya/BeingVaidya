package com.mayurkakade.beingvaidya.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mayurkakade.beingvaidya.Config;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.notification.MessagingUtils;
import com.mayurkakade.beingvaidya.notification.OnUpdateToken;

import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;


public class ActivityPatient extends AppCompatActivity {

    public static final int MY_CAMERA_PERMISSION_CODE = 102;
    public static final int EXTERNAL_STORAGE_PERMISSION_CODE = 107;
    public static final String TAG = "PATIENTS";
    BottomNavigationView bottomNavigationView;
    TextView toolbar_title;
    ImageView toolbar_options;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    MessagingUtils messagingUtils;
    String token;
    OnUpdateToken onUpdateToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        ImageView iv_notifications = findViewById(R.id.iv_notifications);
        iv_notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityPatient.this, ActivityNotifications.class);
                startActivity(intent);
            }
        });


        onUpdateToken = new OnUpdateToken() {
            @Override
            public void onStart() {
                Log.d(TAG, "onStart: " + " Token Update Started");
            }

            @Override
            public void onSuccess(String token) {
//                messagingUtils.sendCloudNotification("+917721962413",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),"NOTIFICATION SENT SUCCESSFULLY", true);
                Log.d(TAG, "onSuccess: " + "Token Updated Successfully : " + token);
            }

            @Override
            public void onFailure(String message) {
                Log.e(TAG, "onFailure: " + "Token Update Failed : " + message);
            }
        };

        messagingUtils = MessagingUtils.getInstance();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<String> task) {
                if (task.isSuccessful()) {
                    messagingUtils.updateToken(task.getResult(), false, onUpdateToken);
                    token = task.getResult();
                } else {
                    Log.e(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });

        toolbar_title = findViewById(R.id.toolbar_title);
        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        toolbar_options = findViewById(R.id.iv_toolbar_options);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
            }
        }


        toolbar_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.close();
                switch (item.getItemId()) {
                    case R.id.log_out:
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            Toast.makeText(ActivityPatient.this, "Logging out", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(ActivityPatient.this, ActivityAuthentication.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ActivityPatient.this, "user not exist", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case R.id.myDoctorFragment:
                       /* NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.patients_nav_host);
                        if (navHostFragment != null) {
                            NavController navController = navHostFragment.getNavController();
                            navController.navigate(R.id.myDoctorFragment);
                        }
*/
                        Intent intentMyDoctor = new Intent(ActivityPatient.this, ActivityMyDoctor.class);
                        startActivity(intentMyDoctor);
                        break;

                    case R.id.my_profile:
                        Intent intentMyProfile = new Intent(ActivityPatient.this, ActivityMyProfilePatients.class);
                        startActivity(intentMyProfile);
                        break;

                    case R.id.rate_us:
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName()));
                        startActivity(intent);
                        break;

                    case R.id.whatsapp_my_doctor:
                        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                        firebaseFirestore.collection("Patients").document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().exists()) {
                                        String doctorPhoneNo = task.getResult().getString("doctor_unique_id");
                                        onClickWhatsApp("Hi " + task.getResult().getString("name") + "\n I am your patient from being Vaidya App", doctorPhoneNo);
                                    }
                                } else {
                                    Log.d("ActivityPatient", "onComplete: " + task.getException().getMessage());
                                }
                            }
                        });

                        break;
                    default:
                        Toast.makeText(ActivityPatient.this, "DEFAULT", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });


        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.patients_nav_host);

        if (navHostFragment != null) {
            NavigationUI.setupWithNavController(bottomNavigationView, navHostFragment.getNavController());
//            NavigationUI.setupWithNavController(navigationView, navHostFragment.getNavController());
            NavController navController = navHostFragment.getNavController();
            navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
                @Override
                public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                    toolbar_title.setText(destination.getLabel());
                    AppBarLayout appBarLayout = findViewById(R.id.app_bar);
                    if (destination.getId() == R.id.fullScreenImageFragment2) {
                        appBarLayout.setVisibility(View.GONE);
                        bottomNavigationView.setVisibility(View.GONE);
                    } else {
                        appBarLayout.setVisibility(View.VISIBLE);
                        bottomNavigationView.setVisibility(View.VISIBLE);
                    }
                }
            });
        }


        Intent intent = getIntent();
        long notificationType = intent.getLongExtra("notificationType", -1);
        String docId;
        NavController navController = null;
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            Bundle args = new Bundle();
            switch (Integer.parseInt(String.valueOf(notificationType))) {
                case -1:
                    // Toast.makeText(this, "negative case ran", Toast.LENGTH_SHORT).show();
                    break;
                case Config.NOTIFICATION_TYPE_TIPS:
                    docId = intent.getStringExtra("docId");
                    args.putString("doc_id", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                    args.putString("itemId", docId);
                    args.putString("from", "self");
                    navController.navigate(R.id.tipsFragment, args);
                    break;
                case Config.NOTIFICATION_TYPE_BLOGS:
                    docId = intent.getStringExtra("docId");
                    args.putString("doc_id", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                    args.putString("itemId", docId);
                    args.putString("from", "self");
                    navController.navigate(R.id.blogsFragment, args);
                    break;

                default:
                    Toast.makeText(this, "default ran", Toast.LENGTH_SHORT).show();
                    break;
            }
        }


    }


    private void onClickWhatsApp(String mensaje, String numero) {
        try {
            PackageManager packageManager = ActivityPatient.this.getPackageManager();
            Intent i = new Intent(Intent.ACTION_VIEW);
            String url = "https://api.whatsapp.com/send?phone=" + numero + "&text=" + URLEncoder.encode(mensaje, "UTF-8");
            i.setPackage("com.whatsapp");
            i.setData(Uri.parse(url));
            if (i.resolveActivity(packageManager) != null) {
                startActivity(i);
            } else {
                Toast.makeText(this, "WhatsApp not Installed : ", Toast.LENGTH_SHORT)
                        .show();
            }
        } catch (Exception e) {
            Log.e("ERRORWHAT", e.toString());
            Toast.makeText(this, "WhatsApp not Installed : " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("result_checking", "onActivityResult: activity");
    }
}