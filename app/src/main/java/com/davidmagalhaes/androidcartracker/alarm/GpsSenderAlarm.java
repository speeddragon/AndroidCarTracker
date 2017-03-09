package com.davidmagalhaes.androidcartracker.alarm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

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
                Toast.makeText(alarmContext, "Sending to Server ...", Toast.LENGTH_SHORT).show();

                serverURL = serverURL.concat(!serverURL.substring(serverURL.length() - 1)
                        .equals("/") ? "/" : "").concat("api.php");

                // Refactor, put in a utility class
                final RequestQueue queue = Volley.newRequestQueue(GpsSenderAlarm.this.alarmContext);

                JSONObject parameters = new JSONObject();
                try {
                    parameters.put("carId", "1");
                    parameters.put("latitude", String.valueOf(location.getLatitude()));
                    parameters.put("longitude", String.valueOf(location.getLongitude()));

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.POST,
                            serverURL,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) { }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(alarmContext, "Erro inesperado!", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );

                    queue.add(jsonObjectRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                countGpsSent++;
            }

            locationManager.removeUpdates(this);
            wl.release();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(alarmContext, "GPS turned OFF", Toast.LENGTH_LONG).show();
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

    // GPS
    private LocationManager locationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        alarmContext = context;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        Toast.makeText(context, "Starting GPS ...", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                5000,   // 3 sec
                10,
                gpsListener);
    }

    public void SetAlarm(Context context)
    {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, GpsSenderAlarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        Integer gpsRepeating = Integer.valueOf(
                sharedPreferences.getString("gpsStaticReport",
                        GpsSenderAlarm.ALARM_LOOP.toString()));

        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                1000 * 60 * gpsRepeating, pi);

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
