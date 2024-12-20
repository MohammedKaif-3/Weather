package com.kaifshaik.weather;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.provider.Settings;

public class BackgroundActivityHelper {

    private static final String TAG = "BackgroundActivityHelper";

    public static boolean isIgnoringBatteryOptimizations(Context context) {
        if (context == null) {
            // Log an error or handle the null context case if needed
            return false;
        }

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager != null && powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    public static void requestBatteryOptimizationExemption(Context context) {
        if (context == null) {
            // Log an error or handle the null context case if needed
            return;
        }

        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            // Handle the exception or log the error
            // For example: Log.e(TAG, "Failed to start battery optimization settings activity: " + e.getMessage(), e);
        }
    }
}
