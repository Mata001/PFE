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



public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static String TAG="info:";
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    public static final int TIME_INTERVAL = 2000;
    private long mPressed;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ArrayList <LatLng> listPoints;
    int i;
    ArrayList<String> destinations;
    int SClosestIndex;
    int EClosestIndex;
    List<String> waypoints;
    ArrayList<LatLng> locToClose;
    ArrayList<LatLng> destToClose;


    ArrayList <LatLng>locdest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listPoints = new ArrayList<>();
        locdest = new ArrayList<>();
        destinations = new ArrayList<>();
        locToClose = new ArrayList<>();
        destToClose = new ArrayList<>();
        waypoints = new ArrayList<>();
        FloatingActionButton currentLocationBtn = findViewById(R.id.currLoc);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();


//--------------------Enable Location services---------------

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
//---------------------------distance request--------------------------------------------
        DistanceMatrixApiUtils.getDistance("35.66125490371664,-0.6320125940081027|35.665560367115546,-0.6346715501204017|35.67129104455131,-0.6381626487760172|35.67608877397281,-0.6411018327659121", "Caféteria CHERGUI,6,Oran", new DistanceMatrixApiUtils.DistanceCallback() {
            @Override
            public void onDistanceReceived(int distance) {
                Log.d(TAG, "Distance: " + distance + " meters");
            }

            @Override
            public void onDistanceFailed() {
                Log.d(TAG, "Failed to retrieve distance.");
            }
        });

//-----------------------------currentLocation-------------------------------------
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        currentLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableCurrentLocation();
                mMap.clear();
            }
        });
//        --------affichage--------------
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//----------------------------------Go next Activity----------------------------
        // Initialize Places.
        if(!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCnMasBoIdVpjj97TGyBUA44oC09BMxjUs");
        }
// Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setLocationRestriction(RectangularBounds.newInstance(
                new LatLng(35.604562, -0.748931),
                new LatLng(35.788521, -0.501718)));

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));


        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                //mMap.clear();
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                Toast.makeText(MainActivity.this, "ok " + place.getLatLng(), Toast.LENGTH_SHORT).show();
                //MarkerOptions markerOptions = new MarkerOptions();
                //markerOptions.position(place.getLatLng());
                //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                //mMap.addMarker(markerOptions);
                if (destToClose.size() > 0) {
                    destToClose.clear();
                }
                destToClose.add(place.getLatLng());
                moveCameraToLocation(place.getLatLng(), 15);

                //------------------------------find closest point--------------------------------

                ClosestPointFinder.findClosestPoint(latlngToString(place.getLatLng()), destinations, new ClosestPointFinder.DistanceCallback() {
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
                    public void onClosestPointReceived(String closestPoint,int index) {
                        EClosestIndex = 12;
                        Log.d(TAG, "Closest point to Destination: " + closestPoint);
                        requestPolyline( castStringToLatLng(closestPoint), destToClose);
                    }
                });

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
        for (i=SClosestIndex+1; i<EClosestIndex; i++){
            waypoints.add(destinations.get(i));
        }
        Log.d(TAG, "waypoints arraylist: " +waypoints);
    }

    //-------------------Tap twice to exit------------------------
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

     //--------------------------------------------------
