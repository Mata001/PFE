package com.example.pfe;

import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BestOnePath implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static String TAG = "info:";
    private GoogleMap mMap;
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
    int SClosestIndex;
    int EClosestIndex;
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
//    listPoints = new ArrayList<>();
//    locdest = new ArrayList<>();
//    locdest1 = new ArrayList<>();
//    locdest2 = new ArrayList<>();
//    empty = new ArrayList<>();

//    locToClose = new ArrayList<>();
//    destToClose = new ArrayList<>();
//    waypoints = new ArrayList<>();
//    waypoints1 = new ArrayList<>();
//    waypoints2 = new ArrayList<>();
//    wayppt = new ArrayList<>();

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }

    public void retrieveData() {

        ArrayList<String> destinations = new ArrayList<>();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("H");
//    FirebaseDatabase fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//    FloatingActionButton currentLocationBtn = findViewById(R.id.currLoc);
//    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//    AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        int shortDistance = Integer.MAX_VALUE;
        int shortDuration = Integer.MAX_VALUE;


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int start = 0;
                long end = snapshot.getChildrenCount();
//                 for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                for (i = start; i < end; i++) {
                    ModelTram modelTram = snapshot.child(Integer.toString(i)).getValue(ModelTram.class);
                    destinations.add(modelTram.getCoordinates());
//                    LatLng test = new LatLng(35.704571, -0.588370);
                    LatLng latLng = MainActivity.castToLatLng(modelTram);
                    Log.d(TAG, "index " + i + " coordinate " + latLng);
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.position(test);
//                            .title(modelTram.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.piner));
//                    mMap.addMarker(markerOptions);
                }

                Log.d(TAG, "whole table " + destinations);

                ClosestPointFinder.findClosestPoint("35.726535,-0.588799", destinations, new ClosestPointFinder.DistanceCallback() {
                    @Override
                    public void onDistanceReceived(int distance) {
                        Log.d(TAG, "distances dest " + distance);
                    }

                    @Override
                    public void onDistanceFailed() {

                    }

                    @Override
                    public void onClosestPointReceived(String closestPoint) {
                        EClosestIndex = destinations.indexOf(closestPoint);
                        Log.d(TAG, "index ta Destination " + EClosestIndex);
                        Log.d(TAG, "Closest point to Destination: " + closestPoint);
//                        requestPolyline(castStringToLatLng(closestPoint), destToClose, empty, "walking");
                    }
                });

                ClosestPointFinder.findClosestPoint("35.665752,-0.629140", destinations ,new ClosestPointFinder.DistanceCallback() {
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
                        SClosestIndex = destinations.indexOf(closestPoint);
                        Log.d(TAG, "index ta Origin " + SClosestIndex);
                        Log.d(TAG, "Closest point to Origin: " + closestPoint);

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
 /*if (wpptNb < 26) {
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
                    }
                else {
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
                    }*/