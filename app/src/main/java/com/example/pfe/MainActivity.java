package com.example.pfe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.service.controls.actions.FloatAction;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.content.Context;



public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    public static final int TIME_INTERVAL = 2000;
    private long mPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton currentLocationBtn = findViewById(R.id.currLoc);

//--------------------Enable Location services---------------

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }


//-----------------------------currentLocation-------------------------------------
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        currentLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableCurrentLocation();
            }
        });
//        --------affichage--------------
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//----------------------------------Go next Activity----------------------------
        EditText editText = findViewById(R.id.destination);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Page2.class);
                startActivity(intent);
//                Toast.makeText(MainActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
            }
        });
//        ----------

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

    //--------------------------------------------
//    --------------------------currentLocation-----------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


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
    }

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
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 20));
                    Toast.makeText(MainActivity.this, "hawala wayni "+currentLatLng, Toast.LENGTH_SHORT).show();
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), 20));

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
}