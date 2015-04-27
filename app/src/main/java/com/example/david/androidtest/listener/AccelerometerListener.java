package com.example.david.androidtest.listener;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.PowerManager;
import android.util.Log;

public class AccelerometerListener implements SensorEventListener {
    private Float lastX, lastY, lastZ;
    private PowerManager.WakeLock wakeLock;
    private Context alarmContext;
    private long startTime;
    private SensorManager mSensorManager;
    private Intent intent;

    public AccelerometerListener(Context context, Intent intent, PowerManager.WakeLock wakeLock) {
        Log.i("AccelerometerListener", "Created!");

        alarmContext = context;
        startTime = System.currentTimeMillis();
        this.wakeLock = wakeLock;
        this.intent = intent;

        this.mSensorManager = (SensorManager) alarmContext.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        Float x = event.values[0];
        Float y = event.values[1];
        Float z = event.values[2];

        Log.i("Accelerometer",
                String.valueOf(x)
                .concat(",")
                .concat(String.valueOf(y))
                .concat(",")
                .concat(String.valueOf(z))
        );

        updateUI(String.valueOf(x)
                .concat(",")
                .concat(String.valueOf(y))
                .concat(",")
                .concat(String.valueOf(z)));

        if (lastX != null) {
            // Calculate inMotion
            Integer fX = Math.round(Math.abs(x - lastX));
            Integer fY = Math.round(Math.abs(y - lastY));
            Integer fZ = Math.round(Math.abs(z - lastZ));

            Integer motion = fX + fY + fZ;

            // For some reason, even still, some of the number can flip a number,
            // to the reason to use 2 instead of 1.
            if (motion >= 2) {

                GpsListener gpsListener = new GpsListener(wakeLock);

                // Disable motion sensor check
                mSensorManager.unregisterListener(this);

                // GPS Setup
                Log.i("GPS", "ENABLED");
                LocationManager locationManager = (LocationManager) alarmContext
                        .getSystemService(Context.LOCATION_SERVICE);

                // Send location every 10 seconds
                // Note: 0m is used to reduce power consumption
                // https://developer.android.com/reference/android/location/LocationManager.html#requestLocationUpdates(long, float, android.location.Criteria, android.app.PendingIntent)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        10000, 10, gpsListener);

                //wakeLock.release();
            }
        }

        // Update last
        lastX = x;
        lastY = y;
        lastZ = z;

        long endTime = System.currentTimeMillis();

        // Disable check of accelerometer after 60 seconds
        if (endTime - startTime > 60 * 1000) {
            // Stop using accelerometer information
            Log.i("Sensor", "DISABLED");

            mSensorManager.unregisterListener(this);
            wakeLock.release();
        }
    }

    public void updateUI(String value) {
        intent.putExtra("accelerometer", value);
        //alarmContext.sendBroadcast(intent, "xxx");
    }
}
