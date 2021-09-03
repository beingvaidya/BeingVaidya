package com.mayurkakade.beingvaidya;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mayurkakade.beingvaidya.data.models.SubscriptionModel;

public class Config {
    public static final int NOTIFICATION_TYPE_PDF_ADDED = 101;
    public static final int NOTIFICATION_TYPE_STORE_ITEM_ADDED = 102;
    public static final int NOTIFICATION_TYPE_COMMENT = 103;
    public static final int NOTIFICATION_TYPE_PATIENT_ADDED = 104;


    public static final int NOTIFICATION_TYPE_BLOGS = 105;
    public static final int NOTIFICATION_TYPE_TIPS = 106;

//    SubscriptionModel modelFreePlan = new SubscriptionModel(0,"5 Patients", "bvFreeSubscriptionId", "Month");
//    SubscriptionModel modelYearlyUnlimited = new SubscriptionModel(3499,"Unlimited", "bvYearlySubscriptionId", "Year");
//    SubscriptionModel modelHalfYearlyUnlimited = new SubscriptionModel(1999,"Unlimited", "bvHalfYearlySubscriptionId", "6 months");
//    SubscriptionModel modelMonthlyUnlimited = new SubscriptionModel(399,"Unlimited", "bvMonthlyUnlimitedSubscriptionId", "Month");
//    SubscriptionModel modelMonthlyThirtyPatients = new SubscriptionModel(249,"30 Patients", "bvMonthlyThirtyPatientsSubscriptionId", "Month");
//    SubscriptionModel modelMonthlyFifteenPatients = new SubscriptionModel(149,"15 Patients", "bvMonthlyFifteenPatientsSubscriptionId", "Month");

    public static class Subscriptions {
        public static final String freePlanSubscriptionId = "freePlanSubscriptionId";
        public static final String yearlyUnlimitedPlanSubscriptionId = "yearlyUnlimitedPlanSubscriptionId";
        public static final String halfYearlyUnlimitedPlanSubscriptionId = "halfYearlyUnlimitedPlanSubscriptionId";
        public static final String monthlyUnlimitedPlanSubscriptionId = "monthlyUnlimitedPlanSubscriptionId";
        public static final String monthlyThirtyPlanSubscriptionId = "monthlyThirtyPlanSubscriptionId";
        public static final String monthlyFifteenPlanSubscriptionId = "monthlrFifteenPlanSubscriptionId";
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromFile(Resources res, String  file,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }





}
