package com.example.pfe;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static java.lang.Math.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClosestPointFinder {
    private static final double EARTH_RADIUS = 6371.0;
    private static final String TAG = "ClosestPointFinder";
    private static final String API_KEY = "AIzaSyCnMasBoIdVpjj97TGyBUA44oC09BMxjUs";
    private static final int DESTINATION_LIMIT = 25;
    public static String origine;
    public static List<String> waypoints ;
    public static int closestIndex = -1;
    public static void findClosestPoint(String origin, List<String> destinations, DistanceCallback callback) {
        origine = origin;
        int numDestinations = destinations.size();
        if (numDestinations <= DESTINATION_LIMIT) {
            // If the number of destinations is within the limit, make a single request
            makeDistanceRequest(origin, destinations, callback);
        } else {
            // If the number of destinations exceeds the limit, paginate through the results
            List<List<String>> destinationChunks = chunkDestinations(destinations, DESTINATION_LIMIT);
            List<String> closestPoints = new ArrayList<>();
            AtomicInteger remainingRequests = new AtomicInteger(destinationChunks.size());

            for (List<String> chunk : destinationChunks) {
                makeDistanceRequest(origin, chunk, new DistanceCallback() {
                    @Override
                    public void onDistanceReceived(int distance) {

                        // Handle distance received
                    }

                    @Override
                    public void onDistanceFailed() {
                        // Handle distance request failure
                        remainingRequests.decrementAndGet();
                        checkAllRequestsCompleted(callback, closestPoints, remainingRequests.get());
                    }

                    @Override
                    public void onClosestPointReceived(String closestPoint) {
                        closestPoints.add(closestPoint);
                        remainingRequests.decrementAndGet();
                        checkAllRequestsCompleted(callback, closestPoints, remainingRequests.get());
                    }
                });
            }
        }
    }

    private static void makeDistanceRequest(String origin, List<String> destinations, DistanceCallback callback) {
        String apiUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?mode=walking&";
        String destinationsString = TextUtils.join("|", destinations);
        String params = "key=" + API_KEY + "&origins=" + origin + "&destinations=" + destinationsString;
        String url = apiUrl + params;
        Log.d(TAG, "makeDistanceRequest: URL = " +url);

        new DistanceMatrixTask(callback, destinations).execute(url);
    }

    private static class DistanceMatrixTask extends AsyncTask<String, Void, String> {
        private DistanceCallback callback;
        private List<String> destinations;

        DistanceMatrixTask(DistanceCallback callback, List<String> destinations) {
            this.callback = callback;
            this.destinations = destinations;
        }

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


        private static List<List<String>> chunkDestinations(List<String> destinations, int chunkSize) {
            List<List<String>> chunks = new ArrayList<>();
            int numDestinations = destinations.size();
            int numChunks = (int) Math.ceil((double) numDestinations / chunkSize);

            for (int i = 0; i < numChunks; i++) {
                int startIndex = i * chunkSize;
                int endIndex = Math.min((i + 1) * chunkSize, numDestinations);
                List<String> chunk = destinations.subList(startIndex, endIndex);
                chunks.add(chunk);
            }

            return chunks;
        }

        private static void checkAllRequestsCompleted(DistanceCallback callback, List<String> closestPoints, int remainingRequests) {
            if (remainingRequests == 0) {
                if (!closestPoints.isEmpty()) {
                    String closestPoint = findClosestPoint(closestPoints);
                    callback.onClosestPointReceived(closestPoint);
                } else {
                    callback.onDistanceFailed();
                }
            }
        }

        private static String findClosestPoint(List<String> points) {
            double minDistance = Double.MAX_VALUE;
            String closestPoint = null;

            for (String point : points) {
                double distance = calculateEuclideanDistance(origine, point);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestPoint = point;
                }
            }

            return closestPoint;
        }

    private static double calculateEuclideanDistance(String point1, String point2) {
        // Assuming the points are in latitude-longitude format (e.g., "latitude,longitude")
        double lat1 = Double.parseDouble(point1.split(",")[0]);
        double lon1 = Double.parseDouble(point1.split(",")[1]);
        double lat2 = Double.parseDouble(point2.split(",")[0]);
        double lon2 = Double.parseDouble(point2.split(",")[1]);

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;

        return distance;
        }

        public interface DistanceCallback {
            void onDistanceReceived(int distance);

            void onDistanceFailed();

            void onClosestPointReceived(String closestPoint);
        }

}