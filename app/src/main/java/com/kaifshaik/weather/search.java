package com.kaifshaik.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class search extends AppCompatActivity {
    private AutoCompleteTextView editText;
    private ArrayAdapter<String> adapter;
    private ImageView backbutton;
    private TextView[] topCities;
    private TextView[] topWorldCities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.search_activity_bg));

        initializeViews();
        setupCityClickListeners();
        setupAutoComplete();
        setupBackButton();
        setupTextWatcher();
    }

    private void initializeViews() {
        topCities = new TextView[] {
                findViewById(R.id.topcities1), findViewById(R.id.topcities2),
                findViewById(R.id.topcities3), findViewById(R.id.topcities4),
                findViewById(R.id.topcities5), findViewById(R.id.topcities6)
        };

        topWorldCities = new TextView[] {
                findViewById(R.id.topworldcities1), findViewById(R.id.topworldcities2),
                findViewById(R.id.topworldcities3), findViewById(R.id.topworldcities4),
                findViewById(R.id.topworldcities5), findViewById(R.id.topworldcities6),
                findViewById(R.id.topworldcities7), findViewById(R.id.topworldcities8),
                findViewById(R.id.topworldcities9)
        };

        backbutton = findViewById(R.id.backButton);
    }

    private void setupCityClickListeners() {
        // Assuming cities and topCities are aligned in order
        String[] cityNames = {
                "New Delhi", "Mumbai", "Chennai", "Hyderabad", "Bengaluru", "Kolkata",
                "Los Angeles", "Paris", "Rome", "Beijing", "London", "Mexico City",
                "Tokyo", "Singapore", "Chicago"
        };

        // Combine the topCities and topWorldCities arrays
        TextView[] allCities = new TextView[topCities.length + topWorldCities.length];
        System.arraycopy(topCities, 0, allCities, 0, topCities.length);
        System.arraycopy(topWorldCities, 0, allCities, topCities.length, topWorldCities.length);

        for (int i = 0; i < allCities.length; i++) {
            if (i < cityNames.length) {
                setClickListener(allCities[i], cityNames[i]);
            }
        }
    }

    private void setClickListener(TextView textView, String city) {
        textView.setOnClickListener(v -> {
            textView.setTextColor(ContextCompat.getColor(search.this, R.color.search));
            setTopCities(city);
        });
    }



    private void setupAutoComplete() {
        List<String> cityList = readCityNamesFromAsset();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cityList);
        editText = findViewById(R.id.editTextText);
        editText.setAdapter(adapter);
    }

    private void setupBackButton() {
        backbutton.setOnClickListener(v -> finish());
    }

    private void setupTextWatcher() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    fetchCitySuggestions(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editText.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = (String) parent.getItemAtPosition(position);
            String cityName = selectedCity.contains(",") ? selectedCity.split(",", 2)[0] : selectedCity;

            editText.setText(cityName);
            editText.setSelection(Math.min(cityName.length(), editText.getText().length()));

            Intent intent = new Intent(search.this, MainActivity.class);
            intent.putExtra("city", cityName);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("lastCity", cityName);
            editor.apply();

            finish();
            startActivity(intent);
        });
    }

    private void fetchCitySuggestions(String query) {
        Log.d("CityLoader", "fetchCitySuggestions called with query: " + query);
        if (adapter != null) {
            adapter.getFilter().filter(query);
            Log.d("CityLoader", "Filter applied.");
        } else {
            Log.d("CityLoader", "Adapter is null, cannot apply filter.");
        }
    }

    private List<String> readCityNamesFromAsset() {
        List<String> cityNames = new ArrayList<>();
        try (InputStream is = getAssets().open("cities.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = reader.readLine()) != null) {
                cityNames.add(line);
            }
            Log.d("CityLoader", "Read " + cityNames.size() + " city names.");
        } catch (IOException e) {
            Log.e("CityLoader", "Error reading city names from asset", e);
        }
        return cityNames;
    }

    private void setTopCities(String city) {
        Intent intent = new Intent(search.this, MainActivity.class);
        intent.putExtra("city", city);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastCity", city);
        editor.apply();

        finish();
        startActivity(intent);
    }
}