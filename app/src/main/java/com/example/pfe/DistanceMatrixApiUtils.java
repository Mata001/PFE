package com.example.pfe;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
public class DistanceMatrixApiUtils {
    private static final String TAG = "DistanceMatrixApiUtils";
    private static final String API_KEY = "AIzaSyCnMasBoIdVpjj97TGyBUA44oC09BMxjUs";
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
                        callback.onDistanceReceived(distanceValue);

                    }

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
}

