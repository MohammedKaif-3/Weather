package com.kaifshaik.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Objects;

public class settings extends AppCompatActivity {
    ImageView backbutton;
    TextView settingsSelectedTemperature, settingsSelectedWind, settingsSelectedAirPressure, settingsSelectedVisibility, settingsSelectedSeaPressure;
    LinearLayout settingsTemperatureBtn, settingsWindBtn, settingsAirPressureBtn, settingsVisibilityBtn, settingsSeaPressureBtn;
    SharedPreferences sharedPreferences;
    String unitTemperature, unitWind, unitAirPressure, unitVisibility, unitSeaPressure, isEnabled;
    SwitchCompat switchCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.search_activity_bg));

        sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        unitTemperature = sharedPreferences.getString("unit_temperature", "celsius");
        unitWind = sharedPreferences.getString("unit_wind", "kilometresperhour");
        unitAirPressure = sharedPreferences.getString("unit_airpressure", "hectopascals");
        unitVisibility = sharedPreferences.getString("unit_visibility", "kilometres");
        unitSeaPressure = sharedPreferences.getString("unit_seapressure", "poundspersquareinch");
        isEnabled = sharedPreferences.getString("switch1_state", "OFF");

        switchCompat = findViewById(R.id.switch1);
        ColorStateList thumbColorStateList = ContextCompat.getColorStateList(this, R.color.switch_thumb_color);
        ColorStateList trackColorStateList = ContextCompat.getColorStateList(this, R.color.switch_track_color);
        switchCompat.setTrackTintList(trackColorStateList);
        switchCompat.setThumbTintList(thumbColorStateList);

        switchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(ContextCompat.checkSelfPermission(settings.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                    openAppSettings();
                    Toast.makeText(settings.this, "Allow Notifications Permission", Toast.LENGTH_SHORT).show();
                    switchCompat.setChecked(false);
                    editor.putString("switch1_state", "OFF");
                    editor.apply();
                }
                if (!BackgroundActivityHelper.isIgnoringBatteryOptimizations(settings.this)) {
                    showBatteryOptimizationDialog();
                    switchCompat.setChecked(false);
                    editor.putString("switch1_state", "OFF");
                    editor.apply();
                }
            }
        });

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ActivityCompat.checkSelfPermission(settings.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            // Request the POST_NOTIFICATIONS permission for Android 13 and above
                            ActivityCompat.requestPermissions(settings.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
                            buttonView.setChecked(false); // Reset the switch to the off position
                            return; // Exit the method until permission is granted
                        }
                    } else {
                        // Handle permissions for Android 6.0 and above
                        if (ActivityCompat.checkSelfPermission(settings.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // Request other necessary permissions
                            ActivityCompat.requestPermissions(settings.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1002);
                            buttonView.setChecked(false); // Reset the switch to the off position
                            return; // Exit the method until permission is granted
                        }
                    }

                    editor.putString("switch1_state", "ON");

                    scheduleNotificationService(settings.this, 8, 0);

                } else {
                    editor.putString("switch1_state", "OFF");
                    Log.d("myweather", "Notifications OFF");
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(settings.this, MyReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(settings.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    alarmManager.cancel(pendingIntent);
                }

                editor.apply();
            }
        });


        settingsSelectedTemperature = findViewById(R.id.settings_selected_temperature);
        settingsSelectedWind = findViewById(R.id.settings_selected_wind);
        settingsSelectedAirPressure = findViewById(R.id.settings_selected_airpressure);
        settingsSelectedVisibility = findViewById(R.id.settings_selected_visibility);
        settingsSelectedSeaPressure = findViewById(R.id.settings_selected_seapressure);
        settingsTemperatureBtn = findViewById(R.id.settings_temperature_btn);
        settingsWindBtn = findViewById(R.id.settings_wind_btn);
        settingsAirPressureBtn = findViewById(R.id.settings_airpressure_btn);
        settingsVisibilityBtn = findViewById(R.id.settings_visibility_btn);
        settingsSeaPressureBtn = findViewById(R.id.settings_seapressure_btn);
        backbutton = findViewById(R.id.backButton);

        if (unitTemperature.equals("celsius")) {
            settingsSelectedTemperature.setText(R.string.settings_celcius);
        }else{
            settingsSelectedTemperature.setText(R.string.settings_faranheit);
        }

        if(unitWind.equals("kilometresperhour")){
            settingsSelectedWind.setText(R.string.settings_kilometres_per_hour);
        }else if(unitWind.equals("milesperhour")){
            settingsSelectedWind.setText(R.string.settings_miles_per_hour);
        }else{
            settingsSelectedWind.setText(R.string.settings_metres_per_second);
        }

        if(unitAirPressure.equals("hectopascals")){
            settingsSelectedAirPressure.setText(R.string.settings_hectopascals);
        }else{
            settingsSelectedAirPressure.setText(R.string.settings_pounds_per_square_inch);
        }

        if(unitVisibility.equals("kilometres")){
            settingsSelectedVisibility.setText(R.string.settings_kilometres);
        }else if(unitVisibility.equals("metres")){
            settingsSelectedVisibility.setText(R.string.settings_metres);
        }else{
            settingsSelectedVisibility.setText(R.string.settings_feet);
        }

        if(unitSeaPressure.equals("hectopascals")){
            settingsSelectedSeaPressure.setText(R.string.settings_hectopascals);
        }else{
            settingsSelectedSeaPressure.setText(R.string.settings_pounds_per_square_inch);
        }

        if(isEnabled.equals("ON")){
            if(ActivityCompat.checkSelfPermission(settings.this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
                switchCompat.setChecked(true);
            }
        }else{
            switchCompat.setChecked(false);
        }

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        settingsTemperatureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        settingsWindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu2(v);
            }
        });

        settingsAirPressureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu3(v);
            }
        });

        settingsVisibilityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu4(v);
            }
        });

        settingsSeaPressureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu5(v);
            }
        });
    }
    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.menu_temperature);

        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
            MenuItem item = popupMenu.getMenu().getItem(i);
            String itemTitle = Objects.requireNonNull(item.getTitle()).toString().toLowerCase().trim();

            // Normalize the title by removing temperature unit symbols and trimming whitespace
            String normalizedTitle = itemTitle.replace("°c", "").replace("°f", "").replace("(", "").replace(")", "").trim();

            if (normalizedTitle.equals(unitTemperature)) {
                Spannable spannable = new SpannableString(item.getTitle());
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#3097FF")), 0, spannable.length(), 0);
                item.setTitle(spannable);
            }
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Intent intent = new Intent(settings.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                String itemTitle = item.getTitle().toString().toLowerCase().trim();
                String normalizedTitle = itemTitle.replace("°c", "").replace("°f", "").replace("(", "").replace(")", "").trim();
                int itemId = item.getItemId();

                if(normalizedTitle.equals(unitTemperature)){
                    Toast.makeText(settings.this, "Already in " + unitTemperature, Toast.LENGTH_SHORT).show();
                    return true;
                }else if (itemId == R.id.action_celsius) {
                    settingsSelectedTemperature.setText(R.string.settings_celcius);
                    editor.putString("unit_temperature", "celsius");
                    editor.apply();
                } else if (itemId == R.id.action_fahrenheit) {
                    settingsSelectedTemperature.setText(R.string.settings_faranheit);
                    editor.putString("unit_temperature", "fahrenheit");
                    editor.apply();
                } else {
                    return false;
                }
                finish();
                startActivity(intent);
                return true;
            }
        });
        popupMenu.show();
    }
    private void showPopupMenu2(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.menu_wind);

        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
            MenuItem item = popupMenu.getMenu().getItem(i);
            String itemTitle = Objects.requireNonNull(item.getTitle()).toString().toLowerCase().trim();

            // Normalize the title by removing temperature unit symbols and trimming whitespace
            String normalizedTitle = itemTitle.replace(" ", "").replace("mph", "").replace("m/s", "").replace("km/h", "").replace("(", "").replace(")", "").trim();
            Log.d("myap", normalizedTitle);
            Log.d("myap", unitWind);

            if (normalizedTitle.equals(unitWind)) {
                Spannable spannable = new SpannableString(item.getTitle());
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#3097FF")), 0, spannable.length(), 0);
                item.setTitle(spannable);
            }
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Intent intent = new Intent(settings.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                String itemTitle = Objects.requireNonNull(item.getTitle()).toString().toLowerCase().trim();
                String toastString = itemTitle.replace("mph", "").replace("m/s", "").replace("km/h", "").replace("(", "").replace(")", "").trim();
                String normalizedTitle = toastString.replace(" ", "");
                int itemId = item.getItemId();

                if(normalizedTitle.equals(unitWind)){
                    Toast.makeText(settings.this, "Already in " + toastString, Toast.LENGTH_SHORT).show();
                    return true;
                }else if (itemId == R.id.action_metres_per_second) {
                    settingsSelectedWind.setText(R.string.settings_metres_per_second);
                    editor.putString("unit_wind", "metrespersecond");
                    editor.apply();
                } else if (itemId == R.id.action_miles_per_hour) {
                    settingsSelectedWind.setText(R.string.settings_miles_per_hour);
                    editor.putString("unit_wind", "milesperhour");
                    editor.apply();
                } else if(itemId == R.id.action_kilometres_per_hour){
                    settingsSelectedWind.setText(R.string.settings_kilometres_per_hour);
                    editor.putString("unit_wind", "kilometresperhour");
                    editor.apply();
                }
                else {
                    return false;
                }

                startActivity(intent);
                return true;
            }
        });
        popupMenu.show();
    }

    private void showPopupMenu3(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.menu_airpressure);

        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
            MenuItem item = popupMenu.getMenu().getItem(i);
            String itemTitle = Objects.requireNonNull(item.getTitle()).toString().toLowerCase().trim();

            // Normalize the title by removing temperature unit symbols and trimming whitespace
            String normalizedTitle = itemTitle.replace(" ", "").replace("hPa", "").replace("hpa", "").replace("psi", "").replace("(", "").replace(")", "").trim();
            Log.d("myap", normalizedTitle);
            Log.d("myap", unitAirPressure);

            if (normalizedTitle.equals(unitAirPressure)) {
                Spannable spannable = new SpannableString(item.getTitle());
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#3097FF")), 0, spannable.length(), 0);
                item.setTitle(spannable);
            }
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Intent intent = new Intent(settings.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                String itemTitle = Objects.requireNonNull(item.getTitle()).toString().toLowerCase().trim();
                String toastString = itemTitle.replace("hpa", "").replace("psi", "").replace("(", "").replace(")", "").trim();
                String normalizedTitle = toastString.replace(" ", "");
                int itemId = item.getItemId();

                if(normalizedTitle.equals(unitAirPressure)){
                    Toast.makeText(settings.this, "Already in " + toastString, Toast.LENGTH_SHORT).show();
                    return true;
                }else if (itemId == R.id.action_hectopascals) {
                    settingsSelectedAirPressure.setText(R.string.settings_hectopascals);
                    editor.putString("unit_airpressure", "hectopascals");
                    editor.apply();
                } else if (itemId == R.id.action_pounds_per_square_inch) {
                    settingsSelectedAirPressure.setText(R.string.settings_pounds_per_square_inch);
                    editor.putString("unit_airpressure", "poundspersquareinch");
                    editor.apply();
                }else {
                    return false;
                }

                startActivity(intent);
                return true;
            }
        });
        popupMenu.show();
    }

    private void showPopupMenu4(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.menu_visibility);

        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
            MenuItem item = popupMenu.getMenu().getItem(i);
            String itemTitle = Objects.requireNonNull(item.getTitle()).toString().toLowerCase().trim();

            // Normalize the title by removing temperature unit symbols and trimming whitespace
            String normalizedTitle = itemTitle.replace(" ", "").replace("(km)", "").replace("(m)", "").replace("(ft)", "").trim();
            Log.d("myap", normalizedTitle);
            Log.d("myap", unitVisibility);

            if (normalizedTitle.equals(unitVisibility)) {
                Spannable spannable = new SpannableString(item.getTitle());
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#3097FF")), 0, spannable.length(), 0);
                item.setTitle(spannable);
            }
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Intent intent = new Intent(settings.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                String itemTitle = Objects.requireNonNull(item.getTitle()).toString().toLowerCase().trim();
                String toastString = itemTitle.replace("(km)", "").replace("(m)", "").replace("(ft)", "").trim();
                String normalizedTitle = toastString.replace(" ", "");
                int itemId = item.getItemId();

                if(normalizedTitle.equals(unitVisibility)){
                    Toast.makeText(settings.this, "Already in " + toastString, Toast.LENGTH_SHORT).show();
                    return true;
                }else if (itemId == R.id.action_kilometres) {
                    settingsSelectedVisibility.setText(R.string.settings_kilometres);
                    editor.putString("unit_visibility", "kilometres");
                    editor.apply();
                } else if (itemId == R.id.action_metres) {
                    settingsSelectedVisibility.setText(R.string.settings_metres);
                    editor.putString("unit_visibility", "metres");
                    editor.apply();
                } else if (itemId == R.id.action_feet) {
                    settingsSelectedVisibility.setText(R.string.settings_feet);
                    editor.putString("unit_visibility", "feet");
                    editor.apply();
                }else {
                    return false;
                }

                startActivity(intent);
                return true;
            }
        });
        popupMenu.show();
    }

    private void showPopupMenu5(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.menu_seapressure);

        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
            MenuItem item = popupMenu.getMenu().getItem(i);
            String itemTitle = Objects.requireNonNull(item.getTitle()).toString().toLowerCase().trim();

            // Normalize the title by removing temperature unit symbols and trimming whitespace
            String normalizedTitle = itemTitle.replace(" ", "").replace("hpa", "").replace("psi", "").replace("(", "").replace(")", "").trim();
            Log.d("myap", normalizedTitle);
            Log.d("myap", unitSeaPressure);

            if (normalizedTitle.equals(unitSeaPressure)) {
                Spannable spannable = new SpannableString(item.getTitle());
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#3097FF")), 0, spannable.length(), 0);
                item.setTitle(spannable);
            }
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Intent intent = new Intent(settings.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                String itemTitle = Objects.requireNonNull(item.getTitle()).toString().toLowerCase().trim();
                String toastString = itemTitle.replace("inhg", "").replace("hpa", "").replace("psi", "").replace("(", "").replace(")", "").trim();
                String normalizedTitle = toastString.replace(" ", "");
                int itemId = item.getItemId();

                if(normalizedTitle.equals(unitSeaPressure)){
                    Toast.makeText(settings.this, "Already in " + toastString, Toast.LENGTH_SHORT).show();
                    return true;
                }else if (itemId == R.id.action_hectopascals2) {
                    settingsSelectedSeaPressure.setText(R.string.settings_hectopascals);
                    editor.putString("unit_seapressure", "hectopascals");
                    editor.apply();
                } else if (itemId == R.id.action_pounds_per_square_inch2) {
                    settingsSelectedSeaPressure.setText(R.string.settings_pounds_per_square_inch);
                    editor.putString("unit_seapressure", "poundspersquareinch");
                    editor.apply();
                }else {
                    return false;
                }

                startActivity(intent);
                return true;
            }
        });
        popupMenu.show();
    }
    public static void scheduleNotificationService(Context context, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long triggerAtMillis = calendar.getTimeInMillis();
        if (triggerAtMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Schedule for tomorrow if the time has already passed
            triggerAtMillis = calendar.getTimeInMillis();
        }

        // Set the repeating alarm
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY, pendingIntent);

        Log.d("myweather", "Daily notification scheduled for: " + calendar.getTime());
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
    private void showBatteryOptimizationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Battery Optimizations")
                .setMessage("To receive daily weather notifications, please enable this app to ignore battery optimizations.")
                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BackgroundActivityHelper.requestBatteryOptimizationExemption(settings.this);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}