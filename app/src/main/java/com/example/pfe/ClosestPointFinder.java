package com.example.pfe;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClosestPointFinder {
    private static final String TAG = "ClosestPointFinder";
    private static final String API_KEY = "AIzaSyCnMasBoIdVpjj97TGyBUA44oC09BMxjUs";

    public static void findClosestPoint(String origin, List<String> destinations, DistanceCallback callback) {
        String apiUrl = "https://maps.googleapis.com/maps/api/distancematrix/json";
        String destinationsString = TextUtils.join("|", destinations);
        String params = "key=" + API_KEY + "&origins=" + origin + "&destinations=" + destinationsString;
        String url = apiUrl + "?" + params;
        Log.d(TAG, "findClosestPoint: " + url);

        new DistanceMatrixTask(callback, destinations).execute(url);
    }

    private static class DistanceMatrixTask extends AsyncTask<String, Void, String> {
        private DistanceCallback callback;
        private List<String> destinations;

        DistanceMatrixTask(DistanceCallback callback, List<String> destinations) {
            this.callback = callback;
            this.destinations = destinations;
        }

        @Override
        protected String doInBackground(String... urls) {
            String result = null;

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                result = stringBuilder.toString();

                reader.close();
                inputStream.close();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.d(TAG, "Response: " + result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray rows = jsonObject.getJSONArray("rows");
                    JSONObject row = rows.getJSONObject(0);
                    JSONArray elements = row.getJSONArray("elements");

                    int minDistance = Integer.MAX_VALUE;
                    int closestIndex = -1;

                    for (int i = 0; i < elements.length(); i++) {
                        JSONObject element = elements.getJSONObject(i);
                        JSONObject distance = element.getJSONObject("distance");
                        int distanceValue = distance.getInt("value");

                        if (distanceValue < minDistance) {
                            minDistance = distanceValue;
                            closestIndex = i;
                        }
                    }

                    if (closestIndex != -1) {
                        callback.onClosestPointReceived(destinations.get(closestIndex));
                        callback.onDistanceReceived(minDistance);
                    } else {
                        callback.onDistanceFailed();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onDistanceFailed();
                }
            } else {
                Log.d(TAG, "Failed to retrieve distance.");
                callback.onDistanceFailed();
            }
        }
    }

    public interface DistanceCallback {
        void onDistanceReceived(int distance);

        void onDistanceFailed();

        void onClosestPointReceived(String closestPoint);
    }
}