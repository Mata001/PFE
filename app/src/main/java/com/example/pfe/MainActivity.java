package com.example.pfe;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static String TAG = "info:";
    public static GoogleMap mMap;
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
    public static ArrayList<JSONObject> meanObject;
    static boolean mod = false;
    public static int shortestDistance;
    ArrayList<Object> listOfLists;
    List<ArrayList<Object>> lakhra;

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
        meanObject = new ArrayList<>();

        BestOnePath bestOnePath = new BestOnePath();
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
                BestOnePath.distances.clear();
                shortestDistance = Integer.MAX_VALUE;
                lakhra = new ArrayList<>();
                BestOnePath.polylineNumbers.clear();
                BestOnePath.polylineOptionsArrayList.clear();
                mMap.clear();

                bestOnePath.readData(new BestOnePath.FirebaseCallback() {
                    @Override
                    public void onCallback(ArrayList<Object> list, long number) {
                    }
                },latlngToString(place.getLatLng()),latlngToString(locdest1.get(0)));
//                latlngToString(locdest1.get(0))
            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
//        ----------
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
                    locdest1.clear();
                    locdest1.add(currentLatLng);
                } else {
                    Toast.makeText(MainActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    //----------------------------On Place searched listener------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.class, R.raw.mapstyle));
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
        wayppt.clear();
        return url;
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
    public void requestPolyline(LatLng latLng, ArrayList<LatLng> lol, List<String> wayppt, String mode) {
        mod = modeProvider(mode);
        String drawTrue = String.valueOf(true);
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
            BestOnePath.TaskRequestDirections taskRequestDirections = new BestOnePath.TaskRequestDirections();
            taskRequestDirections.execute(url,"1");
        }
    }

    //--------------------------------Walking/Driving polyline--------------------------------
    public boolean modeProvider(String mod) {
        if (mod == "walking")
            return false;
        else return true;
    }

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
