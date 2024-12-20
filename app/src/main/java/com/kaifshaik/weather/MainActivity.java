package com.kaifshaik.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements LocationListener{
    ImageView searchBtn, icon, seekbarImageleft, seekbarImageRight, locationSymbol, settingsBtn, infoSymbol;
    TextView city, temp, type, feelslike, visibility, airpressure, windspeed, sealevel, humidity, infoCardText, hourlyheading, dailyheading;
    TextView seekbarTimeLeft, seekbarTimeRight, seekbarTextLeft, seekbarTextRight, textView;
    SeekBar seekBar;
    LottieAnimationView progressBar, lottieBackground;
    CardView infoCard;
    LocationManager locationManager;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView hourly1Time, hourly2Time, hourly3Time, hourly4Time, hourly5Time, hourly6Time, hourly7Time, hourly8Time;
    ImageView hourly1Image, hourly2Image, hourly3Image, hourly4Image, hourly5Image, hourly6Image, hourly7Image, hourly8Image;
    ImageView hourly1Image2, hourly2Image2, hourly3Image2, hourly4Image2, hourly5Image2, hourly6Image2, hourly7Image2, hourly8Image2;
    TextView hourly1Temp, hourly2Temp, hourly3Temp, hourly4Temp, hourly5Temp, hourly6Temp, hourly7Temp, hourly8Temp;
    TextView hourly1Rain, hourly2Rain, hourly3Rain, hourly4Rain, hourly5Rain, hourly6Rain, hourly7Rain, hourly8Rain;
    TextView daily1Date, daily2Date, daily3Date, daily4Date, daily5Date, daily6Date;
    ImageView daily1Image, daily2Image, daily3Image, daily4Image, daily5Image, daily6Image;
    TextView daily1Min, daily2Min, daily3Min, daily4Min, daily5Min, daily6Min;
    TextView daily1Max, daily2Max, daily3Max, daily4Max, daily5Max, daily6Max, dailySlash6;
    LinearLayout daily6;
    TextView airqualitymain, airqualitydescription;
    TextView basic1, basic2, basic3, basic4, basic5, basic6, airqualityheading, slash1, slash2, slash3, slash4, slash5;
    SeekBar airqualityseekbar;
    ObjectAnimator animator1, animator2;
    RequestQueue requestQueue;
    String refreshUrl, refreshForcastUrl, refreshAirQualityUrl, refreshCityName;
    boolean refreshInfo;
    LinearLayout parentLayout, hourlyline, dailyline, airqualityline;
    TextView displayWindSpeedUnits, displayAirPressureUnits, displayVisibilityUnits, displaySeaPressureUnits;
    String unitTemperature, unitWind, unitAirPressure, unitVisibility, unitSeaPressure, switchState1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        grantPermissions();

        initializeViews();

        ConstraintLayout constraintLayout = findViewById(R.id.main_layout);
        setBackgroundAsPerDayNight(constraintLayout);

        refreshInfo = true;
        settingsBtn = findViewById(R.id.settings_button);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, settings.class);
                startActivity(intent);
            }
        });
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
                setBackgroundAsPerDayNight(constraintLayout);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        requestQueue = Volley.newRequestQueue(MainActivity.this);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, search.class);
                startActivity(intent);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        // Retrieve the string value
        String savedString = sharedPreferences.getString("lastCity", "YOUR_COUNTRY");
        unitTemperature = sharedPreferences.getString("unit_temperature", "celsius");
        unitWind = sharedPreferences.getString("unit_wind", "kilometresperhour");
        unitAirPressure = sharedPreferences.getString("unit_airpressure", "hectopascals");
        unitVisibility = sharedPreferences.getString("unit_visibility", "kilometres");
        unitSeaPressure = sharedPreferences.getString("unit_seapressure", "poundspersquareinch");
        switchState1 = sharedPreferences.getString("switch1_state", "OFF");

        if(!savedString.equals("YOUR_COUNTRY")){
            retriveWeatherDataWithCity(savedString);
        }else{
            retriveWeatherDataWithCity("Mumbai");
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("city")) {
            String message = intent.getStringExtra("city");
            assert message != null;
            retriveWeatherDataWithCity(message);
        }
    }

    private void initializeViews() {
        slash1 = findViewById(R.id.slash1);
        slash2 = findViewById(R.id.slash2);
        slash3 = findViewById(R.id.slash3);
        slash4 = findViewById(R.id.slash4);
        slash5 = findViewById(R.id.slash5);
        infoCardText = findViewById(R.id.infoCardText);
        infoSymbol = findViewById(R.id.infoSymbol);
        hourlyheading = findViewById(R.id.hourlyheading);
        hourlyline = findViewById(R.id.hourlyline);
        dailyheading = findViewById(R.id.dailyheading);
        dailyline = findViewById(R.id.dailyline);
        basic1 = findViewById(R.id.basic1);
        basic2 = findViewById(R.id.basic2);
        basic3 = findViewById(R.id.basic3);
        basic4 = findViewById(R.id.basic4);
        basic5 = findViewById(R.id.basic5);
        basic6 = findViewById(R.id.basic6);
        airqualityheading = findViewById(R.id.airqualityheading);
        airqualityline = findViewById(R.id.airqualityline);

        airqualityseekbar = findViewById(R.id.airqualitseekbar);
        airqualitydescription = findViewById(R.id.airqualitydescription);
        airqualitymain = findViewById(R.id.airqualitymain);

        displayWindSpeedUnits = findViewById(R.id.display_windspeed_units);
        displayAirPressureUnits = findViewById(R.id.display_airpressure_units);
        displayVisibilityUnits = findViewById(R.id.display_visibility_units);
        displaySeaPressureUnits = findViewById(R.id.display_sealevel_units);

        // Daily Forcast Displays
        daily6 = findViewById(R.id.daily6);
        dailySlash6 = findViewById(R.id.dailyslash6);
        daily1Date = findViewById(R.id.daily1Date);
        daily2Date = findViewById(R.id.daily2Date);
        daily3Date = findViewById(R.id.daily3Date);
        daily4Date = findViewById(R.id.daily4Date);
        daily5Date = findViewById(R.id.daily5Date);
        daily6Date = findViewById(R.id.daily6Date);
        daily1Image = findViewById(R.id.daily1Image);
        daily2Image = findViewById(R.id.daily2Image);
        daily3Image = findViewById(R.id.daily3Image);
        daily4Image = findViewById(R.id.daily4Image);
        daily5Image = findViewById(R.id.daily5Image);
        daily6Image = findViewById(R.id.daily6Image);
        daily1Min = findViewById(R.id.daily1Min);
        daily2Min = findViewById(R.id.daily2Min);
        daily3Min = findViewById(R.id.daily3Min);
        daily4Min = findViewById(R.id.daily4Min);
        daily5Min = findViewById(R.id.daily5Min);
        daily6Min = findViewById(R.id.daily6Min);
        daily1Max = findViewById(R.id.daily1Max);
        daily2Max = findViewById(R.id.daily2Max);
        daily3Max = findViewById(R.id.daily3Max);
        daily4Max = findViewById(R.id.daily4Max);
        daily5Max = findViewById(R.id.daily5Max);
        daily6Max = findViewById(R.id.daily6Max);

        // 3-Hourly Focarcast Displays
        hourly1Time = findViewById(R.id.hourly1Time);
        hourly1Image = findViewById(R.id.hourly1Image);
        hourly1Image2 = findViewById(R.id.hourly1Image2);
        hourly1Temp = findViewById(R.id.hourly1Temp);
        hourly1Rain = findViewById(R.id.hourly1Rain);
        hourly2Time = findViewById(R.id.hourly2Time);
        hourly2Image = findViewById(R.id.hourly2Image);
        hourly2Image2 = findViewById(R.id.hourly2Image2);
        hourly2Temp = findViewById(R.id.hourly2Temp);
        hourly2Rain = findViewById(R.id.hourly2Rain);
        hourly3Time = findViewById(R.id.hourly3Time);
        hourly3Image = findViewById(R.id.hourly3Image);
        hourly3Image2 = findViewById(R.id.hourly3Image2);
        hourly3Temp = findViewById(R.id.hourly3Temp);
        hourly3Rain = findViewById(R.id.hourly3Rain);
        hourly4Time = findViewById(R.id.hourly4Time);
        hourly4Image = findViewById(R.id.hourly4Image);
        hourly4Image2 = findViewById(R.id.hourly4Image2);
        hourly4Temp = findViewById(R.id.hourly4Temp);
        hourly4Rain = findViewById(R.id.hourly4Rain);
        hourly5Time = findViewById(R.id.hourly5Time);
        hourly5Image = findViewById(R.id.hourly5Image);
        hourly5Image2 = findViewById(R.id.hourly5Image2);
        hourly5Temp = findViewById(R.id.hourly5Temp);
        hourly5Rain = findViewById(R.id.hourly5Rain);
        hourly6Time = findViewById(R.id.hourly6Time);
        hourly6Image = findViewById(R.id.hourly6Image);
        hourly6Image2 = findViewById(R.id.hourly6Image2);
        hourly6Temp = findViewById(R.id.hourly6Temp);
        hourly6Rain = findViewById(R.id.hourly6Rain);
        hourly7Time = findViewById(R.id.hourly7Time);
        hourly7Image = findViewById(R.id.hourly7Image);
        hourly7Image2 = findViewById(R.id.hourly7Image2);
        hourly7Temp = findViewById(R.id.hourly7Temp);
        hourly7Rain = findViewById(R.id.hourly7Rain);
        hourly8Time = findViewById(R.id.hourly8Time);
        hourly8Image = findViewById(R.id.hourly8Image);
        hourly8Image2 = findViewById(R.id.hourly8Image2);
        hourly8Temp = findViewById(R.id.hourly8Temp);
        hourly8Rain = findViewById(R.id.hourly8Rain);

        textView = findViewById(R.id.textView);
        parentLayout = findViewById(R.id.parent_layout);
        // Enable LayoutTransition on the parent layout
        LayoutTransition layoutTransition = new LayoutTransition();
        parentLayout.setLayoutTransition(layoutTransition);

        locationSymbol = findViewById(R.id.locationSymbol);
        infoCard = findViewById(R.id.infoCard);
        seekbarTextLeft = findViewById(R.id.seekbarTextLeft);
        seekbarTextRight = findViewById(R.id.seekbarTextRight);
        seekbarImageleft = findViewById(R.id.seekbarImageLeft);
        seekbarImageRight = findViewById(R.id.seekbarImageRight);
        seekbarTimeLeft = findViewById(R.id.seekbarTimeLeft);
        seekbarTimeRight = findViewById(R.id.seekbarTimeRight);
        seekBar = findViewById(R.id.seekbar);
        sealevel = findViewById(R.id.display_sealevel);
        windspeed = findViewById(R.id.display_windspeed);
        airpressure = findViewById(R.id.display_airpressure);
        visibility = findViewById(R.id.display_visibility);
        feelslike = findViewById(R.id.display_feelslike);
        humidity = findViewById(R.id.display_humidity);
        icon = findViewById(R.id.display_icon);
        city = findViewById(R.id.display_city);
        temp = findViewById(R.id.display_temp);
        type = findViewById(R.id.display_type);
        lottieBackground = findViewById(R.id.lottie_background);
        progressBar = findViewById(R.id.progressBar);
        progressBar.bringToFront();
        progressBar.setVisibility(View.GONE);
        searchBtn = findViewById(R.id.search_button);
    }

    public void setBackgroundAsPerDayNight(ConstraintLayout constraintLayout) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 6 && hour < 18) {
            // Daytime
            constraintLayout.setBackgroundResource(R.drawable.background3);
            getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.status2));
            progressBar.setAnimation(R.raw.weather_loader);
            infoCardText.setTextColor(getResources().getColor(R.color.background));
            infoSymbol.setImageResource(R.drawable.baseline_info_24);
            hourlyheading.setTextColor(getResources().getColor(R.color.background));
            hourlyline.setBackgroundColor(getResources().getColor(R.color.background));
            dailyheading.setTextColor(getResources().getColor(R.color.background));
            dailyline.setBackgroundColor(getResources().getColor(R.color.background));
            basic1.setTextColor(getResources().getColor(R.color.background));
            basic2.setTextColor(getResources().getColor(R.color.background));
            basic3.setTextColor(getResources().getColor(R.color.background));
            basic4.setTextColor(getResources().getColor(R.color.background));
            basic5.setTextColor(getResources().getColor(R.color.background));
            basic6.setTextColor(getResources().getColor(R.color.background));
            slash1.setTextColor(getResources().getColor(R.color.background));
            slash2.setTextColor(getResources().getColor(R.color.background));
            slash3.setTextColor(getResources().getColor(R.color.background));
            slash4.setTextColor(getResources().getColor(R.color.background));
            slash5.setTextColor(getResources().getColor(R.color.background));
            dailySlash6.setTextColor(getResources().getColor(R.color.background));
            airqualityheading.setTextColor(getResources().getColor(R.color.background));
            airqualityline.setBackgroundColor(getResources().getColor(R.color.background));
            seekbarTextLeft.setTextColor(getResources().getColor(R.color.background));
            seekbarTextRight.setTextColor(getResources().getColor(R.color.background));
            seekBar.setProgressDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.track, null));

        } else {
            // Nighttime
            constraintLayout.setBackgroundResource(R.drawable.background4);
            getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.status));
            progressBar.setAnimation(R.raw.weather_loader2);
            infoCardText.setTextColor(getResources().getColor(R.color.transparent));
            infoSymbol.setImageResource(R.drawable.baseline_info_34);
            hourlyheading.setTextColor(getResources().getColor(R.color.transparent));
            hourlyline.setBackgroundColor(getResources().getColor(R.color.transparent));
            dailyheading.setTextColor(getResources().getColor(R.color.transparent));
            dailyline.setBackgroundColor(getResources().getColor(R.color.transparent));
            basic1.setTextColor(getResources().getColor(R.color.transparent));
            basic2.setTextColor(getResources().getColor(R.color.transparent));
            basic3.setTextColor(getResources().getColor(R.color.transparent));
            basic4.setTextColor(getResources().getColor(R.color.transparent));
            basic5.setTextColor(getResources().getColor(R.color.transparent));
            basic6.setTextColor(getResources().getColor(R.color.transparent));
            slash1.setTextColor(getResources().getColor(R.color.transparent));
            slash2.setTextColor(getResources().getColor(R.color.transparent));
            slash3.setTextColor(getResources().getColor(R.color.transparent));
            slash4.setTextColor(getResources().getColor(R.color.transparent));
            slash5.setTextColor(getResources().getColor(R.color.transparent));
            dailySlash6.setTextColor(getResources().getColor(R.color.transparent));
            airqualityheading.setTextColor(getResources().getColor(R.color.transparent));
            airqualityline.setBackgroundColor(getResources().getColor(R.color.transparent));
            seekbarTextLeft.setTextColor(getResources().getColor(R.color.transparent));
            seekbarTextRight.setTextColor(getResources().getColor(R.color.transparent));
            seekBar.setProgressDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.track2, null));
        }
    }

    private void refreshData() {
        if(refreshAirQualityUrl.equals("NULL")){
            double[] coordinates = GeocodingService.getCoordinates(MainActivity.this, refreshCityName.trim());
            if (coordinates != null) {
                refreshAirQualityUrl = "https://api.openweathermap.org/data/2.5/air_pollution?lat="+ coordinates[0] + "&lon=" + coordinates[1] + "&appid=be4c4fe147b1365b0866c895dc758c51";
            } else {
                refreshAirQualityUrl = "NULL";
                Log.d("myapi", "Coordinates not found for city: " + refreshCityName);
            }
        }
        retriveData(refreshUrl, refreshForcastUrl, refreshAirQualityUrl);
        if(refreshInfo){
            Toast.makeText(MainActivity.this, "Refreshed", Toast.LENGTH_SHORT).show();
        }
    }

    private void retriveWeatherDataWithCoordinates(String latitude, String longitude, String cityName){
        // Perform operations with the city name

        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=be4c4fe147b1365b0866c895dc758c51&units=metric";
        String forcastUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=" + latitude + "&lon=" + longitude + "&appid=be4c4fe147b1365b0866c895dc758c51&units=metric";
        String airQualityUrl = "https://api.openweathermap.org/data/2.5/air_pollution?lat=" + latitude + "&lon=" + longitude + "&appid=be4c4fe147b1365b0866c895dc758c51";

        city.setText(Objects.requireNonNull(capitalizeFirstLetter(cityName)).trim());
        retriveData(url, forcastUrl, airQualityUrl);
        infoCard.setVisibility(View.GONE);
        locationSymbol.setVisibility(View.VISIBLE);
    }

    private void retriveData(String url, String forcastUrl, String airQualityUrl){
        refreshUrl = url;
        refreshForcastUrl = forcastUrl;
        refreshAirQualityUrl = airQualityUrl;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // temperature
                    double tem = response.getJSONObject("main").getDouble("temp");
                    int val = (int)Math.rint(tem);
                    // type
                    String Type = response.getJSONArray("weather").getJSONObject(0).getString("description");
                    // icon id
                    int Id = response.getJSONArray("weather").getJSONObject(0).getInt("id");
                    // humidity
                    String Humidity = response.getJSONObject("main").getString("humidity");
                    // feels like
                    double fee = response.getJSONObject("main").getDouble("feels_like");
                    int FeelsLike = (int)Math.rint(fee);
                    // visibility
                    int Visibile = response.getInt("visibility") / 1000;
                    String Visibility = Visibile + "";
                    // Air Pressure
                    int pres = response.getJSONObject("main").getInt("pressure");
                    String Pressure = pres + "";
                    // Wind Speed
                    double wind = response.getJSONObject("wind").getDouble("speed") * 3.6f;
                    int Speed = (int) wind;
                    String WindSpeed = Speed + "";
                    // Sea Level
                    double sea = response.getJSONObject("main").getDouble("sea_level");
                    int Level = (int) sea;
                    String SeaLevel = String.valueOf(Level);
                    // Sunrise and Sunset
                    long rise = response.getJSONObject("sys").getLong("sunrise");
                    long set = response.getJSONObject("sys").getLong("sunset");

                    setBasicData(SeaLevel, WindSpeed, Pressure, Visibility, FeelsLike, Humidity, Id, Type, val);
                    setSunriseSunsetData(rise, set);

                    // Retrive Forcast Data
                    retriveForcastData(forcastUrl);
                    retriveAirQualityData(airQualityUrl);

                    refreshInfo = true;
                } catch (JSONException e) {
                    refreshInfo = false;
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                refreshInfo = false;
                if (error instanceof NetworkError) {
                    Toast.makeText(MainActivity.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void retriveAirQualityData(String airQualityUrl) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, airQualityUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray list = response.getJSONArray("list");
                            JSONObject firstItem = list.getJSONObject(0);
                            JSONObject main = firstItem.getJSONObject("main");
                            int aqi = main.getInt("aqi");

                            // Now you can use the parsed data as needed
                            // Example: Display or process the retrieved air quality information
                            setAirQualityData(aqi);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            refreshInfo = false;
                            // Handle JSON parsing errors here
                            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NetworkError) {
                    Toast.makeText(MainActivity.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }else {
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                error.printStackTrace();
                refreshInfo = false;
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    private void setAirQualityData(int aqi) {
        airqualityseekbar.setMax(600);
        airqualityseekbar.setProgress(aqi*100);
        String quality;
        String description;
        if(aqi == 1){
            quality = "Good";
            description = "Air quality is safe, no health impacts are expected.";
        }else if(aqi == 2){
            quality = "Fair";
            description = "Air quality is acceptable, but sensitive individuals may experience minor health issues.";
        }else if(aqi == 3){
            quality = "Moderate";
            description = "Air quality is okay, but some people may experience health concerns, particularly if sensitive.";
        }else if(aqi == 4){
            quality = "Poor";
            description = "Air quality is unhealthy for sensitive groups, others are less likely to be affected.";
        }else if(aqi == 5){
            quality = "Very Poor";
            description = "Air quality is unhealthy, and everyone may experience health effects.";
        }else{
            quality = "No Data";
            description = "";
        }

        airqualitymain.setText(quality);
        airqualitydescription.setText(description);
    }

    private void retriveForcastData(String forcastUrl) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, forcastUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray list = jsonObject.getJSONArray("list");
                    Map<String, JSONObject> dailyForecasts = new TreeMap<>();
                    List<JSONObject> hourlyForecasts = new ArrayList<>();

                    // Get the current time
                    ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

                    for (int i = 0; i < list.length(); i++) {
                        JSONObject forecast = list.getJSONObject(i);
                        String dateTime = forecast.getString("dt_txt");

                        // Convert UTC time to local time
                        String localTime = convertUtcToLocalTime(dateTime);
                        LocalDateTime forecastDateTime = LocalDateTime.parse(localTime, DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));

                        String[] localTimeParts = localTime.split(" ");
                        String date = localTimeParts[0];
                        String time = localTimeParts[1] + " " + localTimeParts[2];

                        // Add to daily forecasts if not already added for the day
                        dailyForecasts.putIfAbsent(date, forecast);

                        // Check if this forecast is within the next eight 3-hourly forecasts from now
                        if (hourlyForecasts.size() < 8) {
                            ZonedDateTime forecastZonedDateTime = forecastDateTime.atZone(ZoneId.systemDefault());
                            if (forecastZonedDateTime.isAfter(now)) {
                                forecast.put("time", time); // Add time part only to forecast
                                hourlyForecasts.add(forecast);
                            }
                        }
                    }

                    String[] times= new String[8];
                    int[] temps = new int[8];
                    int[] icons = new int[8];
                    String[] rains = new String[8];

                    for (int i = 0; i < 8 && i < hourlyForecasts.size(); i++) {
                        JSONObject hourlyForecast = hourlyForecasts.get(i);
                        times[i] = hourlyForecast.getString("time");
                        temps[i] = hourlyForecast.getJSONObject("main").getInt("temp");
                        icons[i] = hourlyForecast.getJSONArray("weather").getJSONObject(0).getInt("id");
                        rains[i] = (int) (hourlyForecast.getDouble("pop") * 100) + "%";
                    }

                    for (int i = 0; i < times.length; i++) {
                        if (times[i].startsWith("0")) {
                            times[i] = times[i].substring(1);
                        }
                    }

                    // Process daily forecasts
                    int dailySize = dailyForecasts.size();
                    String[] dailyDates = new String[dailySize];
                    int[] dailyMinTemps = new int[dailySize];
                    int[] dailyMaxTemps = new int[dailySize];
                    int[] dailyIcons = new int[dailySize];

                    int index = 0;
                    for (Map.Entry<String, JSONObject> entry : dailyForecasts.entrySet()) {
                        String date = entry.getKey();
                        JSONObject dailyForecast = entry.getValue();

                        dailyDates[index] = getDailyForcastDates(date);
                        dailyMinTemps[index] = dailyForecast.getJSONObject("main").getInt("temp_min");
                        dailyMaxTemps[index] = dailyForecast.getJSONObject("main").getInt("feels_like");
                        dailyIcons[index] = dailyForecast.getJSONArray("weather").getJSONObject(0).getInt("id");
                        index++;
                    }

                    // Setting 3-Hourly Forcast Data
                    setHourlyForcastData(times, temps, icons, rains);
                    // Setting Daily Forcast Data
                    setDailyForcastData(dailyDates, dailyIcons, dailyMinTemps, dailyMaxTemps);

                } catch (JSONException e) {
                    refreshInfo = false;
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                refreshInfo = false;
                if (error instanceof NetworkError) {
                    Toast.makeText(MainActivity.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

        requestQueue.add(stringRequest);
    }

    private void setDailyForcastData(String[] dailyDates, int[] dailyIcons, int[] dailyMinTemps, int[] dailyMaxTemps) {

        daily1Date.setText(dailyDates[0]);
        daily2Date.setText(dailyDates[1]);
        daily3Date.setText(dailyDates[2]);
        daily4Date.setText(dailyDates[3]);
        daily5Date.setText(dailyDates[4]);

        setWeatherIcon(dailyIcons[0], daily1Image);
        setWeatherIcon(dailyIcons[1], daily2Image);
        setWeatherIcon(dailyIcons[2], daily3Image);
        setWeatherIcon(dailyIcons[3], daily4Image);
        setWeatherIcon(dailyIcons[4], daily5Image);

        handleTemperature(dailyMinTemps[0], daily1Min);
        handleTemperature(dailyMinTemps[1], daily2Min);
        handleTemperature(dailyMinTemps[2], daily3Min);
        handleTemperature(dailyMinTemps[3], daily4Min);
        handleTemperature(dailyMinTemps[4], daily5Min);

        handleTemperature(dailyMaxTemps[0], daily1Max);
        handleTemperature(dailyMaxTemps[1], daily2Max);
        handleTemperature(dailyMaxTemps[2], daily3Max);
        handleTemperature(dailyMaxTemps[3], daily4Max);
        handleTemperature(dailyMaxTemps[4], daily5Max);

        if(dailyDates.length > 5 && dailyIcons.length > 5 && dailyMinTemps.length > 5 && dailyMaxTemps.length > 5){
            daily6Date.setText(dailyDates[5]);
            setWeatherIcon(dailyIcons[5], daily6Image);
            handleTemperature(dailyMinTemps[5], daily6Min);
            handleTemperature(dailyMaxTemps[5], daily6Max);
            dailySlash6.setText(R.string.slash);
        }else{
            daily6.setVisibility(View.GONE);
        }
    }

    public static String getDailyForcastDates(String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        String dayOfWeek;
        if (localDate.equals(today)) {
            dayOfWeek = "Today";
        } else if (localDate.equals(tomorrow)) {
            dayOfWeek = "Tomorrow";
        } else {
            dayOfWeek = localDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        }

        String formattedDate = localDate.format(DateTimeFormatter.ofPattern("dd MMM"));
        return formattedDate + " " + dayOfWeek;
    }

    private void setHourlyForcastData(String[] times, int[] temps, int[] icons, String[] rains) {

        hourly1Time.setText(times[0]);
        setWeatherIcon(icons[0], hourly1Image);
        handleTemperature(temps[0], hourly1Temp);
        if(!rains[0].equals("0%")){
            hourly1Image2.setImageResource(R.drawable.umbrella);
            hourly1Rain.setText(rains[0]);
        }else{
            hourly1Image2.setImageResource(R.drawable.no_rain);
            hourly1Rain.setText("");
        }

        hourly2Time.setText(times[1]);
        setWeatherIcon(icons[1], hourly2Image);
        handleTemperature(temps[1], hourly2Temp);
        if(!rains[1].equals("0%")){
            hourly2Image2.setImageResource(R.drawable.umbrella);
            hourly2Rain.setText(rains[1]);
        }else{
            hourly2Image2.setImageResource(R.drawable.no_rain);
            hourly2Rain.setText("");
        }


        hourly3Time.setText(times[2]);
        setWeatherIcon(icons[2], hourly3Image);
        handleTemperature(temps[2], hourly3Temp);
        if(!rains[2].equals("0%")){
            hourly3Image2.setImageResource(R.drawable.umbrella);
            hourly3Rain.setText(rains[2]);
        }else{
            hourly3Image2.setImageResource(R.drawable.no_rain);
            hourly3Rain.setText("");
        }

        hourly4Time.setText(times[3]);
        setWeatherIcon(icons[3], hourly4Image);
        handleTemperature(temps[3], hourly4Temp);
        if(!rains[3].equals("0%")){
            hourly4Image2.setImageResource(R.drawable.umbrella);
            hourly4Rain.setText(rains[3]);
        }else{
            hourly4Image2.setImageResource(R.drawable.no_rain);
            hourly4Rain.setText("");
        }

        hourly5Time.setText(times[4]);
        setWeatherIcon(icons[4], hourly5Image);
        handleTemperature(temps[4], hourly5Temp);
        if(!rains[4].equals("0%")){
            hourly5Image2.setImageResource(R.drawable.umbrella);
            hourly5Rain.setText(rains[4]);
        }else{
            hourly5Image2.setImageResource(R.drawable.no_rain);
            hourly5Rain.setText("");
        }

        hourly6Time.setText(times[5]);
        setWeatherIcon(icons[5], hourly6Image);
        handleTemperature(temps[5], hourly6Temp);
        if(!rains[5].equals("0%")){
            hourly6Image2.setImageResource(R.drawable.umbrella);
            hourly6Rain.setText(rains[5]);
        }else{
            hourly6Image2.setImageResource(R.drawable.no_rain);
            hourly6Rain.setText("");
        }

        hourly7Time.setText(times[6]);
        setWeatherIcon(icons[6], hourly7Image);
        handleTemperature(temps[6], hourly7Temp);
        if(!rains[6].equals("0%")){
            hourly7Image2.setImageResource(R.drawable.umbrella);
            hourly7Rain.setText(rains[6]);
        }else{
            hourly7Image2.setImageResource(R.drawable.no_rain);
            hourly7Rain.setText("");
        }

        hourly8Time.setText(times[7]);
        setWeatherIcon(icons[7], hourly8Image);
        handleTemperature(temps[7], hourly8Temp);
        if(!rains[7].equals("0%")){
            hourly8Image2.setImageResource(R.drawable.umbrella);
            hourly8Rain.setText(rains[7]);
        }else{
            hourly8Image2.setImageResource(R.drawable.no_rain);
            hourly8Rain.setText("");
        }

        progressBar.setVisibility(View.GONE);
    }

    public String convertUtcToLocalTime(String utcDateTime) {
        // Parse UTC datetime string
        // Parse UTC datetime string
        LocalDateTime utcDateTimeObj = LocalDateTime.parse(utcDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Get the device's default timezone
        ZoneId deviceTimeZone = ZoneId.systemDefault();

        // Convert UTC datetime to local datetime
        ZonedDateTime localDateTime = utcDateTimeObj.atZone(ZoneOffset.UTC).withZoneSameInstant(deviceTimeZone);

        // Format the local datetime in 12-hour format with AM/PM without seconds
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        return localDateTime.format(formatter);
    }

    private void retriveWeatherDataWithCity(String cityw){
        refreshCityName = cityw;
        String airQualityUrl;
        // Perform operations with the city name
        progressBar.setVisibility(View.VISIBLE);

        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityw.trim() + "&appid=be4c4fe147b1365b0866c895dc758c51&units=metric";
        String forcastUrl = "https://api.openweathermap.org/data/2.5/forecast?q=" + cityw.trim() + "&appid=be4c4fe147b1365b0866c895dc758c51&units=metric";

//        String name = "kadapa";0
        double[] coordinates = GeocodingService.getCoordinates(MainActivity.this, cityw.trim());
        if (coordinates != null) {
            airQualityUrl = "https://api.openweathermap.org/data/2.5/air_pollution?lat="+ coordinates[0] + "&lon=" + coordinates[1] + "&appid=be4c4fe147b1365b0866c895dc758c51";

        } else {
            airQualityUrl = "NULL";
            Log.d("myapi", "Coordinates not found for city: " + cityw);
        }

        Log.d("myapi", airQualityUrl);

        city.setText(Objects.requireNonNull(capitalizeFirstLetter(cityw)).trim());
        retriveData(url, forcastUrl, airQualityUrl);
    }

    private void setBasicData(String SeaLevel, String WindSpeed, String Pressure, String Visibility, int FeelsLike, String Humidity,
                              int Id, String Type, int Temp) {
        handleSeaPressure(SeaLevel, sealevel);
        handleWind(WindSpeed, windspeed);
        handleAirPressure(Pressure, airpressure);
        handleVisibility(Visibility, visibility);
        humidity.setText(Humidity);
        setWeatherIcon(Id, icon);
        type.setText(capitalizeEveryWord(Type));
        handleTemperature(FeelsLike, feelslike);
        handleTemperature(Temp, temp);
    }

    private void handleSeaPressure(String pressure, TextView textView){
        if(unitSeaPressure.equals("hectopascals")){
            textView.setText(pressure);
            displaySeaPressureUnits.setText(R.string.pressure);
        }else if(unitSeaPressure.equals("poundspersquareinch")){
            int a = Integer.parseInt(pressure);
            double b = a * 0.0145038f;
            int c = (int) b;
            String d = String.valueOf(c);
            textView.setText(d);
            displaySeaPressureUnits.setText(R.string.pressure2);
        }
    }

    private void handleVisibility(String visibility, TextView textView){
        if(unitVisibility.equals("kilometres")){
            textView.setText(visibility);
            displayVisibilityUnits.setText(R.string.kilometer);
        }else if(unitVisibility.equals("metres")){
            int a = Integer.parseInt(visibility) * 1000;
            String b = String.valueOf(a);
            textView.setText(b);
            displayVisibilityUnits.setText(R.string.metres);
        }else{
            double a = Double.parseDouble(visibility);
            double b = a * 3280.84;
            int c = (int) b;
            String d = String.valueOf(c);
            textView.setText(d);
            displayVisibilityUnits.setText(R.string.feet);
        }
    }
    private void handleAirPressure(String pressure, TextView textView){
        if(unitAirPressure.equals("hectopascals")){
            textView.setText(pressure);
            displayAirPressureUnits.setText(R.string.pressure);
        }else if(unitAirPressure.equals("poundspersquareinch")){
            int a = Integer.parseInt(pressure);
            double b = a * 0.0145038f;
            int c = (int) b;
            String d = String.valueOf(c);
            textView.setText(d);
            displayAirPressureUnits.setText(R.string.pressure2);
        }
    }

    private void handleTemperature(int Temp, TextView textView) {
        if(unitTemperature.equals("celsius")){
            String t = Temp + "°";
            textView.setText(t);
        }else{
            int t = (Temp * 9/5) + 32;
            String te = t + "°";
            textView.setText(te);
        }
    }
    private void handleWind(String speed, TextView textView){
        if(unitWind.equals("kilometresperhour")){
            textView.setText(speed);
            displayWindSpeedUnits.setText(R.string.kilometerperhour);
        }else if(unitWind.equals("milesperhour")){
            int s = Integer.parseInt(speed);
            float f = s * 0.621371f;
            int las = (int) f;
            String last = las + "";
            textView.setText(last);
            displayWindSpeedUnits.setText(R.string.milesperhour);
        }else if(unitWind.equals("metrespersecond")){
            final double CONVERSION_FACTOR = 5.0 / 18.0;
            double s = Double.parseDouble(speed);
            double f = s * CONVERSION_FACTOR;
            int las = (int) f;
            String last = las + "";
            textView.setText(last);
            displayWindSpeedUnits.setText(R.string.metrespersecond);
        }
    }
    private void setSunriseSunsetData(long rise, long set) {
        // Convert Unix timestamp to ZonedDateTime for sunrise
        ZonedDateTime sunriseZonedDateTime = Instant.ofEpochSecond(rise)
                .atZone(ZoneId.systemDefault());

        // Convert Unix timestamp to ZonedDateTime for sunset
        ZonedDateTime sunsetZonedDateTime = Instant.ofEpochSecond(set)
                .atZone(ZoneId.systemDefault());

        // Format the time for display in 12-hour format with AM/PM
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a").withLocale(Locale.US);
        String formattedSunriseTime = sunriseZonedDateTime.format(formatter).toUpperCase();
        String formattedSunsetTime = sunsetZonedDateTime.format(formatter).toUpperCase();

        DateTimeFormatter onlyTime = DateTimeFormatter.ofPattern("HH:mm").withLocale(Locale.US);
        String onlyTimeFormat1 = sunsetZonedDateTime.format(onlyTime);
        String onlyTimeFormat2 = sunriseZonedDateTime.format(onlyTime);

        int maxTimeSunset = getTotalSeconds(onlyTimeFormat1); // 2 minutes for example
        int maxTimeSunrise = getTotalSeconds(onlyTimeFormat2); // 2 minutes for example
        int currentTime = getCurrentTimeInSeconds();

        if (currentTime >= maxTimeSunrise && currentTime <= maxTimeSunset) {
            // Daytime configuration
            configureSeekBarForDaytime(formattedSunriseTime, formattedSunsetTime, maxTimeSunrise, maxTimeSunset, currentTime);
        } else {
            // Nighttime configuration
            configureSeekBarForNighttime(formattedSunriseTime, formattedSunsetTime, maxTimeSunrise, maxTimeSunset, currentTime);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        animateSeekBarProgress();


//        startUpdatingSeekBar(maxTimeSunrise, maxTimeSunset);

    }
    private void animateSeekBarProgress() {
        // Get current progress of the SeekBar
        int currentProgress = seekBar.getProgress();
        if (animator1 == null || !animator1.isRunning()) {
            animator1 = ObjectAnimator.ofInt(seekBar, "progress", 0, currentProgress);
            animator1.setDuration(3000);
            animator1.setInterpolator(new LinearInterpolator());
            animator1.start();
        }
    }
    private void animate1ForResume(){
        int currentProgress = seekBar.getProgress();
        ObjectAnimator animator3 = ObjectAnimator.ofInt(seekBar, "progress", 0, currentProgress);
        animator3.setDuration(3000);
        animator3.setInterpolator(new LinearInterpolator());
        animator3.start();
    }
    @Override
    protected void onResume() {
        super.onResume();
        animate1ForResume();
    }

    private void animateAirQualitySeekBar(){
        int currentProgress = airqualityseekbar.getProgress();
        if (animator2 == null || !animator2.isRunning()) { // Check if the animator is null or not running
            animator2 = ObjectAnimator.ofInt(airqualityseekbar, "progress", 0, currentProgress);
            animator2.setDuration(3000);
            animator2.setInterpolator(new LinearInterpolator());
            animator2.start();
        }
    }
    private void configureSeekBarForDaytime(String formattedSunriseTime, String formattedSunsetTime, int maxTimeSunrise, int maxTimeSunset, int currentTime) {
        seekbarImageleft.setImageResource(R.drawable.sunrise);
        seekbarImageRight.setImageResource(R.drawable.sunset);
        seekbarTextLeft.setText(R.string.sunrise);
        seekbarTextRight.setText(R.string.sunset);
        seekbarTimeLeft.setText(formattedSunriseTime);
        seekbarTimeRight.setText(formattedSunsetTime);
        seekBar.setThumb(ResourcesCompat.getDrawable(getResources(), R.drawable.seekbarsun, null));

        // Calculate the duration from sunrise to sunset
        int duration = maxTimeSunset - maxTimeSunrise;
        seekBar.setMax(duration);

        // Calculate the progress based on the current time
        int progress = currentTime - maxTimeSunrise;
        seekBar.setProgress(progress);
    }

    private void configureSeekBarForNighttime(String formattedSunriseTime, String formattedSunsetTime, int maxTimeSunrise, int maxTimeSunset, int currentTime) {
        seekbarImageleft.setImageResource(R.drawable.sunset);
        seekbarImageRight.setImageResource(R.drawable.sunrise);
        seekbarTextLeft.setText(R.string.sunset);
        seekbarTextRight.setText(R.string.sunrise);
        seekbarTimeRight.setText(formattedSunriseTime);
        seekbarTimeLeft.setText(formattedSunsetTime);
        seekBar.setThumb(ResourcesCompat.getDrawable(getResources(), R.drawable.seekbarmoon, null));

        // Calculate the duration from sunset to the next sunrise
        int duration = (24 * 3600 - maxTimeSunset) + maxTimeSunrise;
        seekBar.setMax(duration);

        // Calculate the progress based on the current time
        int progress = (currentTime >= maxTimeSunset) ? (currentTime - maxTimeSunset) : (currentTime + 24 * 3600 - maxTimeSunset);
        seekBar.setProgress(progress);
    }
    private int getCurrentTimeInSeconds() {
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        return hours * 3600 + minutes * 60 + seconds;
    }
    public static int getTotalSeconds(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 3600 + minutes * 60;
    }

    private void setWeatherIcon(int id, ImageView imageView) {
        if(id >= 200 && id < 300){
            imageView.setImageResource(R.drawable.thunderstrom);
        }else if(id >= 300 && id < 500){
            imageView.setImageResource(R.drawable.drizzle);
        }else if(id == 500){
            imageView.setImageResource(R.drawable.drizzle);
        }else if(id >= 501 && id < 600){
            imageView.setImageResource(R.drawable.rain);
        }else if(id >= 600 && id < 701){
            imageView.setImageResource(R.drawable.snow);
        }else if(id >= 701 && id < 800){
            imageView.setImageResource(R.drawable.mist);
        }else if(id == 800){
            imageView.setImageResource(R.drawable.sun);
        }else if(id == 801){
            imageView.setImageResource(R.drawable.few_clouds);
        }else if(id == 802){
            imageView.setImageResource(R.drawable.scattered_clouds);
        }else if(id == 803 || id == 804){
            imageView.setImageResource(R.drawable.scattered_clouds);
        }
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String capitalizeEveryWord(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        String[] words = str.split("\\s+");

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                result.append(word.substring(1).toLowerCase());
            }
            result.append(" ");
        }

        return result.toString().trim();
    }

    public void removeInfoCard(View view) {
        infoCard.setVisibility(View.GONE);
    }

    public void setDataWithLocation(View view) {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
            Toast.makeText(this, "Allow Location Permission", Toast.LENGTH_SHORT).show();
        }else{
            checkLocationisEnabled();
            getLocation();
        }
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        String Bcity, Acity;
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            assert addresses != null;
            Acity = addresses.get(0).getLocality();
            Bcity = addresses.get(0).getSubLocality();
            String latitude = location.getLatitude() + "";
            String longitude = location.getLongitude() + "";

            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            // Create an editor
            SharedPreferences.Editor editor = sharedPreferences.edit();
            // Put the string value
            editor.putString("lastCity", Acity);
            // Apply the changes
            editor.apply();

            retriveWeatherDataWithCoordinates(latitude, longitude, Acity + ", "  + Bcity + ".");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }
    private void grantPermissions() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100);
            } else {
                // Permission is already granted
                Log.d("Permission", "Notification permission granted.");
            }
        } else {
            // No need to request notification permission for lower API levels
            Log.d("Permission", "Notification permission not required.");
        }
    }
    private void getLocation() {
        try{
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5, (LocationListener) this);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }
    private void checkLocationisEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try{
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(!gpsEnabled && !networkEnabled){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Enable GPS Service")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).setNegativeButton("Cancel", null)
                    .show();
        }else{
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void animate2(View view) {
        animateAirQualitySeekBar();
    }
    public void animate(View view){
        animateSeekBarProgress();
    }
}