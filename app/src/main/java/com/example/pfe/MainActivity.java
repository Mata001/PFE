package com.example.pfe;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, BestOnePath.StationsReturned {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static String TAG = "info:";
    public static GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    public static final int TIME_INTERVAL = 2000;
    private ImageView header_Arrow_Image;
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
    private static int counter = 0;
    private static int counterType = 0;
    ArrayList<LatLng> locdest2;
    //    public static ArrayList<JSONObject> meanObject;
    static boolean mod = false;
    public static int shortestDistance;
    public static int shortestDistanceIndex;
        BottomSheetBehavior bottomSheetBehavior;
    CoordinatorLayout bottomSheetContainer;
    ArrayList<Object> listOfLists;
    public static List<ArrayList<Object>> lakhra;
    TextView destinationName;
    public static RecyclerView recyclerView;
    //    public ArrayList<StationItem> stationItems;
    MyAdapter myAdapter;
    public BestOnePath.TaskParser taskParser;

    public static TextView meanName;
    public static TextView meanDuration;
    public static TextView orginDuration;
    public static TextView destDuration;
    public static ImageView meanIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BestOnePath bestOnePath = new BestOnePath(this);
        View layoutInclude = findViewById(R.id.layout_include);
        bottomSheetBehavior =BottomSheetBehavior.from(layoutInclude);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        header_Arrow_Image = findViewById(R.id.bottom_sheet_arrow);
        header_Arrow_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

            }
        });
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                header_Arrow_Image.setRotation(slideOffset * 180);
            }
        });


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
        recyclerView = layoutInclude.findViewById(R.id.stationsList);
        meanName = (TextView) findViewById(R.id.meanName);
        meanDuration = (TextView) findViewById(R.id.mean_duration);
        orginDuration = (TextView) findViewById(R.id.walk_duration_origin);
        destDuration = (TextView) findViewById(R.id.walk_duration_dest);
        meanIcon = (ImageView) findViewById(R.id.meanIcon);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        FloatingActionButton currentLocationBtn = findViewById(R.id.currLoc);
        FloatingActionButton userGuideBtn = findViewById(R.id.userGuide);
        FloatingActionButton mapStyleBtn = findViewById(R.id.style);
        FloatingActionButton mapTypeBtn = findViewById(R.id.maptype);
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
                bottomSheetBehavior.setPeekHeight(0);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                mMap.clear();
                moveCameraToLocation(locdest1.get(0), 15);
            }
        });
        //-----------------------------userguide -------------------------------------
        userGuideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserGuideActivityForbutton.class);
                startActivity(intent);
            }
        });
        //-----------------------------mapStyle -------------------------------------
        mapStyleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter++;

                if (counter == 1) {
                    // Perform action 1
                    Log.d(TAG, "counterstyle1: " +counter);
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.darkmapstyle));
                } else if (counter == 2) {
                    // Perform action 2
                    Log.d(TAG, "counterstyle2: " +counter);
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.normalmapstyle));
                } else if (counter == 3) {
                    // Perform action 3
                    Log.d(TAG, "counterstyle3: " +counter);
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.mapstyle));
                    counter = 0; // Reset the counter
                }
            }
        });


        //-----------------------------mapSTpe-------------------------------------
        mapTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counterType++;

                if (counterType == 1) {
                    // Perform action 1
                    Log.d(TAG, "countertype1: " +counterType);
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (counterType == 2) {
                    Log.d(TAG, "countertype2: " +counterType);
                    // Perform action 2
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                } else if (counterType == 3) {
                    // Perform action 3
                    Log.d(TAG, "countertype3: " +counterType);
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    counterType = 0; // Reset the counter
                }
            }
        });
//--------------------------------Display map---------------------------------------
        mapFragment.getMapAsync(this);
//----------------------------55------------------------------
        // Initialize Places.
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCpqiUvaAud8Fa3o9L29kSJ5Yzu7V8pips");
//            AIzaSyARlcOfXAA-JfGWFW6VH8AbtQbI96qjj6I hna ytbdl
//            AIzaSyCnMasBoIdVpjj97TGyBUA44oC09BMxjUs hna ytbdl
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

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(place.getLatLng()).title("destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.piner));
                mMap.addMarker(markerOptions).showInfoWindow();

                BestOnePath.distances.clear();
                shortestDistance = Integer.MAX_VALUE;
                shortestDistanceIndex = -1;
                lakhra = new ArrayList<>();
                BestOnePath.polylineNumbers.clear();
                BestOnePath.polylineOptionsArrayList.clear();
                //mMap.clear();
                Log.d(TAG, "Aya allah yrb7 " + BestOnePath.distances + "    " + shortestDistance);
                destinationName = (TextView) layoutInclude.findViewById(R.id.destinationName);
                destinationName.setText(place.getName());
                bestOnePath.readData(new BestOnePath.FirebaseCallback() {
                    @Override
                    public void onCallback(ArrayList<Object> list, long number) {
                        listOfLists = new ArrayList<>();
                        listOfLists = (ArrayList<Object>) list.clone();
                        lakhra.add(listOfLists);
//                        if (lakhra.size()==number){
//                            Log.d(TAG, "lakra fel meathod "+lakhra.get(0));
////                            Log.d(TAG, "lakra fel meathod "+lakhra.get(shortestDistanceIndex));
//                            Log.d(TAG, "onCallback: " + shortestDistanceIndex);
//                        }
                    }
                }, latlngToString(place.getLatLng()),latlngToString( locdest1.get(0)));
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                }, 10000);
//                latlngToString(locdest1.get(0))
//                ecole :35.665618,-0.634003
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
    public static String getRequestUrl(LatLng origin, LatLng dest, List<String> wayppt, String mode) {
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
        String mod = "mode=" + mode;
        //String key for api key
        String key = "key=AIzaSyCpqiUvaAud8Fa3o9L29kSJ5Yzu7V8pips";
        //            AIzaSyARlcOfXAA-JfGWFW6VH8AbtQbI96qjj6I hna ytbdl
        //            AIzaSyCnMasBoIdVpjj97TGyBUA44oC09BMxjUs hna ytbdl
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
            String url = getRequestUrl(lol.get(0), lol.get(1), wayppt, "driving");
            BestOnePath.TaskRequestDirections taskRequestDirections = new BestOnePath.TaskRequestDirections();
            taskRequestDirections.execute(url, "1");
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
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(location).title("I'm here").icon(BitmapDescriptorFactory.fromResource(R.drawable.piner));
        mMap.addMarker(markerOptions).showInfoWindow();
    }

    //--------------------------------Latlng to String--------------------------------
    public String latlngToString(LatLng lg) {
        String stringlatlong = Double.toString(lg.latitude) + "," + Double.toString(lg.longitude);
        return stringlatlong;
    }

    //--------------------------------ModelTram to Latlng--------------------------------
    public static LatLng castToLatLng(ModelNameCoordinates modelNameCoordinates) {
        String[] latlong = modelNameCoordinates.getCoordinates().split(",");
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


    @Override
    public void onCallbackStations(ArrayList<StationItem> list) {
        Log.d(TAG, "onCallbackStations: "+list);
        ArrayList<StationItem> stationItems = new ArrayList<StationItem>(list);
        Log.d(TAG, "statttttt " + stationItems);


        myAdapter = new MyAdapter(this, stationItems);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(myAdapter);
        } else if (recyclerView == null) {
            Log.d(TAG, "kayn errorrr ");
        }
        bottomSheetBehavior.setPeekHeight(150);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }
}
