package com.davidmagalhaes.androidcartracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by David on 22/06/14.
 */
public class GpsSenderAlarm extends BroadcastReceiver {

    public final static Integer ALARM_LOOP = 30;

    PowerManager pm;
    PowerManager.WakeLock wl;

    Context alarmContext;
    SharedPreferences sharedPreferences;

    private LocationListener gpsListener = new LocationListener() {

        Integer countGpsSent = 0;

        @Override
        public void onLocationChanged(Location location) {
            String serverURL = sharedPreferences.getString("serverURL", null);

            if (serverURL != null) {
                serverURL = serverURL.concat(!serverURL.substring(serverURL.length() - 1)
                        .equals("/") ? "/" : "").concat("api.php");

                // Send to server
                LocationLogTask locationLogTask = new LocationLogTask();
                locationLogTask.execute(
                        serverURL,
                        "1",
                        String.valueOf(location.getLatitude()),
                        String.valueOf(location.getLongitude()));

                countGpsSent++;
            }

            locationManager.removeUpdates(this);
            wl.release();
        }

        @Override
        public void onProviderDisabled(String provider) {
            //Toast.makeText(BroadcastReceiver.this, "GPS turned OFF", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            //Toast.makeText(context, "Gps turned ON", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }
    };

    // GPS
    private LocationManager locationManager;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        Toast.makeText(context, "GPS Sender MotionSensorAlarm", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                5000,   // 3 sec
                10,
                gpsListener);
    }

    public void SetAlarm(Context context)
    {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, GpsSenderAlarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                1000 * 60 * GpsSenderAlarm.ALARM_LOOP, pi);

        Toast.makeText(context, "Set up GPS Sender Alarm!", Toast.LENGTH_LONG).show();
    }

    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, GpsSenderAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
