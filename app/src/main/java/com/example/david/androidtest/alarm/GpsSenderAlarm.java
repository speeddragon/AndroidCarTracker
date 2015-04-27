package com.example.david.androidtest.alarm;

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

import com.example.david.androidtest.LocationLogTask;
import com.example.david.androidtest.listener.GpsListener;

public class GpsSenderAlarm extends BroadcastReceiver {

    public final static Integer ALARM_LOOP = 30; // Minutes

    PowerManager pm;
    PowerManager.WakeLock wl;

    Context alarmContext;
    SharedPreferences sharedPreferences;

    // GPS
    private LocationManager locationManager;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        GpsListener gpsListener = new GpsListener(wl);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        Toast.makeText(context, "GPS Sender MotionSensorAlarm", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                10000,
                0,
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
