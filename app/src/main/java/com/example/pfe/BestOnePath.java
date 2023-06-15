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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BestOnePath implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static String TAG = "info:";
    private GoogleMap mMap;
    int i;
    int wpptNb;
    int SClosestIndex = 0;
    int EClosestIndex;
    List<String> waypoints = new ArrayList<>();
    List<String> waypoints1 = new ArrayList<>();
    List<String> waypoints2 = new ArrayList<>();
    List<String> wayppt = new ArrayList<>();
    List<List<String>> meanWaypoints = new ArrayList<>();
    //    static final ArrayList<Object> meanStations= new ArrayList<>();
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
                    Log.d(TAG, "Mean :" + meanName + ", Number :" + end + ", Stations :" + destinations);
//                    info.add(meanName);
                    //Closest transport mean stations for destination
                    ClosestPointFinder.findClosestPoint("35.705813,-0.527153", destinations, new ClosestPointFinder.DistanceCallback() {
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
//                            Log.d(TAG, "index ta Destination " + EClosestIndex);
//                            Log.d(TAG, "Closest point to Destination for " + meanName + " is: " + closestPoint);
//                            info.add(EClosestIndex);
                            info.clear();
                            info.add(meanName);
                            info.add(SClosestIndex);
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

                            EClosestIndex = destinations.indexOf(closestPoint);
//                            Log.d(TAG, "Index ta Origin " + SClosestIndex);
//                            Log.d(TAG, "Closest point to Origin for " + meanName + " is: " + closestPoint);
                            info.add(EClosestIndex);
                            info.add(closestPoint);
                            if (EClosestIndex > SClosestIndex) {
                                wpptNb = EClosestIndex - SClosestIndex - 1;
                                if (wpptNb < 26) {
                                    waypoints = destinations.subList(SClosestIndex + 1, EClosestIndex);
//                                    Log.d(TAG, "wahd " + waypoints);
                                    info.add(waypoints);
                                } else {
                                    waypoints1 = destinations.subList(SClosestIndex + 1, SClosestIndex + 26);
                                    waypoints2 = destinations.subList(SClosestIndex + 27, EClosestIndex);
//                                    Log.d(TAG, "lowl " + waypoints1);
//                                    Log.d(TAG, "tani " + waypoints2);
                                    info.add(waypoints1);
                                    info.add(destinations.get(SClosestIndex + 26));
                                    info.add(waypoints2);
                                }
                                firebaseCallback.onCallback(info);

                            } else {
                                wpptNb = -EClosestIndex + SClosestIndex - 1;
                                if (wpptNb < 26) {
                                    waypoints = destinations.subList(EClosestIndex + 1, SClosestIndex - 1);
                                    Log.d(TAG, "wahd " + waypoints);
                                    info.add(waypoints);
                                } else {
                                    waypoints1 = destinations.subList(EClosestIndex + 1, EClosestIndex + 26);
                                    waypoints2 = destinations.subList(EClosestIndex + 27, SClosestIndex);
//                                    Log.d(TAG, "lowl " + waypoints1);
//                                    Log.d(TAG, "tani " + waypoints2);
                                    info.add(waypoints1);
                                    info.add(destinations.get(EClosestIndex + 26));
                                    info.add(waypoints2);

                                }
                                firebaseCallback.onCallback(info);
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
        void onCallback(ArrayList<Object> list);
    }

    public void getInfo(ArrayList<Object> lista) {
//            MainActivity.TaskRequestDirections taskRequestDirections = new MainActivity.TaskRequestDirections();
//            taskRequestDirections.execute(url);
        List<String> khawi = new ArrayList<>();
        Log.d(TAG, "karitha siyed mn " + lista);
        String originUrl = MainActivity.getRequestUrl(new LatLng(35.693351, -0.589981), castObjectToLatLng(lista.get(2)), khawi);
        String destinationUrl = MainActivity.getRequestUrl(new LatLng(35.627148, -0.598247), castObjectToLatLng(lista.get(4)), khawi);
        if (lista.size() == 6) {
            String url = MainActivity.getRequestUrl(castObjectToLatLng(lista.get(2)), castObjectToLatLng(lista.get(4)), castObjectToList(lista.get(5)));
            Log.d(TAG, "urls ta direction ki maykounch chunk \n" + originUrl + "\n" + destinationUrl + "\n" + url);
        } else if (lista.size() == 8) {
            String url1 = MainActivity.getRequestUrl(castObjectToLatLng(lista.get(2)), castObjectToLatLng(lista.get(6)), castObjectToList(lista.get(5)));
            String url2 = MainActivity.getRequestUrl(castObjectToLatLng(lista.get(6)), castObjectToLatLng(lista.get(5)), castObjectToList(lista.get(7)));
            Log.d(TAG, "urls ta direction ki ykoun chunk \n" + originUrl + "\n" + destinationUrl + "\n" + url1 + "\n" + url2);
        } else {
            Log.d(TAG, "ak ghalt sa7bi me ttali ");
        }

    }

    public LatLng castObjectToLatLng(Object object) {
        String string = object.toString();
        String[] latlong = string.split(",");
        double longitude = Double.parseDouble(latlong[1]);
        double latitude = Double.parseDouble(latlong[0]);
        LatLng latLng = new LatLng(latitude, longitude);
        return latLng;
    }

    public List<String> castObjectToList(Object object) {
        List<String> waypointsList = new ArrayList<>();
        waypointsList = (List<String>) object;
        return waypointsList;
    }
}
