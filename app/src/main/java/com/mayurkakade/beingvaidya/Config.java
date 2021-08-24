package com.mayurkakade.beingvaidya;

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

}
