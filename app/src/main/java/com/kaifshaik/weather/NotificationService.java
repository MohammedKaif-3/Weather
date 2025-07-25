package com.kaifshaik.weather;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationService extends Service {
    private static final String URL1 = "https://api.openweathermap.org/data/2.5/weather?q=";
    private static final String URL2 = "YOUR-API-KEY&units=metric";
    private static final String CHANNEL_ID = "weather_notifications_channel";
    private static final int NOTIFICATION_ID_FOREGROUND = 1;
    private static final int NOTIFICATION_ID_WEATHER = 2;
    private static final String TAG = "myweather";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        if (isNetworkConnected()) {
            createNotificationChannel();
            startForeground(NOTIFICATION_ID_FOREGROUND, createForegroundNotification());

            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            String city = sharedPreferences.getString("lastCity", "Mumbai");
            fetchWeatherData(city);
        } else {
            Log.d(TAG, "Service Stopped due to no network connection");
            stopSelf();
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void fetchWeatherData(String city) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String apiUrl = URL1 + city.trim() + URL2;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, apiUrl, null,
                response -> handleApiResponse(response, city),
                error -> {
                    Log.e(TAG, "API request error: " + error.getMessage());
                    stopSelf();
                }
        );

        queue.add(jsonObjectRequest);
    }

    private void handleApiResponse(JSONObject response, String city) {
        try {
            double temp = response.getJSONObject("main").getDouble("temp");
            String temperature = (int) Math.rint(temp) + "°C";
            String weatherDescription = response.getJSONArray("weather").getJSONObject(0).getString("description");
            int weatherId = response.getJSONArray("weather").getJSONObject(0).getInt("id");

            Notification notification = createWeatherNotification(city, weatherDescription, temperature, weatherId);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(this).notify(NOTIFICATION_ID_WEATHER, notification);
                Log.d(TAG, "Weather data fetched and notification sent");
            } else {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted");
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage());
        } finally {
            stopSelf();
        }
    }

    private Notification createForegroundNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Weather Service")
                .setContentText("Fetching weather data...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    private Notification createWeatherNotification(String city, String weatherDescription, String temperature, int weatherId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(city + ": " + capitalizeEveryWord(weatherDescription))
                .setLargeIcon(getWeatherIcon(this, weatherId))
                .setContentText(temperature + " - See forecast")
                .setSubText("Today")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Weather Notifications", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Notifications related to weather updates");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        Log.d(TAG, "Notification channel created");
    }

    private Bitmap getWeatherIcon(Context context, int weatherId) {
        int drawableId;

        if (weatherId >= 200 && weatherId < 300) {
            drawableId = R.drawable.thunderstrom;
        } else if (weatherId >= 300 && weatherId < 500) {
            drawableId = R.drawable.drizzle;
        } else if (weatherId >= 500 && weatherId < 600) {
            drawableId = R.drawable.rain;
        } else if (weatherId >= 600 && weatherId < 700) {
            drawableId = R.drawable.snow;
        } else if (weatherId >= 700 && weatherId < 800) {
            drawableId = R.drawable.mist;
        } else if (weatherId == 800) {
            drawableId = R.drawable.sun;
        } else if (weatherId == 801) {
            drawableId = R.drawable.few_clouds;
        } else if (weatherId == 802) {
            drawableId = R.drawable.scattered_clouds;
        } else {
            drawableId = R.drawable.scattered_clouds;
        }

        return BitmapFactory.decodeResource(context.getResources(), drawableId);
    }

    private static String capitalizeEveryWord(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        String[] words = str.split("\\s+");

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }
}
