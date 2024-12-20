package com.kaifshaik.weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            Log.e(TAG, "Context or Intent is null");
            return;
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed. Scheduling NotificationService...");

            // Schedule your NotificationService
            try {
                settings.scheduleNotificationService(context, 8, 0);
            } catch (Exception e) {
                Log.e(TAG, "Failed to schedule NotificationService: " + e.getMessage(), e);
            }
        }
    }
}
