package com.example.pfe;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BestOnePath implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static String TAG = "info:";
    private static GoogleMap mMap;
    int i;
    public static int number = 0;
    int wpptNb;
    int SClosestIndex = 0;
    int EClosestIndex;
    public static List<Integer> distances = new ArrayList<>();
    public static ArrayList<JSONObject> meanObject = new ArrayList<>();
    List<String> waypoints = new ArrayList<>();
    List<String> waypoints1 = new ArrayList<>();
    List<String> waypoints2 = new ArrayList<>();
    List<String> waypointsName = new ArrayList<>();
    List<String> waypoints1Name = new ArrayList<>();
    List<String> waypoints2Name = new ArrayList<>();
    List<String> wayppt = new ArrayList<>();
    List<List<String>> meanWaypoints = new ArrayList<>();
    //    static final ArrayList<Object> meanStations= new ArrayList<>();
    List<String> empty;
    static boolean mod = false;
    public static ArrayList<Object> info = new ArrayList<>();
    public static ArrayList<PolylineOptions> polylineOptionsArrayList = new ArrayList<>();
    public static ArrayList<Integer> polylineNumbers = new ArrayList<>();
    public static StationsReturned stationsReturned;


//    public StationsReturned stationsReturned;
//    public  void setOnListChanged(StationsReturned stationsReturned2){
//        this.stationsReturned = stationsReturned2;
//    }


    public BestOnePath(StationsReturned stationsReturned1) {
        this.stationsReturned = stationsReturned1;

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }

    public void readData(FirebaseCallback firebaseCallback, String destination, String origin) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int start = 0;
