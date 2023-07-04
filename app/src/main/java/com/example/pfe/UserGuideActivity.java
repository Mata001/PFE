package com.example.pfe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserGuideActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView textView;
    private Button nextButton;
    private Button skipButton;

    private int currentStep;
    private int[] screenshotIds; // Array of screenshot resource IDs
    private String[] stepTexts; // Array of step texts

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String USER_GUIDE_KEY = "UserGuideShown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);

        // Initialize views
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        nextButton = findViewById(R.id.nextButton);
        skipButton = findViewById(R.id.skipButton);

        // Set up the screenshot IDs and step texts
        screenshotIds = new int[]{R.drawable.screenshot_0,R.drawable.screenshot_1, R.drawable.screenshot_2, R.drawable.screenshot_3, R.drawable.screenshot_4,R.drawable.screenshot_5};
        stepTexts = new String[]{"Step 1: ", "Step 2: ", "Step 3: ", "Step 4: " , "Step 5: ", "Step 6: "};


        // Get shared preferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if the user has already seen the user guide
        boolean userGuideShown = sharedPreferences.getBoolean(USER_GUIDE_KEY, false);
        if (!userGuideShown) {
            startMainActivity(); // Skip user guide if already shown
        } else {
            currentStep = 0;
            showStep(currentStep);

            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentStep++;
                    if (currentStep < screenshotIds.length) {
                        showStep(currentStep);
                    } else {
                        startMainActivity();
                    }
                }
            });

            skipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startMainActivity();
                }
            });
        }
    }

    private void showStep(int step) {
        imageView.setImageResource(screenshotIds[step]);
        textView.setText(stepTexts[step]);
    }

    private void startMainActivity() {
        // Set user guide shown flag in shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(USER_GUIDE_KEY, true);
        editor.apply();

        Intent intent = new Intent(UserGuideActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