//-------------------------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        int start=0;
        int end=25;
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (int i = 0;i<32;i++){
//
//                    DataSnapshot dataSnapshot = snapshot.getChildren();
                for (i=start;i<end;i++)
                {
                    ModelTram modelTram = snapshot.child(Integer.toString(i)).getValue(ModelTram.class);
//                }
            //    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //ModelTram modelTram = dataSnapshot.getValue(ModelTram.class);
                    destinations.add(modelTram.getCoordinates());
                    LatLng latLng = castToLatLng(modelTram);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng).title(modelTram.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.piner));
                    mMap.addMarker(markerOptions).showInfoWindow();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Enable current location button and show current location on the map
            enableCurrentLocation();
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

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
                    String url = getRequestUrl(listPoints.get(0), listPoints.get(1));
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    taskRequestDirections.execute(url);
                }
            }
        } );
    }

    private String getRequestUrl(LatLng origin, LatLng dest) {
        String waypointsString = TextUtils.join("|via:", waypoints);
        //Value of origin
        String str_org = "origin=" + origin.latitude +","+origin.longitude;
        //Value of destination
        String str_dest = "destination=" + dest.latitude+","+dest.longitude;
        //Set value enable alternative ways
        String alt = "alternatives=TRUE";
        //Set waypoints those are the bus stations
        String way = "waypoints=" + waypointsString;
        //Mode for find direction
        String mode =  "mode=walking";
        //String key for api key
        String key = "key=AIzaSyBXqq6EL6IwRunjoA9r7ctDwzikEaN1FEE";
        //Build the full param
        String param = alt +"&"+ str_org +"&" +mode +"&"+ str_dest +
                "&"+ way+
                "&" +key;
        //Output format
        String output = "json";
        //Create url to request
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
        return url;
    }
    private String requestDirection(String reqUrl) throws Exception {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try{
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
    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return  responseString;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse json here
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }
    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>> > {

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

                    points.add(new LatLng(lat,lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(20f);
                polylineOptions.color(Color.rgb(252, 3, 161));
                polylineOptions.startCap(new RoundCap());
                polylineOptions.jointType(1);
                polylineOptions.geodesic(true);
                List<PatternItem> pattern;
                pattern = Arrays.asList(new Dot(),new Gap(30));
                polylineOptions.pattern(pattern);

            }

            if (polylineOptions!=null) {
                mMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getApplicationContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void enableCurrentLocation() {
        // Check if the device has location services enabled
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //database marker

        // Enable the location layer on the map
        mMap.setMyLocationEnabled(true);

        // Get the last known location of the device
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Move the camera to the user's current location
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    moveCameraToLocation(currentLatLng,15);
                    if (locdest.size() > 0) {
                        locdest.clear();
                    }
                    locdest.add(currentLatLng);
                    if (locToClose.size() > 0) {
                        locToClose.clear();
                    }
                    locToClose.add(currentLatLng);


//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oran, 10));
                    Log.d(TAG, "location is : " + currentLatLng);
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), 20));
//------------------------------find closest point--------------------------------

                    ClosestPointFinder.findClosestPoint(latlngToString(currentLatLng), destinations, new ClosestPointFinder.DistanceCallback() {
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
                        public void onClosestPointReceived(String closestPoint, int index) {
                            SClosestIndex = 5;
                            Log.d(TAG, "Closest point to current location: " + closestPoint);
                           requestPolyline( castStringToLatLng(closestPoint), locToClose);
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
    private void moveCameraToLocation(LatLng location, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));
        mMap.addMarker(new MarkerOptions().position(location));
    }
    public String latlngToString(LatLng lg){
    String stringlatlong = Double.toString(lg.latitude)+","+Double.toString(lg.longitude);
    return stringlatlong;
    }
    public LatLng castToLatLng(ModelTram modelTram){
        String[] latlong = modelTram.getCoordinates().split(",");
        double longitude = Double.parseDouble(latlong[1]);
        double latitude = Double.parseDouble(latlong[0]);
        LatLng latLng = new LatLng(latitude,longitude);
        return latLng;
    }
    public LatLng castStringToLatLng(String string){
        String[] latlong = string.split(",");
        double longitude = Double.parseDouble(latlong[1]);
        double latitude = Double.parseDouble(latlong[0]);
        LatLng latLng = new LatLng(latitude,longitude);
        return latLng;
    }

    public void requestPolyline(LatLng latLng , ArrayList<LatLng> lol) {
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
            String url = getRequestUrl(lol.get(0), lol.get(1));
            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
            taskRequestDirections.execute(url);
        }
    }

}