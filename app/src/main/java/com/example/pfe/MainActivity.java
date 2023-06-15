package com.example.pfe;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static String TAG = "info:";
    private static GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    public static final int TIME_INTERVAL = 2000;
    private long mPressed;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ArrayList<LatLng> listPoints;
    int i;
    int wpptNb;
    ArrayList<String> destinations;
    static int SClosestIndex;
    static int EClosestIndex;
    List<String> waypoints;
    List<String> waypoints1;
    List<String> waypoints2;
    List<String> wayppt;
    ArrayList<LatLng> locToClose;
    ArrayList<LatLng> destToClose;
    List<String> empty;
    ArrayList<LatLng> locdest;
    ArrayList<LatLng> locdest1;
    ArrayList<LatLng> locdest2;
    boolean mod = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listPoints = new ArrayList<>();
        locdest = new ArrayList<>();
        locdest1 = new ArrayList<>();
        locdest2 = new ArrayList<>();
        empty = new ArrayList<>();
        destinations = new ArrayList<>();
        locToClose = new ArrayList<>();
        destToClose = new ArrayList<>();
        waypoints = new ArrayList<>();
        waypoints1 = new ArrayList<>();
        waypoints2 = new ArrayList<>();
        wayppt = new ArrayList<>();

        BestOnePath bestOnePath = new BestOnePath();
        bestOnePath.readData(new BestOnePath.FirebaseCallback() {
            @Override
            public void onCallback(ArrayList<Object> list) {
                bestOnePath.getInfo(list);
            }
        });
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        FloatingActionButton currentLocationBtn = findViewById(R.id.currLoc);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);


//--------------------Enable Location services from Settings---------------
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

//-----------------------------CurrentLocation -------------------------------------
        currentLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableCurrentLocation();
                mMap.clear();
            }
        });
//--------------------------------Display map---------------------------------------
        mapFragment.getMapAsync(this);

//----------------------------55------------------------------
        // Initialize Places.
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyARlcOfXAA-JfGWFW6VH8AbtQbI96qjj6I");
        }
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.

//----------------------------Autocomplete Restriction to Oran------------------------------
        autocompleteFragment.setLocationRestriction(RectangularBounds.newInstance(
                new LatLng(35.604562, -0.748931),
                new LatLng(35.788521, -0.501718)));
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        // Set up a PlaceSelectionListener to handle the response.

//----------------------------On Place searched listener------------------------------
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Toast.makeText(MainActivity.this, place.getLatLng() + place.getName() + " is your destination", Toast.LENGTH_SHORT).show();
//              marker
                if (destToClose.size() > 0) {
                    destToClose.clear();
                }
                destToClose.add(place.getLatLng());
//                LatLng center = new LatLng((lat+lat)/2)
//                moveCameraToLocation(center, 15);

                //------------------------------find closest point--------------------------------

                /*ClosestPointFinder.findClosestPoint(latlngToString(place.getLatLng()), destinations, new ClosestPointFinder.DistanceCallback() {
                    String TAG = " ";

                    @Override
                    public void onDistanceReceived(int distance) {
                        Log.d(TAG, "Distance to Destination: " + distance);
                        // Handle distance received
                    }

                    @Override
                    public void onDistanceFailed() {
                        // Handle distance request failure
                    }

                    @Override
                    public void onClosestPointReceived(String closestPoint) {
                        EClosestIndex = destinations.indexOf(closestPoint);
                        Log.d(TAG, "index ta destination" + EClosestIndex);
                        Log.d(TAG, "Closest point to Destination: " + closestPoint);
                        requestPolyline(castStringToLatLng(closestPoint), destToClose, empty, "walking");
                        wpptNb = EClosestIndex - SClosestIndex - 1;


                        if (wpptNb < 26) {
                            for (int j = SClosestIndex + 1; j < EClosestIndex; j++) {
                                waypoints.add(destinations.get(j));
                            }
                            Log.d(TAG, "wahd " + waypoints);
                            if (locdest.size() > 0) {
                                locdest.clear();
                            }
                            locdest.add(castStringToLatLng(destinations.get(SClosestIndex)));
                            new Handler().postDelayed(new Runnable(){
                                @Override
                                public void run() {
                                    requestPolyline(castStringToLatLng(destinations.get(EClosestIndex)) , locdest , waypoints, "driving");
                                }
                            }, 500);
//                            requestPolyline(castStringToLatLng(destinations.get(EClosestIndex)), locdest, waypoints);
                        } else {
                            for (int m = SClosestIndex + 1; m < SClosestIndex + 26; m++) {
                                waypoints1.add(destinations.get(m));
                            }
                            for (int n = SClosestIndex + 27; n < EClosestIndex - 1; n++) {
                                waypoints2.add(destinations.get(n));
                            }

                            Log.d(TAG, "lowl " + waypoints1);
                            Log.d(TAG, "tani " + waypoints2);
                            if (locdest1.size() > 0) {
                                locdest1.clear();
                            }
                            locdest1.add(castStringToLatLng(destinations.get(SClosestIndex)));

                            if (locdest2.size() > 0) {
                                locdest2.clear();
                            }
                            locdest2.add(castStringToLatLng(destinations.get(SClosestIndex + 26)));

//                            wayppt = waypoints1;
//                            wayppt = waypoints2;
                            new Handler().postDelayed(new Runnable(){
                                @Override
                                public void run() {

//                                    wayppt = waypoints1;
                                    requestPolyline(castStringToLatLng(destinations.get(SClosestIndex + 26)), locdest1, waypoints1, "driving");
//                                    wayppt = waypoints2;
                                    requestPolyline(castStringToLatLng(destinations.get(EClosestIndex)), locdest2, waypoints2, "driving");
                                }
                            }, 500);
//                            requestPolyline(castStringToLatLng(destinations.get(SClosestIndex + 24)), locdest1, waypoints1);
                        }
                    }
                });*/
            }

            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
