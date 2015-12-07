package com.davidmagalhaes.androidcartracker;

/**
 * Created by David on 22/06/14.
 */
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class MotionSensorAlarm extends BroadcastReceiver
{
    final static Integer ALARM_END = 5;
    final static Integer ALARM_LOOP = 15;

    PowerManager pm;
    PowerManager.WakeLock wl;
    Context alarmContext;

    long startTime, endTime;

    private LocationListener gpsListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // Send to server
            LocationLogTask locationLogTask = new LocationLogTask();
            locationLogTask.execute(
                    "1",
                    String.valueOf(location.getLatitude()),
                    String.valueOf(location.getLongitude()));

            endTime = System.currentTimeMillis();

            // X minutes tracking
            if (endTime - startTime > MotionSensorAlarm.ALARM_END * 60 * 1000 ) {
                wl.release();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(alarmContext, "GPS turned OFF", Toast.LENGTH_LONG).show();
            mSensorManager.unregisterListener(accelerometerListener);
            wl.release();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(alarmContext, "Gps turned ON", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }
    };

    private SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event)
        {
            Float x = event.values[0];
            Float y = event.values[1];
            Float z = event.values[2];

            if (lastX != null) {
                // Calculate inMotion
                Integer fX = Math.round(Math.abs(x - lastX));
                Integer fY = Math.round(Math.abs(y - lastY));
                Integer fZ = Math.round(Math.abs(z - lastZ));

                Integer motion = fX + fY + fZ;

                if (motion >= 2) {
                    // Disable motion
                    mSensorManager.unregisterListener(this);

                    // GPS Setup
                    Log.i("GPS", "ENABLED");
                    locationManager = (LocationManager) alarmContext
                            .getSystemService(Context.LOCATION_SERVICE);

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            10000, 10, gpsListener);

                    wl.release();
                }
            }

            // Update last
            lastX = x;
            lastY = y;
            lastZ = z;

            endTime = System.currentTimeMillis();

            if (endTime - startTime > 60 * 1000) {
                // Unregister
                Log.i("Sensor", "DISABLED");
                mSensorManager.unregisterListener(this);
                wl.release();
            }
        }
    };

    // GPS
    private LocationManager locationManager;

    // Accelerometer
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    Float lastX;
    Float lastY;
    Float lastZ;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        alarmContext = context;

        // Put here YOUR code.
        Toast.makeText(context, "MotionSensorAlarm - Started!", Toast.LENGTH_LONG).show(); // For example

        Log.i("Sensor", "ENABLE");

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(accelerometerListener,
                mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);

        startTime = System.currentTimeMillis();
    }

    public void SetAlarm(Context context)
    {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, MotionSensorAlarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                1000 * 60 * MotionSensorAlarm.ALARM_LOOP, pi); // Millisec * Second * Minute

        Toast.makeText(context, "Set up Motion Sensor Alarm!", Toast.LENGTH_LONG).show(); // For example
    }

    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, MotionSensorAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