//                Iterator<DataSnapshot> iterator =snapshot.getChildren().iterator();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    number++;
                    ArrayList<String> destinations = new ArrayList<>();
                    ArrayList<String> destinationsName = new ArrayList<>();
                    long meanNb = snapshot.getChildrenCount();
                    String meanName = dataSnapshot.getKey();
                    long end = snapshot.child(meanName).getChildrenCount();
                    for (i = start; i < end; i++) {
                        ModelNameCoordinates modelNameCoordinates = snapshot.child(meanName).child(Integer.toString(i)).getValue(ModelNameCoordinates.class);
                        destinations.add(modelNameCoordinates.getCoordinates());
                        destinationsName.add(modelNameCoordinates.getName());

                    }
                    Log.d(TAG, "Mean :" + meanName + ", Number :" + end + ", Stations :" + destinations);
                    ClosestPointFinder.findClosestPoint(origin, destinations, new ClosestPointFinder.DistanceCallback() {
                        @Override
                        public void onDistanceReceived(int distance) {
                            Log.d(TAG, "distances dest " + distance);
                        }

                        @Override
                        public void onDistanceFailed() {

                        }

                        @Override
                        public void onClosestPointReceived(String closestPoint) {

                            SClosestIndex = destinations.indexOf(closestPoint);
                            String closestS = destinationsName.get(SClosestIndex);
                            Log.d(TAG, "index ta Origin " + SClosestIndex + "  " + closestS);
                            info.clear();
                            info.add(origin);
                            info.add(destination);
                            info.add(meanName);
                            info.add(closestS);
                            info.add(closestPoint);
                        }
                    });
                    //Closest transport mean stations for origine
                    ClosestPointFinder.findClosestPoint(destination, destinations, new ClosestPointFinder.DistanceCallback() {
                        @Override
                        public void onDistanceReceived(int distance) {
                            Log.d(TAG, "distances origin " + distance);

                        }

                        @Override
                        public void onDistanceFailed() {
                            Log.d(TAG, "onDistanceFailed: Sorry");

                        }

                        @Override
                        public void onClosestPointReceived(String closestPoint) {
                            EClosestIndex = destinations.indexOf(closestPoint);
                            String closestE = destinationsName.get(EClosestIndex);
                            Log.d(TAG, "Index ta Destination " + EClosestIndex + "  " + closestE);
                            if (EClosestIndex == SClosestIndex) {
                                Log.d(TAG, "dir directions direct a lhaj");
                                getInfo(info);
                                firebaseCallback.onCallback(info, meanNb);
                            } else if (EClosestIndex != SClosestIndex) {
//                                wpptNb = -EClosestIndex + SClosestIndex - 1;
                                info.add(closestE);
                                info.add(closestPoint);
                                if (EClosestIndex > SClosestIndex) {
                                    wpptNb = EClosestIndex - SClosestIndex - 1;
                                    if (wpptNb < 26) {
                                        waypointsName = destinationsName.subList(SClosestIndex + 1, EClosestIndex);
                                        waypoints = destinations.subList(SClosestIndex + 1, EClosestIndex);
                                        Log.d(TAG, "wahd " + waypoints + "    " + waypoints1Name);
                                        info.add(waypoints);
                                        info.add(waypointsName);
                                    } else if (wpptNb > 25) {
                                        waypoints1 = destinations.subList(SClosestIndex + 1, SClosestIndex + 26);
                                        waypoints2 = destinations.subList(SClosestIndex + 27, EClosestIndex);
                                        waypoints1Name = destinationsName.subList(SClosestIndex + 1, SClosestIndex + 26);
                                        waypoints2Name = destinationsName.subList(SClosestIndex + 27, EClosestIndex);
                                        Log.d(TAG, "lowl " + waypoints1 + "  " + waypoints1Name);
                                        Log.d(TAG, "tani " + waypoints2 + "   " + waypoints2Name);
                                        info.add(waypoints1);
                                        info.add(destinations.get(SClosestIndex + 26));
                                        info.add(waypoints2);
                                        info.add(waypoints1Name);
                                        info.add(waypoints2Name);
                                    }
                                    getInfo(info);
                                    firebaseCallback.onCallback(info, meanNb);

                                } else if (SClosestIndex > EClosestIndex) {
                                    wpptNb = -EClosestIndex + SClosestIndex - 1;
                                    if (wpptNb < 26) {
                                        waypoints = destinations.subList(EClosestIndex + 1, SClosestIndex);
                                        waypointsName = destinationsName.subList(EClosestIndex + 1, SClosestIndex);
                                        Log.d(TAG, "wahd " + waypoints + "    " + waypoints1Name);
                                        info.add(waypoints);
                                        info.add(waypointsName);
                                    } else if (wpptNb > 25) {
                                        waypoints1 = destinations.subList(EClosestIndex + 1, EClosestIndex + 26);
                                        waypoints2 = destinations.subList(EClosestIndex + 27, SClosestIndex);
                                        waypoints1Name = destinationsName.subList(EClosestIndex + 1, EClosestIndex + 26);
                                        waypoints2Name = destinationsName.subList(EClosestIndex + 27, SClosestIndex);
                                        Log.d(TAG, "lowl " + waypoints1 + "   " + waypoints1Name);
                                        Log.d(TAG, "tani " + waypoints2 + "   " + waypoints2Name);
                                        info.add(waypoints1);
                                        info.add(destinations.get(EClosestIndex + 26));
                                        info.add(waypoints2);
                                        info.add(waypoints1Name);
                                        info.add(waypoints2Name);
                                    }
                                    getInfo(info);
                                    firebaseCallback.onCallback(info, meanNb);
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public interface FirebaseCallback {
        void onCallback(ArrayList<Object> list, long number);
    }

    public void getInfo(ArrayList<Object> lista) {
        List<String> urls = new ArrayList<>();
        List<String> khawi = new ArrayList<>();
        Log.d(TAG, "karitha siyed mn " + lista);

        if (lista.get(2).toString().equals("B") || lista.get(2).toString().equals("H")) {
            Log.d(TAG, "wchmn mean transport " + lista.get(2));
            if (lista.size() == 5) {
                Log.d(TAG, " mat7tajch transport ");
                String urldirect = MainActivity.getRequestUrl(castObjectToLatLng(lista.get(0)), castObjectToLatLng(lista.get(1)), khawi, "walking");
                urls.add(urldirect);
            } else if (lista.size() != 5) {
                String originUrl = MainActivity.getRequestUrl(castObjectToLatLng(lista.get(0)), castObjectToLatLng(lista.get(4)), khawi, "walking");
                String destinationUrl = MainActivity.getRequestUrl(castObjectToLatLng(lista.get(1)), castObjectToLatLng(lista.get(6)), khawi, "walking");
                urls.add(originUrl);
                urls.add(destinationUrl);
                if (lista.size() == 9) {
                    String url = MainActivity.getRequestUrl(castObjectToLatLng(lista.get(4)), castObjectToLatLng(lista.get(6)), castObjectToList(lista.get(7)), "driving");
                    Log.d(TAG, "urls ta direction ki maykounch chunk \n" + originUrl + "\n" + destinationUrl + "\n" + url);
                    urls.add(url);

                } else if (lista.size() == 12) {
                    String url1 = MainActivity.getRequestUrl(castObjectToLatLng(lista.get(4)), castObjectToLatLng(lista.get(8)), castObjectToList(lista.get(7)), "driving");
                    String url2 = MainActivity.getRequestUrl(castObjectToLatLng(lista.get(8)), castObjectToLatLng(lista.get(6)), castObjectToList(lista.get(9)), "driving");
                    Log.d(TAG, "urls ta direction ki ykoun chunk \n" + originUrl + "\n" + destinationUrl + "\n" + url1 + "\n" + url2);
                    urls.add(url1);
                    urls.add(url2);

                }
            } else {
                Log.d(TAG, "ak ghalt sa7bi me ttali ");
            }
        } else if (lista.get(2).toString().equals("Tramway")) {
            Log.d(TAG, "moyen de transport tram " + lista.get(2));
            if (lista.size() == 5) {
                Log.d(TAG, " mat7tajch transport ");
                String urldirect = MainActivity.getRequestUrl(castObjectToLatLng(lista.get(0)), castObjectToLatLng(lista.get(1)), khawi, "walking");
                urls.add(urldirect);
            } else if (lista.size() != 5) {
                String originUrl = MainActivity.getRequestUrl(castObjectToLatLng(lista.get(0)), castObjectToLatLng(lista.get(4)), khawi, "walking");
                String destinationUrl = MainActivity.getRequestUrl(castObjectToLatLng(lista.get(1)), castObjectToLatLng(lista.get(6)), khawi, "walking");
                urls.add(originUrl);
                urls.add(destinationUrl);
                if (lista.size() == 9) {
                    String url = MainActivity.getRequestUrl(castObjectToLatLng(lista.get(4)), castObjectToLatLng(lista.get(6)), castObjectToList(lista.get(7)), "walking");
                    Log.d(TAG, "urls ta direction ki maykounch chunk \n" + originUrl + "\n" + destinationUrl + "\n" + url);
                    urls.add(url);

                } else if (lista.size() == 12) {
                    String url1 = MainActivity.getRequestUrl(castObjectToLatLng(lista.get(4)), castObjectToLatLng(lista.get(8)), castObjectToList(lista.get(7)), "walking");
                    String url2 = MainActivity.getRequestUrl(castObjectToLatLng(lista.get(8)), castObjectToLatLng(lista.get(6)), castObjectToList(lista.get(9)), "walking");
                    Log.d(TAG, "urls ta direction ki ykoun chunk \n" + originUrl + "\n" + destinationUrl + "\n" + url1 + "\n" + url2);
                    urls.add(url1);
                    urls.add(url2);

                }
            } else {
                Log.d(TAG, "ak ghalt sa7bi me ttali ");
            }

        }
        String urlsNb = String.valueOf(urls.size());
        polylineNumbers.add(urls.size());

        if (urls.size() == 3) {
            for (String pieceUrl : urls.subList(0, 2)) {
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
//            for (int l=0 ;l< 2;l++){
                taskRequestDirections.execute(pieceUrl, urlsNb, "walking");
//            }
//            for (int l=2 ;l<urls.size();l++){
//                taskRequestDirections.execute(pieceUrl, urlsNb, "driving");
//            }
////            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
////            taskRequestDirections.execute(pieceUrl, urlsNb);
////            Log.d(TAG, "getInfo: "+ mainActivity.meanObject);
            }
            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
            taskRequestDirections.execute(urls.get(2), urlsNb, "driving");
        } else if (urls.size() == 4) {
            for (String pieceUrl : urls.subList(0, 2)) {
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(pieceUrl, urlsNb, "walking");
            }
            for (String pieceUrl : urls.subList(2, 4)) {
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(pieceUrl, urlsNb, "driving");
            }

        } else if (urls.size() == 1) {
            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
            taskRequestDirections.execute(urls.get(0), urlsNb, "walking");

        } else {
            Log.d(TAG, "getInfo: error bro " + urls + "    " + urls.size());
        }
        Log.d(TAG, "faslaaa " + urls);
    }

    public static LatLng castObjectToLatLng(Object object) {
        String string = object.toString();
        String[] latlong = string.split(",");
        double longitude = Double.parseDouble(latlong[1]);
        double latitude = Double.parseDouble(latlong[0]);
        LatLng latLng = new LatLng(latitude, longitude);
        return latLng;
    }

    public static List<String> castObjectToList(Object object) {
        List<String> waypointsList = new ArrayList<>((List<String>) object);
//        waypointsList = (List<String>) object;
        return waypointsList;
    }

    public static int acumilatorDurationDistance(ArrayList<JSONObject> object) {
        DirectionsParser directionsParser = new DirectionsParser();
        int total = 0;
        Log.d(TAG, "meeeee " + MainActivity.lakhra);
        Log.d(TAG, "meeeee infoooo " + info);

        try {
            if (object.size() == 4) {
                total = directionsParser.getdisdur(object.get(0)).get(1) + directionsParser.getdisdur(object.get(1)).get(1) + (directionsParser.getdisdur(object.get(2)).get(1) + directionsParser.getdisdur(object.get(3)).get(1)) / 3;
                Log.d(TAG, "total ta 4 " + total);
            } else if (object.size() == 3) {
                total = directionsParser.getdisdur(object.get(0)).get(1) + directionsParser.getdisdur(object.get(1)).get(1) + directionsParser.getdisdur(object.get(2)).get(1) / 3;
                Log.d(TAG, "total ta 3 " + total);
            } else if (object.size() == 1) {
                total = directionsParser.getdisdur(object.get(0)).get(1);
                Log.d(TAG, "total ta direct");
            } else if (object.size() == 7) {
                directionsParser.getdisdur(object.get(4)).get(1);
                directionsParser.getdisdur(object.get(5)).get(1);
                directionsParser.getdisdur(object.get(6)).get(1);
                Log.d(TAG, "acumilatorDurationDistance: dima dima chwi");
            }
        } catch (Exception e) {
//            Toast.makeText(this, "click on the location button first", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "acumilatorDurationDistance: " + e);
        }
//        Log.d(TAG, "distance total "+total);
        return total;
    }


    //--------------------------------Request Direction URL--------------------------------
    public static String requestDirection(String reqUrl) throws Exception {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
//        System.out.println(responseString);
        return responseString;
    }

    //--------------------------------55--------------------------------
    public static class TaskRequestDirections extends AsyncTask<String, Void, String> {
        String urlsNb;
        String mode;


        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                urlsNb = strings[1];
                mode = strings[2];
                responseString = requestDirection(strings[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseString;

        }

        @Override
        public void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse json here
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s, urlsNb, mode);
        }
    }

    //-------------------------------55---------------------------------
    public static class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {
        String urlsNb;
        String mode;
        public ArrayList<StationItem> stationItems = new ArrayList<>();
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            urlsNb = strings[1];
            mode = strings[2];
            try {
                jsonObject = new JSONObject(strings[0]);
                meanObject.add(jsonObject);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
                int Nb = Integer.valueOf(urlsNb);

//            polylineNumbers.add(Nb);
//            int totalNbPoly = Arrays.stream(polylineNumbers).sum();


                if (meanObject.size() == Nb) {
//                Log.d(TAG, "info list " + MainActivity.lakhra);
                    int distanceReturned = acumilatorDurationDistance(meanObject);
                    Log.d(TAG, "distanceReturned " + distanceReturned);
                    distances.add(distanceReturned);
                    meanObject.clear();
                    Log.d(TAG, "distances " + distances);
                    if (distanceReturned < MainActivity.shortestDistance) {
                        MainActivity.shortestDistance = distanceReturned;
                        MainActivity.shortestDistanceIndex = distances.indexOf(MainActivity.shortestDistance);
                        Log.d(TAG, "shortest distance is " + MainActivity.shortestDistance);
                        Log.d(TAG, "index ta shortest distance is " + distances.indexOf(MainActivity.shortestDistance));
                        Log.d(TAG, "index ta shortest distance is " + MainActivity.shortestDistanceIndex);

                    } else {
                        Log.d(TAG, "shortest aw 9bl wla aw jay ");
                    }
                    Log.d(TAG, "index ta shortestII distance is " + MainActivity.shortestDistanceIndex);
//                Log.d(TAG, "lisssssst polyyyys "+polylineOptionsArrayList);
                } else {
                    Log.d(TAG, "rah kayn decalage fel mean Object addition "+meanObject.size() +"   "+Nb);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
//            Log.d(TAG, "meanObject   "+ urlsNb);
//            BestOnePath.acumilatorDurationDistance(meanObject);
            return routes;
        }

        @Override
        public void onPostExecute(List<List<HashMap<String, String>>> lists) {
            int totalNbPoly = 0;
            int startIndex = 0;
            for (int n = 0; n < polylineNumbers.size(); n++) {
                totalNbPoly += polylineNumbers.get(n);
            }
            Log.d(TAG, "total Nb " + totalNbPoly);
            ArrayList points = null;
            PolylineOptions polylineOptions = null;
            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();
                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));
                    points.add(new LatLng(lat, lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15f);
                polylineOptions.color(Color.argb(200, 252, 3, 36));
                polylineOptions.startCap(new RoundCap());
                polylineOptions.endCap(new RoundCap());
                polylineOptions.jointType(1);
                polylineOptions.geodesic(true);
                List<PatternItem> pattern;
                if (mode == "driving") {
                    pattern = Arrays.asList(new Dash(30));

                } else if (mode == "walking") {
                    pattern = Arrays.asList(new Dot(), new Gap(30));
                    polylineOptions.color(Color.argb(200, 110, 0, 15));
                    polylineOptions.width(15f);
                    polylineOptions.pattern(pattern);
                }
            }
            if (polylineOptions != null) {
//                    MainActivity.mMap.addPolyline(polylineOptions);
                polylineOptionsArrayList.add(polylineOptions);

            } else {
                polylineOptionsArrayList.add(null);
//                Toast.makeText(getApplicationContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onPostExecute: ya babaaaa direction not found ");
            }
            if (polylineOptionsArrayList.size() == totalNbPoly && MainActivity.shortestDistanceIndex != -1) {
                for (int i = 0; i < MainActivity.shortestDistanceIndex; i++) {
                    Log.d(TAG, "indexx " + MainActivity.shortestDistanceIndex);
                    Log.d(TAG, "polynumbers " + polylineNumbers);
                    startIndex += polylineNumbers.get(i);
                    Log.d(TAG, "startindex " + startIndex);
                }

//                    BestOnePath bestOnePath = new BestOnePath();

                Log.d(TAG, "adihi list li n3amro biha infos " + MainActivity.lakhra.get(MainActivity.shortestDistanceIndex));

                CircleOptions circleOptions1 = new CircleOptions();
                CircleOptions circleOptions2 = new CircleOptions();
                circleOptions1.radius(15f);
                circleOptions1.strokeWidth(10f);
                circleOptions1.fillColor(Color.BLACK);
                circleOptions1.strokeColor(Color.rgb(117, 196, 249));
                circleOptions2.radius(15f);
                circleOptions2.strokeWidth(10f);

                circleOptions2.fillColor(Color.rgb(117, 196, 249));
                circleOptions1.center(MainActivity.castStringToLatLng(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(4).toString()));
                circleOptions2.center(MainActivity.castStringToLatLng(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(6).toString()));


                MainActivity.mMap.addCircle(circleOptions1);
                MainActivity.mMap.addCircle(circleOptions2);
                List<String> stationsForBottomSheet = castObjectToList(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(8));
                MainActivity.meanName.setText(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(2).toString());
                if (MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(2).toString().equals("Tramway")) {
                    MainActivity.meanIcon.setImageResource(R.drawable.tram_front_view);
                } else {
                    MainActivity.meanIcon.setImageResource(R.drawable.front_bus);

                }


                if (stationsForBottomSheet.size() == 0) {
                    stationItems.add(new StationItem(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(3).toString(), R.drawable.endpoint));
                    stationItems.add(new StationItem("", R.drawable.no_waypoint));
                    stationItems.add(new StationItem(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(5).toString(), R.drawable.endpoint));

                } else if (stationsForBottomSheet.size() == 1) {
                    stationItems.add(new StationItem(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(3).toString(), R.drawable.endpoint));
                    stationItems.add(new StationItem(stationsForBottomSheet.get(0), R.drawable.one_station));
                    stationItems.add(new StationItem(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(5).toString(), R.drawable.endpoint));
                } else if (stationsForBottomSheet.size() == 2) {
                    stationItems.add(new StationItem(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(3).toString(), R.drawable.endpoint));
                    stationItems.add(new StationItem(stationsForBottomSheet.get(0), R.drawable.closest_origin));
                    stationItems.add(new StationItem(stationsForBottomSheet.get(1), R.drawable.closest_destination));
                    stationItems.add(new StationItem(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(5).toString(), R.drawable.endpoint));
                } else if (stationsForBottomSheet.size() > 2) {
                    stationItems.add(new StationItem(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(3).toString(), R.drawable.endpoint));
                    stationItems.add(new StationItem(stationsForBottomSheet.get(0), R.drawable.closest_origin));
                    for (
                            int s = 1; s < stationsForBottomSheet.size() - 1; s++) {
                        stationItems.add(new StationItem(stationsForBottomSheet.get(s), R.drawable.middle_station));
                    }
                    stationItems.add(new StationItem(stationsForBottomSheet.get(stationsForBottomSheet.size() - 1), R.drawable.closest_destination));
                    stationItems.add(new StationItem(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(5).toString(), R.drawable.endpoint));

                }
                Log.d(TAG, "reccc " + stationItems);
                stationsReturned.onCallbackStations(stationItems);


                Log.d(TAG, "indexxxx " + MainActivity.shortestDistanceIndex);
                int endIndex = startIndex + polylineNumbers.get(MainActivity.shortestDistanceIndex);
                Log.d(TAG, "endIndex " + endIndex);
//                        MainActivity.mMap.addPolyline(polylineOptionsArrayList.get(6));
//                        MainActivity.mMap.addPolyline(polylineOptionsArrayList.get(7));
//                        MainActivity.mMap.addPolyline(polylineOptionsArrayList.get(8));
//                        ArrayList<PolylineOptions> finalPolys = polylineOptionsArrayList.subList(startIndex,endIndex);
                for (
                        int m = startIndex;
                        m < endIndex; m++) {
                    if (polylineOptionsArrayList.get(m) != null) {
                        MainActivity.mMap.addPolyline(polylineOptionsArrayList.get(m));
                        Log.d(TAG, "mmm " + m);
                    } else {
                        Log.d(TAG, "null adik matrssmtch mais bon");
                    }

                }
            } else if (MainActivity.shortestDistanceIndex == -1) {
                MainActivity.shortestDistance = Integer.MAX_VALUE;
                Log.d(TAG, "Internet is Poor");

            }


            //Get list route and display it into the map


//            Log.d(TAG, "list poly " + polylineOptionsArrayList);

        }

//        public void parseTasks(StationsReturned stationsReturned) {
//            BestOnePath bestOnePath = new BestOnePath();
//            ArrayList<StationItem> stationItems = new ArrayList<>();
//            Log.d(TAG, "adihi list li n3amro biha infos " + MainActivity.lakhra.get(MainActivity.shortestDistanceIndex));
//            List<String> stationsForBottomSheet = bestOnePath.castObjectToList(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(7));
//
//            if (stationsForBottomSheet.size() == 0) {
//                Log.d(TAG, "No transport mean is suitable");
//
//            } else if (stationsForBottomSheet.size() == 1) {
//                stationItems.add(new StationItem(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(4).toString(), R.drawable.endpoint));
//                stationItems.add(new StationItem(stationsForBottomSheet.get(0), R.drawable.one_station));
//                stationItems.add(new StationItem(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(6).toString(), R.drawable.endpoint));
//            } else if (stationsForBottomSheet.size() == 2) {
//                stationItems.add(new StationItem(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(4).toString(), R.drawable.endpoint));
//                stationItems.add(new StationItem(stationsForBottomSheet.get(0), R.drawable.closest_origin));
//                stationItems.add(new StationItem(stationsForBottomSheet.get(1), R.drawable.closest_destination));
//                stationItems.add(new StationItem(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(6).toString(), R.drawable.endpoint));
//            } else if (stationsForBottomSheet.size() < 2) {
//                stationItems.add(new StationItem(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(4).toString(), R.drawable.endpoint));
//                stationItems.add(new StationItem(stationsForBottomSheet.get(0), R.drawable.closest_origin));
//                for (
//                        int s = 1; s < stationsForBottomSheet.size() - 1; s++) {
//                    stationItems.add(new StationItem(stationsForBottomSheet.get(s), R.drawable.middle_station));
//                }
//                stationItems.add(new StationItem(stationsForBottomSheet.get(stationsForBottomSheet.size() - 1), R.drawable.closest_destination));
//                stationItems.add(new StationItem(MainActivity.lakhra.get(MainActivity.shortestDistanceIndex).get(6).toString(), R.drawable.endpoint));
//
//            }
//            Log.d(TAG, "reccc " + stationItems);
//            stationsReturned.onCallbackStations(stationItems);
//        }

    }

    public interface StationsReturned {
        void onCallbackStations(ArrayList<StationItem> list);
    }


}
