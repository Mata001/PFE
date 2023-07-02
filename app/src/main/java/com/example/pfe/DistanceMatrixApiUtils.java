package com.example.pfe;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
public class DistanceMatrixApiUtils {
    private static final String TAG = "DistanceMatrixApiUtils";
    private static final String API_KEY = "AIzaSyCpqiUvaAud8Fa3o9L29kSJ5Yzu7V8pips";
    //            AIzaSyARlcOfXAA-JfGWFW6VH8AbtQbI96qjj6I hna ytbdl
    //            AIzaSyCnMasBoIdVpjj97TGyBUA44oC09BMxjUs hna ytbdl

    public static int closestDistance=Integer.MAX_VALUE;
    public static void getDistance(String origin, String destination, DistanceCallback callback) {
        String apiUrl = "https://maps.googleapis.com/maps/api/distancematrix/json";
        String params = "key=" + API_KEY + "&origins=" + origin + "&destinations=" + destination;
        String url = apiUrl + "?" + params;

        new DistanceMatrixTask(callback).execute(url);
    }

    private static class DistanceMatrixTask extends AsyncTask<String, Void, String> {
        private DistanceCallback callback;

        DistanceMatrixTask(DistanceCallback callback) {
            this.callback = callback;
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
                // Parse the JSON response here
                // Extract the distance value and pass it to the callback

                // Example parsing with org.json library:

                try {
                    int distanceValue;
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray rows = jsonObject.getJSONArray("rows");

                    for (int i = 0; i < rows.length(); i++) {
                        JSONObject row = rows.getJSONObject(i);
                        JSONArray elements = row.getJSONArray("elements");
                        JSONObject element = elements.getJSONObject(0);
                        JSONObject distance = element.getJSONObject("distance");
                        distanceValue = distance.getInt("value");
                        if(distanceValue<closestDistance){
                            closestDistance= distanceValue;

                    }}
                        callback.onDistanceReceived(closestDistance);

                }catch(JSONException e){
                        e.printStackTrace();
                    }

                }else {
                Log.d(TAG, "Failed to retrieve distance.");
                callback.onDistanceFailed();
            }
        }
    }

    public interface DistanceCallback {
        void onDistanceReceived(int distance);

        void onDistanceFailed();
    }
    public static void closestStopStation() {
        //ModelTram closestStopStation=null;

        getDistance("35.66125490371664,-0.6320125940081027"+"|"+"35.665560367115546,-0.6346715501204017"+"|"+"35.67129104455131,-0.6381626487760172|35.67608877397281,-0.6411018327659121", "Caféteria CHERGUI,6,Oran", new DistanceMatrixApiUtils.DistanceCallback() {
            @Override
            public void onDistanceReceived(int distance) {
                Log.d(TAG, "Distance: " + distance + " meters");

                }


            @Override
            public void onDistanceFailed() {
                Log.d(TAG, "Failed to retrieve distance.");
            }
        });

//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(castToLatLng(closestStopStation)).title("Closest "+ closestStopStation.getName()).icon(BitmapDescriptorFactory.HUE_MAGENTA);
//        mMap.addMarker(markerOptions);

    }
}

