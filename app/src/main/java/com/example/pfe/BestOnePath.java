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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class BestOnePath implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static String TAG = "info:";
    private GoogleMap mMap;
    int i;
    int wpptNb;
    int SClosestIndex=0;
    int EClosestIndex;
    List<String> waypoints= new ArrayList<>();
    List<String> waypoints1=new ArrayList<>();
    List<String> waypoints2=new ArrayList<>();
    List<String> wayppt = new ArrayList<>();
    List<List<String>> meanWaypoints =new ArrayList<>();
    static final ArrayList<Object> meanStations= new ArrayList<>();
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
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int start = 0;
//                Iterator<DataSnapshot> iterator =snapshot.getChildren().iterator();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ArrayList<String> destinations = new ArrayList<>();
                    String meanName = dataSnapshot.getKey();
                    long end = snapshot.child(meanName).getChildrenCount();
//                    destinations.clear();
                    for (i = start; i < end; i++) {
                        ModelTram modelTram = snapshot.child(meanName).child(Integer.toString(i)).getValue(ModelTram.class);
                        destinations.add(modelTram.getCoordinates());
//                        LatLng test = new LatLng(35.704571, -0.588370);
//                        LatLng latLng = MainActivity.castToLatLng(modelTram);
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.position(test);
//                            .title(modelTram.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.piner));
//                    mMap.addMarker(markerOptions);
                    }
                    Log.d(TAG, "Mean :"+meanName+", Number :"+end +", Stations :"+ destinations);
//                    info.add(meanName);
                    //Closest transport mean stations for destination
                    ClosestPointFinder.findClosestPoint("35.693351,-0.589981", destinations, new ClosestPointFinder.DistanceCallback() {
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
                            Log.d(TAG, "Closest point to Destination for "+ meanName +" is: " + closestPoint);
//                            info.add(EClosestIndex);
                            info.clear();
                            info.add(meanName);
                            info.add(EClosestIndex);
                            info.add(closestPoint);
                        }
                    });
                    //Closest transport mean stations for origine
                    ClosestPointFinder.findClosestPoint("35.627148,-0.598247", destinations, new ClosestPointFinder.DistanceCallback() {
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
                            Log.d(TAG, "Index ta Origin "+ SClosestIndex);
                            Log.d(TAG, "Closest point to Origin for "+ meanName +" is: " + closestPoint);
                            info.add(SClosestIndex);
                            info.add(closestPoint);
                            Log.d(TAG, "Table needed "+info);
                            meanStations.add(info);
                            firebaseCallback.onCallback(meanStations);

                        }
                    });
//                            meanStations.add(list);
//                            Log.d(TAG, "mean Stations "+meanStations);
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
/*

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
        }*/