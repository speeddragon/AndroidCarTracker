package com.example.david.androidtest.listener;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.david.androidtest.LocationLogTask;

import java.math.BigDecimal;

/**
 * Created by David on 25/04/15.
 */
public class GpsListener implements LocationListener {

    //private long startTime;
    Context alarmContext;
    SensorManager mSensorManager;
    AccelerometerListener accelerometerListener;
    private PowerManager.WakeLock wakeLock;

    double previousLatitude, previousLongitude;
    int gpsMotionCount = 0; // Increase if the gps location is stop.

    public GpsListener(PowerManager.WakeLock wakeLock) {
        Log.i("GpsListener", "Created!");
        //this.startTime = System.currentTimeMillis();
        this.wakeLock = wakeLock;
    }

    @Override
    public void onLocationChanged(Location location) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(alarmContext);

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        Log.i("GPS:Coords", String.valueOf(currentLatitude).concat(" - ")
                .concat(String.valueOf(currentLongitude)));

        String serverURL = sharedPreferences.getString("serverURL", null);

        serverURL = serverURL.concat(!serverURL.substring(serverURL.length() - 1)
                .equals("/") ? "/" : "").concat("api.php");

        // Send to server
        LocationLogTask locationLogTask = new LocationLogTask();
        locationLogTask.execute(
                serverURL,
                String.valueOf(sharedPreferences.getInt("carID", 1)),
                String.valueOf(currentLatitude),
                String.valueOf(currentLongitude));

        // Check time from the beginning
        /*endTime = System.currentTimeMillis();

        // X minutes tracking
        if (endTime - startTime > MotionSensorAlarm.ALARM_END * 60 * 1000 ) {
            LocationManager locationManager = (LocationManager) alarmContext
                    .getSystemService(Context.LOCATION_SERVICE);

            locationManager.removeUpdates(this);
            //wl.release();
        }*/

        // Check by motion
        if (!isInMotion(currentLatitude, currentLongitude)) {
            LocationManager locationManager = (LocationManager) alarmContext
                    .getSystemService(Context.LOCATION_SERVICE);

            locationManager.removeUpdates(this);

            gpsMotionCount = 0;
            wakeLock.release();
        }

        previousLatitude = currentLatitude;
        previousLongitude = currentLongitude;
    }

    public Boolean isInMotion(double currentLatitude, double currentLongitude) {
        Double latitude = getValueWithPrecision(currentLatitude, 3);
        Double longitude = getValueWithPrecision(currentLongitude, 3);

        Double lastLatitude = getValueWithPrecision(previousLatitude, 3);
        Double lastLongitude = getValueWithPrecision(previousLongitude, 3);

        Log.i("GPS Coords", String.valueOf(latitude).concat(" - ").concat(String.valueOf(longitude)));

        if (lastLatitude.equals(latitude) && lastLongitude.equals(longitude)) {
            gpsMotionCount++;
        }

        return gpsMotionCount < 30;
    }

    public Double getValueWithPrecision(Double value, Integer precision) {
        return new BigDecimal(value)
                .setScale(precision, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(alarmContext, "GPS turned OFF", Toast.LENGTH_LONG).show();
        mSensorManager.unregisterListener(accelerometerListener);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(alarmContext, "Gps turned ON", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }
}
