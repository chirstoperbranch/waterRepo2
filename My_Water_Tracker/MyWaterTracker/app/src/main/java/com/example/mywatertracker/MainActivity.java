package com.example.mywatertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button drinkButton;
    private Intent waterTrackingServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drinkButton = findViewById(R.id.drink_button);

        // Start the service
        waterTrackingServiceIntent = new Intent(this, WaterTrackingService.class);
        startService(waterTrackingServiceIntent);

        drinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int waterAmount = 250; // Assuming the user drinks 250ml of water
                updateWaterLevel(waterAmount);
            }
        });
    }

    private void updateWaterLevel(int amount) {
        Intent intent = new Intent("com.example.watertracker.ADD_FLUID");
        intent.putExtra("waterAmount", amount);
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the service when the activity is destroyed
        stopService(waterTrackingServiceIntent);
    }
}