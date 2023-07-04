package com.example.pfe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserGuideActivityForbutton extends AppCompatActivity {
    private ImageView imageView;
    private TextView textView;
    private Button nextButton;
    private Button skipButton;

    private int currentStep;
    private int[] screenshotIds; // Array of screenshot resource IDs
    private String[] stepTexts; // Array of step texts


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

    private void showStep(int step) {
        imageView.setImageResource(screenshotIds[step]);
        textView.setText(stepTexts[step]);
    }

    private void startMainActivity() {

        Intent intent = new Intent(UserGuideActivityForbutton.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
