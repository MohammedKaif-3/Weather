package com.kaifshaik.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("myweather", "Alarm received, starting service");
        Intent serviceIntent = new Intent(context, NotificationService.class);
        ContextCompat.startForegroundService(context, serviceIntent);
    }
}
