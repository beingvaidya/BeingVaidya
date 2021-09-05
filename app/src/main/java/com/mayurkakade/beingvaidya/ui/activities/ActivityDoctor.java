package com.mayurkakade.beingvaidya.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

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

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
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
import com.mayurkakade.beingvaidya.BuildConfig;
import com.mayurkakade.beingvaidya.Config;
import com.mayurkakade.beingvaidya.R;
import com.mayurkakade.beingvaidya.notification.MessagingUtils;
import com.mayurkakade.beingvaidya.notification.OnUpdateToken;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ActivityDoctor extends AppCompatActivity  implements BillingProcessor.IBillingHandler{

    BottomNavigationView bottomNavigationView;
    TextView toolbar_title;
    ImageView toolbar_options;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    public static final int CAMERA_REQUEST_PRESCRIPTION = 101;
    public static final int CAMERA_REQUEST_REPORT = 102;
    public static final int MY_CAMERA_PERMISSION_CODE = 103;
    public static final int CAMERA_REQUEST_FEED = 104;
    public static final int CAMERA_REQUEST_PATIENTS_COMMUNITY = 105;
    public static final int EXTERNAL_STORAGE_PERMISSION_CODE = 107;
    MessagingUtils messagingUtils;
    String token;

    private BillingProcessor bp;

    public static final String TAG = "DOCTORS";

    OnUpdateToken onUpdateToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        bp = new BillingProcessor(this, getString(R.string.google_play_license_key), this);
        bp.initialize();

        ImageView iv_notifications = findViewById(R.id.iv_notifications);
        iv_notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityDoctor.this, ActivityNotifications.class);
                startActivity(intent);
            }
        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.doctors_nav_host);
        onUpdateToken = new OnUpdateToken() {
            @Override
            public void onStart() {
                Log.d(TAG, "onStart: " + " Token Update Started");
            }

            @Override
            public void onSuccess(String token) {
                Log.d(TAG, "onSuccess: " + "Token Updated Successfully : " + token);
            }

            @Override
            public void onFailure(String message) {
                Log.e(TAG, "onFailure: " + "Token Update Failed : " + message );
            }
        };

        messagingUtils = MessagingUtils.getInstance();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<String> task) {
                if (task.isSuccessful()) {
                    messagingUtils.updateToken(task.getResult(),true,onUpdateToken);
                    token = task.getResult();
                } else {
                    Log.e(TAG, "onComplete: " + task.getException().getMessage() );
                }
            }
        });



        toolbar_title = findViewById(R.id.toolbar_title);
        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        toolbar_options = findViewById(R.id.iv_toolbar_options);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},EXTERNAL_STORAGE_PERMISSION_CODE);
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
                            Toast.makeText(ActivityDoctor.this, "Logging out", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(ActivityDoctor.this, ActivityAuthentication.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ActivityDoctor.this, "user not exist", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case R.id.my_subscription:
                        Intent intentSubscription = new Intent(ActivityDoctor.this,SubscriptionsActivity.class);
                        startActivity(intentSubscription);
                        break;

                    case R.id.my_profile:
                        if (navHostFragment != null) {
                            NavController navController = navHostFragment.getNavController();
                            Bundle args = new Bundle();
                            args.putString("doc_id",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                            args.putString("from","self");
                            navController.navigate(R.id.doctorsProfileShowFragment,args);
                            bottomNavigationView.setVisibility(View.GONE);
                        }

                        break;

                    case R.id.patients_community:
                            if (navHostFragment != null) {
                                NavController navController = navHostFragment.getNavController();
                                navController.navigate(R.id.patientsCommunityFragment);
                                bottomNavigationView.setVisibility(View.GONE);
                            }
                        break;

                    case R.id.search_doctors:
                        if (navHostFragment != null) {
                            NavController navController = navHostFragment.getNavController();
                            navController.navigate(R.id.searchDoctorsFragment);
                        }
                        break;


                    case R.id.rate_us:
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName()));
                        startActivity(intent);
                        break;


                    case R.id.my_posts:
                        if (navHostFragment != null) {
                            NavController navController = navHostFragment.getNavController();
                            navController.navigate(R.id.myPostsFragment);
                            bottomNavigationView.setVisibility(View.GONE);
                        }
                        break;

                    case R.id.saved_posts:
                        if (navHostFragment != null) {
                            NavController navController = navHostFragment.getNavController();
                            navController.navigate(R.id.savedPostsFragment);
                            bottomNavigationView.setVisibility(View.GONE);
                        }
                        break;

                    case R.id.share_the_app:
                        try {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Being Vaidya");
                            String shareMessage= "\nLet me recommend you this application\n\n";
                            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                            startActivity(Intent.createChooser(shareIntent, "choose one"));
                        } catch(Exception e) {
                            //e.toString();
                        }
                        break;

                    case R.id.share_my_doc_id:


                        break;

                    default:
                        //Toast.makeText(ActivityDoctor.this, "DEFAULT", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });


//        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.doctors_nav_host);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navHostFragment.getNavController());
            navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
                @Override
                public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                    toolbar_title.setText(destination.getLabel());
                    if(destination.getId() == R.id.fullScreenImageFragment) {
                        bottomNavigationView.setVisibility(View.GONE);
                    } else {
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
                    Toast.makeText(this, "negative case ran", Toast.LENGTH_SHORT).show();
                    break;
                case Config.NOTIFICATION_TYPE_PDF_ADDED:
                    docId = intent.getStringExtra("docId");
                    args.putString("doc_id", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                    args.putString("itemId", docId);
                    args.putString("from", "self");
                    navController.navigate(R.id.learningFragment, args);
                    break;

                case Config.NOTIFICATION_TYPE_STORE_ITEM_ADDED:
                    Toast.makeText(this, "Store Item Redirector case", Toast.LENGTH_SHORT).show();
                    docId = intent.getStringExtra("docId");
                    args.putString("doc_id", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                    args.putString("itemId", docId);
                    args.putString("from", "self");
                    navController.navigate(R.id.storeFragment, args);
                    break;

                case Config.NOTIFICATION_TYPE_COMMENT:
                    docId = intent.getStringExtra("docId");
                    args.putString("doc_id", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                    args.putString("itemId", docId);
                    args.putString("from", "self");
                    navController.navigate(R.id.feedFragment, args);
                    break;

                case Config.NOTIFICATION_TYPE_PATIENT_ADDED:

                    break;
                default:
                    Toast.makeText(this, "default ran", Toast.LENGTH_SHORT).show();
                    break;
            }
        }


    }


    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {
        if (bp.isPurchased(Config.Subscriptions.freePlanSubscriptionId)) {
            updateSubscription(Config.Subscriptions.freePlanSubscriptionId);
//            TransactionDetails subscriptionTransactionDetails = bp.getSubscriptionTransactionDetails(Config.Subscriptions.freePlanSubscriptionId);
        } else if (bp.isPurchased(Config.Subscriptions.yearlyUnlimitedPlanSubscriptionId)) {
            updateSubscription(Config.Subscriptions.yearlyUnlimitedPlanSubscriptionId);
        } else if (bp.isPurchased(Config.Subscriptions.halfYearlyUnlimitedPlanSubscriptionId)) {
            updateSubscription(Config.Subscriptions.halfYearlyUnlimitedPlanSubscriptionId);
        } else if (bp.isPurchased(Config.Subscriptions.monthlyUnlimitedPlanSubscriptionId)) {
            updateSubscription(Config.Subscriptions.monthlyUnlimitedPlanSubscriptionId);
        } else if (bp.isPurchased(Config.Subscriptions.monthlyThirtyPlanSubscriptionId)) {
            updateSubscription(Config.Subscriptions.monthlyThirtyPlanSubscriptionId);
        } else if (bp.isPurchased(Config.Subscriptions.monthlyFifteenPlanSubscriptionId)) {
            updateSubscription(Config.Subscriptions.monthlyFifteenPlanSubscriptionId);
        } else {
            updateSubscription(Config.Subscriptions.freePlanSubscriptionId);
        }
    }

    private void updateSubscription(String planSubscriptionId) {
        /*Map<String,Object> params = new HashMap<>();
        params.put("plan_name",planSubscriptionId);
        String collectionAddress = "Doctors/"+FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"/myPlan";
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(collectionAddress).document("plan_name").update(params).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Log.d(TAG, "onComplete: " + Objects.requireNonNull(task.getException()).getMessage());
                } else {
                    Log.d(TAG, "onComplete: " + "task Successful");
                }
            }
        });*/
    }
}