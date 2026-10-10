package com.example.tripboxie.utils;

import com.example.tripboxie.BuildConfig;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeocodingHelper {

    public interface OnGeocodingResult {
        void onSuccess(double lat, double lng);
        void onError(String message);
    }

    public static void getCoordinatesFromAddress(String address, OnGeocodingResult callback) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String result = null;
            try {
                String encodedAddress = address.replace(" ", "+");
                String apiKey = BuildConfig.GOOGLE_MAPS_API_KEY;
                String urlString = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                        encodedAddress + "&key=" + apiKey;

                Log.d("GeocodingHelper", "Geocoding: " + encodedAddress);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                result = sb.toString();
                reader.close();
                conn.disconnect();

            } catch (Exception e) {
                Log.e("GeocodingHelper", "Error fetching coordinates", e);
            }

            String finalResult = result;
            handler.post(() -> {
                if (finalResult == null) {
                    callback.onError("無法取得結果");
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(finalResult);
                    JSONArray results = jsonObject.getJSONArray("results");
                    if (results.length() > 0) {
                        JSONObject location = results.getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location");
                        double lat = location.getDouble("lat");
                        double lng = location.getDouble("lng");
                        callback.onSuccess(lat, lng);
                    } else {
                        callback.onError("查無地點");
                    }
                } catch (Exception e) {
                    callback.onError("解析失敗");
                }
            });
        });
    }

}
