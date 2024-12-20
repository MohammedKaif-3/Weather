package com.kaifshaik.weather;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeocodingService {

    private static final String TAG = "GeocodingService";

    public static double[] getCoordinates(Context context, String cityName) {
        if (context == null || cityName == null || cityName.trim().isEmpty()) {
            Log.e(TAG, "Invalid context or cityName");
            return null;
        }

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double lat = address.getLatitude();
                double lng = address.getLongitude();
                return new double[]{lat, lng};
            } else {
                Log.d(TAG, "No results found for city: " + cityName);
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException occurred: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Exception occurred: " + e.getMessage(), e);
        }

        return null;
    }
}
