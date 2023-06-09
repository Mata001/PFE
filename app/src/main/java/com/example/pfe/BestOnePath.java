package com.example.pfe;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BestOnePath implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static String TAG = "info:";
    private GoogleMap mMap;
    int i;
    int wpptNb;
    int SClosestIndex;
    int EClosestIndex;
    List<String> waypoints;
    List<String> waypoints1;
    List<String> waypoints2;
    List<String> wayppt;
    List<String> empty;
    boolean mod = false;
//    empty = new ArrayList<>();
//    waypoints = new ArrayList<>();
//    waypoints1 = new ArrayList<>();
//    waypoints2 = new ArrayList<>();
//    wayppt = new ArrayList<>();

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }

    public void readData(FirebaseCallback firebaseCallback) {
        ArrayList<Object> info = new ArrayList<>();
        ArrayList<String> destinations = new ArrayList<>();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int start = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String meanName = dataSnapshot.getKey();
                    long end = snapshot.child(meanName).getChildrenCount();

                    for (i = start; i < 27; i++) {
                        ModelTram modelTram = snapshot.child(meanName).child(Integer.toString(i)).getValue(ModelTram.class);
                        destinations.add(modelTram.getCoordinates());
//                        LatLng test = new LatLng(35.704571, -0.588370);
//                        LatLng latLng = MainActivity.castToLatLng(modelTram);
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.position(test);
//                            .title(modelTram.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.piner));
//                    mMap.addMarker(markerOptions);
                    }
                        Log.d(TAG, "Mean :"+meanName+", Number :"+end+", Stations :"+destinations);

                   /* Log.d(TAG, "whole table " + destinations);

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
                            info.add(EClosestIndex);
                            info.add(closestPoint);
//                        requestPolyline(castStringToLatLng(closestPoint), destToClose, empty, "walking");
                        }
                    });

                    ClosestPointFinder.findClosestPoint("35.665752,-0.629140", destinations, new ClosestPointFinder.DistanceCallback() {
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
                            info.add(SClosestIndex);
                            info.add(closestPoint);
                            firebaseCallback.onCallback(info);
                            Log.d(TAG, "onClosestPointReceived: "+snapshot.getKey()+"   "+info);
                        }
                    });*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public interface FirebaseCallback {
        void onCallback(ArrayList<Object> list);
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