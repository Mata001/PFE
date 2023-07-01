package com.example.pfe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.*;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;


public class SplashScreen extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String USER_GUIDE_KEY = "UserGuideShown";
    private static String TAG = "Splash:";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

// Get shared preferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if the user has already seen the user guide
        boolean userGuideShown = sharedPreferences.getBoolean(USER_GUIDE_KEY, false);
        Log.d(TAG, "onCreate: ====" +userGuideShown);
        if (!userGuideShown) {
            // User guide not shown yet, start UserGuideActivity
            Intent intent = new Intent(SplashScreen.this, UserGuideActivity.class);
            startActivity(intent);
            // Set user guide shown flag in shared preferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(USER_GUIDE_KEY, true);
            editor.apply();
     } else {

      // getSupportActionBar().hide();
         setContentView(R.layout.activity_splash_screen);
     new Handler().postDelayed(new Runnable(){
         @Override
         public void run() {
             Intent intent;
             intent = new Intent(SplashScreen.this,MainActivity.class);
             startActivity(intent);
             finish();
         }
     }, 500);


 }
}}