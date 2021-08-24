package com.mayurkakade.beingvaidya.ui.fragments.patient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mayurkakade.beingvaidya.notification.MessagingUtils;

public class AlarmReceiver extends BroadcastReceiver {

    MessagingUtils messagingUtils;

    @Override
    public void onReceive(Context context, Intent intent) {
        messagingUtils = MessagingUtils.getInstance();
        messagingUtils.createNotification("you have appointment with your doctor","Reminder",context,false,-1,"null");
    }
}
