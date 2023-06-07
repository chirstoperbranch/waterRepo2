package com.example.mywatertracker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class WaterTrackingService extends Service {

    private Button drinkButton;
    private Intent waterTrackingServiceIntent;
    private int waterLevel;
    private static final int NOTIFICATION_ID = 1;
    private static final String EXTRA_WATER_AMOUNT = "waterAmount";
    private static final String CHANNEL_ID = "water_tracker_channel";


    @Override
    public void onCreate(){
        startForegroundService();
        createNotification();
        startDecreasingWaterLevel();
    }

    public WaterTrackingService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private Notification createNotification() {
        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Water Tracker Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("My Water Tracker")
                .setContentText("Current Water Level: " + waterLevel + "ml")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Create a pending intent to launch the app when the notification is clicked
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        return builder.build();
    }


    private void startForegroundService() {
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);
        // Add your code here to start tracking the water level continuously
    }


    @SuppressLint("MissingPermission")
    private void updateWaterLevel(int amount) {
        waterLevel += amount;
        // Update the notification with the new water level
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, createNotification());
    }

    private Handler handler = new Handler();
    private Runnable decreaseWaterLevelRunnable = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            // Decrease the water level if it is more than 0
            if(waterLevel > 0){
                waterLevel -= 1; // Assuming 1ml is lost every 5 seconds (its not this much but it is an example)
            }

            //fix waterlevel if it ever goes below 0
            if(waterLevel < 0){
                waterLevel = 0;
            }

            // Update the notification with the new water level
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(WaterTrackingService.this);
            notificationManager.notify(NOTIFICATION_ID, createNotification());

            // Schedule the next execution after 5 seconds
            handler.postDelayed(this, 5000);
        }
    };

    // Call this method to start decreasing the water level
    private void startDecreasingWaterLevel() {
        handler.postDelayed(decreaseWaterLevelRunnable, 5000);
    }

    private BroadcastReceiver fluidAddedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int amount = intent.getIntExtra(EXTRA_WATER_AMOUNT, 0);
            updateWaterLevel(amount);
        }
    };

    // Register the receiver in the onStartCommand() method of the service
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(fluidAddedReceiver, new IntentFilter("com.example.watertracker.ADD_FLUID"));
        return super.onStartCommand(intent, flags, startId);
    }

    // Unregister the receiver in the onDestroy() method of the service
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(fluidAddedReceiver);
        handler.removeCallbacksAndMessages(null);
    }


}