//        ----------
        databaseReference = FirebaseDatabase.getInstance().getReference("features");
//        ----------------------------- Waypoints creation
    }

    //----------------------------On Place searched listener------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.class, R.raw.mapstyle));
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        //--------------------------------Database Retrieve Data--------------------------------
        /*databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int start = 0;
                long end = snapshot.getChildrenCount();
                Log.d(TAG, "length "+ end);
                for (i = start; i < end; i++) {
                    //DataSnapshot dataSnapshot = snapshot.getChildren();
                    //for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //ModelTram modelTram = dataSnapshot.getValue(ModelTram.class);
                    ModelTram modelTram = snapshot.child(Integer.toString(i)).getValue(ModelTram.class);
                    destinations.add(modelTram.getCoordinates());
                    LatLng latLng = castToLatLng(modelTram);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng).title(modelTram.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.piner));
                    mMap.addMarker(markerOptions).showInfoWindow();
                }
                Log.d(TAG, "destination arraylist " + destinations);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });*/
        //--------------------------------Location Permission--------------------------------
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Enable current location button and show current location on the map
            enableCurrentLocation();
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        //--------------------------------On Map Long Click Listener--------------------------------
        /*googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                //Reset marker when already 2
                if (listPoints.size() == 2) {
                    listPoints.clear();
                    mMap.clear();
                }
                //Save first point select
                listPoints.add(latLng);
                //Create marker
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                if (listPoints.size() == 1) {
                    //Add first marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else {
                    //Add second marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                mMap.addMarker(markerOptions);

                if (listPoints.size() == 2) {
                    //Create the URL to get request from first marker to second marker
                    String url = getRequestUrl(listPoints.get(0), listPoints.get(1), wayppt);
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    taskRequestDirections.execute(url);
                }
            }
        });*/
    }

    //--------------------------------Create Direction URL--------------------------------
    public static String getRequestUrl(LatLng origin, LatLng dest, List<String> wayppt) {
        String waypointsString = TextUtils.join("|via:", wayppt);
        //Value of origin
        String str_org = "origin=" + origin.latitude + "," + origin.longitude;
        //Value of destination
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        //Set value enable alternative ways
        String alt = "alternatives=TRUE";
        //Set waypoints those are the bus stations
        String way = "waypoints=" + waypointsString;
        //Mode for find direction
        String mod = "mode=walking";
        //String key for api key
        String key = "key=AIzaSyARlcOfXAA-JfGWFW6VH8AbtQbI96qjj6I";
        //Build the full param
        String param = alt + "&" + str_org + "&" + mod + "&" + str_dest + "&" + way + "&" + key;
        //Create url to request
        String url = "https://maps.googleapis.com/maps/api/directions/json?" + param;
        Log.d(TAG, "waypoints requested " + url);
        wayppt.clear();
        return url;
    }

    //--------------------------------Request Direction URL--------------------------------
    private String requestDirection(String reqUrl) throws Exception {
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
        System.out.println(responseString);
        return responseString;
    }

    //--------------------------------55--------------------------------
    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseString;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse json here
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    //-------------------------------55---------------------------------
    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        public void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map
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
                polylineOptions.color(Color.argb(150, 252, 3, 36));
                polylineOptions.startCap(new RoundCap());
                polylineOptions.endCap(new RoundCap());
                polylineOptions.jointType(1);
                polylineOptions.geodesic(true);
                List<PatternItem> pattern;
                if (mod) {
                    pattern = Arrays.asList(new Dash(30));
                } else {
                    pattern = Arrays.asList(new Dot(), new Gap(30));
                    polylineOptions.color(Color.argb(150, 252, 3, 161));
                    polylineOptions.width(15f);
                    polylineOptions.pattern(pattern);
                }
            }
            if (polylineOptions != null) {
                mMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getApplicationContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    //--------------------------------Enable Current Location--------------------------------
    private void enableCurrentLocation() {
        // Check if the device has location services enabled
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Enable the location layer on the map
        mMap.setMyLocationEnabled(true);

        // Get the last known location of the device
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Move the camera to the user's current location
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    moveCameraToLocation(currentLatLng, 15);
                    if (locToClose.size() > 0) {
                        locToClose.clear();
                    }
                    locToClose.add(currentLatLng);
                    Log.d(TAG, "location is : " + currentLatLng);

//------------------------------find closest point--------------------------------

                    /*ClosestPointFinder.findClosestPoint(latlngToString(currentLatLng), destinations, new ClosestPointFinder.DistanceCallback() {
                        @Override
                        public void onDistanceReceived(int distance) {
                            Log.d(TAG, "Shortest distance to current location " + distance);
                            // Handle distance received
                        }

                        @Override
                        public void onDistanceFailed() {
                            // Handle distance request failure
                        }

                        @Override
                        public void onClosestPointReceived(String closestPoint) {
                            SClosestIndex = destinations.indexOf(closestPoint);
                            Log.d(TAG, " index Start " + SClosestIndex);
                            Log.d(TAG, "Closest point to current location: " + closestPoint);
                            requestPolyline(castStringToLatLng(closestPoint), locToClose, empty, "walking");
                        }
                    });*/
                } else {
                    Toast.makeText(MainActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //--------------------------------Location Permission--------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable current location
                enableCurrentLocation();
            } else {
                // Permission denied, show a message or handle the situation accordingly
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //--------------------------------Request a Polyline--------------------------------
    public void requestPolyline(LatLng
                                        latLng, ArrayList<LatLng> lol, List<String> wayppt, String mode) {
        mod = modeProvider(mode);
        Log.d(TAG, "requestPolyline: mode is " + mod + "       " + mode);
        //Reset marker when already 2
        if (lol.size() == 2) {
            lol.clear();
            mMap.clear();
        }
        //Save first point select
        lol.add(latLng);
        //Create marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);

        if (lol.size() == 1) {
            //Add first marker to the map
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else {
            //Add second marker to the map
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
        mMap.addMarker(markerOptions);

        if (lol.size() == 2) {
            //Create the URL to get request from first marker to second marker
            String url = getRequestUrl(lol.get(0), lol.get(1), wayppt);
            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
            taskRequestDirections.execute(url);
        }
    }

    //--------------------------------Walking/Driving polyline--------------------------------
    public boolean modeProvider(String mod) {
        if (mod == "walking")
            return false;
        else return true;
    }
//
//            public boolean modeProvider (String modee){
//                return modee != "walking";
//            }
//
//            public void closestfinder (LatLng latLng, ArrayList < String > dest, String what){
//                if (what == "tramway") {
//                    ClosestPointFinder.findClosestPoint(latlngToString(latLng), dest, new ClosestPointFinder.DistanceCallback() {
//                        @Override
//                        public void onDistanceReceived(int distance) {
//                            Log.d(TAG, "Shortest distance to current location " + distance);
//                            // Handle distance received
//                        }
//
//                        @Override
//                        public void onDistanceFailed() {
//                            // Handle distance request failure
//                        }
//
//                        @Override
//                        public void onClosestPointReceived(String closestPoint) {
//                            SClosestIndex = dest.indexOf(closestPoint);
//                            Log.d(TAG, " index Start " + SClosestIndex);
//                            Log.d(TAG, "Closest point to current location: " + closestPoint);
//                            requestPolyline(castStringToLatLng(closestPoint), locToClose, empty, "walking", what);
//                            Log.d(TAG, "onClosestPointReceived: it's this one bro" + closestPoint);
//
//                        }
//                    });
//                } else {
//                    ClosestPointFinder.findClosestPoint(latlngToString(latLng), dest, new ClosestPointFinder.DistanceCallback() {
//                        @Override
//                        public void onDistanceReceived(int distance) {
//                            Log.d(TAG, "Shortest distance to current location " + distance);
//                            // Handle distance received
//                        }
//
//                        @Override
//                        public void onDistanceFailed() {
//                            // Handle distance request failure
//                        }
//
//                        @Override
//                        public void onClosestPointReceived(String closestPoint) {
//                            SClosestIndexH = dest.indexOf(closestPoint);
//                            Log.d(TAG, " index Start " + SClosestIndexH);
//                            Log.d(TAG, "Closest point to current location: " + closestPoint);
//                            requestPolyline(castStringToLatLng(closestPoint), locToCloseH, empty, "walking", what);
//                            Log.d(TAG, "onClosestPointReceived: it's this one bro" + closestPoint);
//
//                        }
//                    });
//                }
//            }
//            public void closestplacefinder (LatLng latLng, ArrayList < String > dest, String what){
//                if (what == "tramway") {
//                    ClosestPointFinder.findClosestPoint(latlngToString(latLng), dest, new ClosestPointFinder.DistanceCallback() {
//                        String TAG = " ";
//
//                        @Override
//                        public void onDistanceReceived(int distance) {
//                            Log.d(TAG, "Distance to Destination: " + distance);
//                            // Handle distance received
//                        }
//
//                        @Override
//                        public void onDistanceFailed() {
//                            // Handle distance request failure
//                        }
//
//                        @Override
//                        public void onClosestPointReceived(String closestPoint) {
//                            EClosestIndex = dest.indexOf(closestPoint);
//                            Log.d(TAG, "index ta destination" + EClosestIndex);
//                            Log.d(TAG, "Closest point to Destination: " + closestPoint);
//                            requestPolyline(castStringToLatLng(closestPoint), destToClose, empty, "walking", what);
//                            if (EClosestIndex >= SClosestIndex) {
//                                wpptNb = EClosestIndex - SClosestIndex - 1;
//                                if (wpptNb < 26) {
//                                    for (int j = SClosestIndex + 1; j < EClosestIndex; j++) {
//                                        waypoints.add(dest.get(j));
//                                    }
//                                    Log.d(TAG, "wahd " + waypoints);
//                                    if (locdest.size() > 0) {
//                                        locdest.clear();
//                                    }
//                                    locdest.add(castStringToLatLng(dest.get(SClosestIndex)));
//                                    new Handler().postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            requestPolyline(castStringToLatLng(dest.get(EClosestIndex)), locdest, waypoints, "driving", what);
//                                        }
//                                    }, 1500);
//
////                            requestPolyline(castStringToLatLng(destinations.get(EClosestIndex)), locdest, waypoints);
//                                } else {
//                                    for (int m = SClosestIndex + 1; m < SClosestIndex + 26; m++) {
//                                        waypoints1.add(dest.get(m));
//                                    }
//                                    for (int n = SClosestIndex + 27; n < EClosestIndex - 1; n++) {
//                                        waypoints2.add(dest.get(n));
//                                    }
//
//                                    Log.d(TAG, "lowl " + waypoints1);
//                                    Log.d(TAG, "tani " + waypoints2);
//                                    if (locdest1.size() > 0) {
//                                        locdest1.clear();
//                                    }
//                                    locdest1.add(castStringToLatLng(dest.get(SClosestIndex)));
//
//                                    if (locdest2.size() > 0) {
//                                        locdest2.clear();
//                                    }
//                                    locdest2.add(castStringToLatLng(dest.get(SClosestIndex + 26)));
//
////                            wayppt = waypoints1;
////                            wayppt = waypoints2;
//                                    new Handler().postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//
////                                    wayppt = waypoints1;
//                                            requestPolyline(castStringToLatLng(dest.get(SClosestIndex + 26)), locdest1, waypoints1, "driving", what);
////                                    wayppt = waypoints2;
//                                            requestPolyline(castStringToLatLng(dest.get(EClosestIndex)), locdest2, waypoints2, "driving", what);
//                                        }
//                                    }, 500);
//
////                            requestPolyline(castStringToLatLng(destinations.get(SClosestIndex + 24)), locdest1, waypoints1);
//                                }
//
//                            }
//                            else {
//                                wpptNb = -EClosestIndex + SClosestIndex + 1;
//
//
//                                if (wpptNb < 26) {
//                                    for (int j = SClosestIndex - 1; j > EClosestIndex; j--) {
//                                        waypoints.add(dest.get(j));
//                                    }
//                                    Log.d(TAG, "wahd " + waypoints);
//                                    if (locdest.size() > 0) {
//                                        locdest.clear();
//                                    }
//                                    locdest.add(castStringToLatLng(dest.get(SClosestIndex)));
//                                    new Handler().postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            requestPolyline(castStringToLatLng(dest.get(EClosestIndex)), locdest, waypoints, "driving", what);
//                                        }
//                                    }, 1500);
//
////                            requestPolyline(castStringToLatLng(destinations.get(EClosestIndex)), locdest, waypoints);
//                                } else {
//                                    for (int m = SClosestIndex - 1; m > SClosestIndex - 26; m--) {
//                                        waypoints1.add(dest.get(m));
//                                    }
//                                    for (int n = SClosestIndex - 27; n > EClosestIndex + 1; n--) {
//                                        waypoints2.add(dest.get(n));
//                                    }
//
//                                    Log.d(TAG, "lowl " + waypoints1);
//                                    Log.d(TAG, "tani " + waypoints2);
//                                    if (locdest1.size() > 0) {
//                                        locdest1.clear();
//                                    }
//                                    locdest1.add(castStringToLatLng(dest.get(SClosestIndex)));
//
//                                    if (locdest2.size() > 0) {
//                                        locdest2.clear();
//                                    }
//                                    locdest2.add(castStringToLatLng(dest.get(SClosestIndex - 26)));
//
////                            wayppt = waypoints1;
////                            wayppt = waypoints2;
//                                    new Handler().postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//
////                                    wayppt = waypoints1;
//                                            requestPolyline(castStringToLatLng(dest.get(SClosestIndex - 26)), locdest1, waypoints1, "driving", what);
////                                    wayppt = waypoints2;
//                                            requestPolyline(castStringToLatLng(dest.get(EClosestIndex)), locdest2, waypoints2, "driving", what);
//                                        }
//                                    }, 500);
//
////                            requestPolyline(castStringToLatLng(destinations.get(SClosestIndex + 24)), locdest1, waypoints1);
//                                }
//
//                            }
//                        }
//                    });
//                } else {
//                    ClosestPointFinder.findClosestPoint(latlngToString(latLng), dest, new ClosestPointFinder.DistanceCallback() {
//                        String TAG = " ";
//
//                        @Override
//                        public void onDistanceReceived(int distance) {
//                            Log.d(TAG, "Distance to Destination: " + distance);
//                            // Handle distance received
//                        }
//
//                        @Override
//                        public void onDistanceFailed() {
//                            // Handle distance request failure
//                        }
//
//                        @Override
//                        public void onClosestPointReceived(String closestPoint) {
//                            EClosestIndexH = dest.indexOf(closestPoint);
//                            Log.d(TAG, "index ta destination" + EClosestIndexH);
//                            Log.d(TAG, "Closest point to Destination: " + closestPoint);
//                            requestPolyline(castStringToLatLng(closestPoint), destToCloseH, empty, "walking", what);
//                            if (EClosestIndex >= SClosestIndex) {
//                                wpptNbH = EClosestIndexH - SClosestIndexH - 1;
//
//
//                                if (wpptNbH < 26) {
//                                    for (int j = SClosestIndexH + 1; j < EClosestIndexH; j++) {
//                                        waypointsH.add(dest.get(j));
//                                    }
//                                    Log.d(TAG, "wahd " + waypointsH);
//                                    if (locdestH.size() > 0) {
//                                        locdestH.clear();
//                                    }
//                                    locdestH.add(castStringToLatLng(dest.get(SClosestIndexH)));
//                                    new Handler().postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            requestPolyline(castStringToLatLng(dest.get(EClosestIndexH)), locdestH, waypointsH, "driving", what);
//                                        }
//                                    }, 1500);
//
////                            requestPolyline(castStringToLatLng(destinations.get(EClosestIndex)), locdest, waypoints);
//                                } else {
//                                    for (int m = SClosestIndexH + 1; m < SClosestIndexH + 26; m++) {
//                                        waypoints1H.add(dest.get(m));
//                                    }
//                                    for (int n = SClosestIndexH + 27; n < EClosestIndexH - 1; n++) {
//                                        waypoints2H.add(dest.get(n));
//                                    }
//
//                                    Log.d(TAG, "lowl " + waypoints1H);
//                                    Log.d(TAG, "tani " + waypoints2H);
//                                    if (locdest1H.size() > 0) {
//                                        locdest1H.clear();
//                                    }
//                                    locdest1H.add(castStringToLatLng(dest.get(SClosestIndexH)));
//
//                                    if (locdest2H.size() > 0) {
//                                        locdest2H.clear();
//                                    }
//                                    locdest2H.add(castStringToLatLng(dest.get(SClosestIndexH + 26)));
//
////                            wayppt = waypoints1;
////                            wayppt = waypoints2;
//                                    new Handler().postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//
////                                    wayppt = waypoints1;
//                                            requestPolyline(castStringToLatLng(dest.get(SClosestIndexH + 26)), locdest1H, waypoints1H, "driving", what);
////                                    wayppt = waypoints2;
//                                            requestPolyline(castStringToLatLng(dest.get(EClosestIndexH)), locdest2H, waypoints2H, "driving", what);
//                                        }
//                                    }, 500);
//
////                            requestPolyline(castStringToLatLng(destinations.get(SClosestIndex + 24)), locdest1, waypoints1);
//                                }
//
//                            } else {
//                                wpptNbH = -EClosestIndexH + SClosestIndexH + 1;
//
//
//                                if (wpptNbH < 26) {
//                                    for (int j = SClosestIndexH - 1; j > EClosestIndexH; j--) {
//                                        waypointsH.add(dest.get(j));
//                                    }
//                                    Log.d(TAG, "wahd " + waypointsH);
//                                    if (locdestH.size() > 0) {
//                                        locdestH.clear();
//                                    }
//                                    locdestH.add(castStringToLatLng(dest.get(SClosestIndexH)));
//                                    new Handler().postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            requestPolyline(castStringToLatLng(dest.get(EClosestIndexH)), locdestH, waypointsH, "driving", what);
//                                        }
//                                    }, 1500);
//
////                            requestPolyline(castStringToLatLng(destinations.get(EClosestIndex)), locdest, waypoints);
//                                } else {
//                                    for (int m = SClosestIndexH - 1; m > SClosestIndexH - 26; m--) {
//                                        waypoints1H.add(dest.get(m));
//                                    }
//                                    for (int n = SClosestIndexH - 27; n > EClosestIndexH - 1; n--) {
//                                        waypoints2H.add(dest.get(n));
//                                    }
//
//                                    Log.d(TAG, "lowl " + waypoints1H);
//                                    Log.d(TAG, "tani " + waypoints2H);
//                                    if (locdest1H.size() > 0) {
//                                        locdest1H.clear();
//                                    }
//                                    locdest1H.add(castStringToLatLng(dest.get(SClosestIndexH)));
//
//                                    if (locdest2H.size() > 0) {
//                                        locdest2H.clear();
//                                    }
//                                    locdest2H.add(castStringToLatLng(dest.get(SClosestIndexH - 26)));
//
////                            wayppt = waypoints1;
////                            wayppt = waypoints2;
//                                    new Handler().postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//
////                                    wayppt = waypoints1;
//                                            requestPolyline(castStringToLatLng(dest.get(SClosestIndexH - 26)), locdest1H, waypoints1H, "driving", what);
////                                    wayppt = waypoints2;
//                                            requestPolyline(castStringToLatLng(dest.get(EClosestIndexH)), locdest2H, waypoints2H, "driving", what);
//                                        }
//                                    }, 500);
//
////                            requestPolyline(castStringToLatLng(destinations.get(SClosestIndex + 24)), locdest1, waypoints1);
//                                }
//
//                            }
//                        }
//                    });
//
//                }
//            }
//
//            public boolean acumilatorDurationDistance (ArrayList < JSONObject > object) {
//                int totalTram = 0;
//                int totalH = 0;
//                DirectionsParser directionsParser = new DirectionsParser();
//                // Log.d(TAG, "doInBackground:::::::::::::::::::::::::::::: "+directionsParser.getdisdur(objectfortram.get(0))+"   "+directionsParser.getdisdur(objectfortram.get(1))+"    "+directionsParser.getdisdur(objectfortram.get(2)));
//                try {
//                    if (object.size() == 8) {
//                        totalTram = directionsParser.getdisdur(object.get(0)).get(1) + directionsParser.getdisdur(object.get(2)).get(1) + (directionsParser.getdisdur(object.get(4)).get(1) + directionsParser.getdisdur(object.get(5)).get(1)) / 3;
//                        totalH = directionsParser.getdisdur(object.get(1)).get(1) + directionsParser.getdisdur(object.get(3)).get(1) + (directionsParser.getdisdur(object.get(6)).get(1) + directionsParser.getdisdur(object.get(7)).get(1)) / 3;
//                        Log.d(TAG, "acumilatorDurationDistance: for tramway " + totalTram);
//                        Log.d(TAG, "acumilatorDurationDistance: for H bus " + totalH);
//                    } else if (object.size() == 6) {
//                        totalTram = directionsParser.getdisdur(object.get(0)).get(1) + directionsParser.getdisdur(object.get(2)).get(1) + directionsParser.getdisdur(object.get(4)).get(1) / 3;
//                        totalH = directionsParser.getdisdur(object.get(1)).get(1) + directionsParser.getdisdur(object.get(3)).get(1) + directionsParser.getdisdur(object.get(5)).get(1) / 3;
//                        Log.d(TAG, "acumilatorDurationDistance:::: for tramway " + totalTram);
//                        Log.d(TAG, "acumilatorDurationDistance:::: for H bus " + totalH);
//                    }
//                    object.clear();
//                } catch (Exception e) {
//                    Toast.makeText(this, "click on the location button first", Toast.LENGTH_SHORT).show();
//                    Log.d(TAG, "acumilatorDurationDistance: " + e);
//                }
//                return bestRoute(totalTram, totalH);
//            }
//
//            //better option between tram and H
//            public boolean bestRoute ( int totalt, int totalth){
//                if (totalt <= totalth) {
//                }
//                    mMap.addPolyline(polylineOptionsTramW);
//                    mMap.addPolyline(polylineOptionsTram);
//                    mMap.addPolyline(ppTram);
//                } else {
//                    mMap.addPolyline(polylineOptionsHW);
//                    mMap.addPolyline(polylineOptionsH);
//                    mMap.addPolyline(ppH);
//                }
//                return totalt <= totalth;
//            }

    //--------------------------------Move Camera/ Add Marker-------------------------------
    private void moveCameraToLocation(LatLng location, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));
        mMap.addMarker(new MarkerOptions().position(location));
    }

    //--------------------------------Latlng to String--------------------------------
    public String latlngToString(LatLng lg) {
        String stringlatlong = Double.toString(lg.latitude) + "," + Double.toString(lg.longitude);
        return stringlatlong;
    }

    //--------------------------------ModelTram to Latlng--------------------------------
    public static LatLng castToLatLng(ModelTram modelTram) {
        String[] latlong = modelTram.getCoordinates().split(",");
        double longitude = Double.parseDouble(latlong[1]);
        double latitude = Double.parseDouble(latlong[0]);
        LatLng latLng = new LatLng(latitude, longitude);
        return latLng;
    }

    //--------------------------------String to Latlng--------------------------------
    public static LatLng castStringToLatLng(String string) {
        String[] latlong = string.split(",");
        double longitude = Double.parseDouble(latlong[1]);
        double latitude = Double.parseDouble(latlong[0]);
        LatLng latLng = new LatLng(latitude, longitude);
        return latLng;
    }

    //--------------------------------Tap twice to exit--------------------------------
    @Override
    public void onBackPressed() {
        if (mPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(this, "Tap twice to exit", Toast.LENGTH_SHORT).show();
        }
        mPressed = System.currentTimeMillis();
    }
}
