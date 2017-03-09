package com.davidmagalhaes.androidcartracker.alarm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MotionSensorAlarm extends BroadcastReceiver {
    public final static Integer ALARM_END = 5;
    public final static Integer ALARM_LOOP = 15;

    PowerManager pm;
    PowerManager.WakeLock wl;
    Context alarmContext;

    SharedPreferences sharedPreferences;
    long startTime, endTime;

    private LocationListener gpsListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            initializeSharedPreferences(alarmContext);

            // Refactor, put in a utility class
            final RequestQueue queue = Volley.newRequestQueue(MotionSensorAlarm.this.alarmContext);

            String serverURL = sharedPreferences.getString("serverURL", null);

            serverURL = serverURL.concat(!serverURL.substring(serverURL.length() - 1)
                    .equals("/") ? "/" : "").concat("api.php");

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
                            public void onResponse(JSONObject response) {
                            }
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

            endTime = System.currentTimeMillis();

            Integer gpsMotionReport = Integer.valueOf(
                    sharedPreferences.getString("gpsMotionReport",
                            MotionSensorAlarm.ALARM_END.toString()));

            // X minutes tracking
            if ((endTime - startTime > gpsMotionReport * 60 * 1000) && wl.isHeld()) {
                wl.release();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(alarmContext, "GPS turned OFF", Toast.LENGTH_LONG).show();
            mSensorManager.unregisterListener(accelerometerListener);

            if (wl.isHeld()) {
                wl.release();
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(alarmContext, "GPS turned ON", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    private SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
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
                    Toast.makeText(alarmContext, "Motion ENGAGED!", Toast.LENGTH_SHORT).show();

                    // Disable motion
                    if (mSensorManager != null) {
                        mSensorManager.unregisterListener(this);
                    }

                    // GPS Setup
                    Log.i("GPS", "ENABLED");
                    locationManager = (LocationManager) alarmContext
                            .getSystemService(Context.LOCATION_SERVICE);

                    if (ActivityCompat.checkSelfPermission(MotionSensorAlarm.this.alarmContext,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MotionSensorAlarm.this.alarmContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

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
                            10000, 1, gpsListener);

                    if (wl != null && wl.isHeld()) {
                        wl.release();
                    }
                }
            }

            // Update last
            lastX = x;
            lastY = y;
            lastZ = z;

            endTime = System.currentTimeMillis();

            if ((endTime - startTime) > 60 * 1000) {
                // Unregister
                Toast.makeText(alarmContext, "Motion Sensor DISABLED!", Toast.LENGTH_SHORT).show();
                Log.i("Sensor", "DISABLED");

                if (mSensorManager != null) {
                    mSensorManager.unregisterListener(this);
                }

                if (wl != null && wl.isHeld()) {
                    wl.release();
                }
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
        Toast.makeText(context, "Motion Sensor - Started!", Toast.LENGTH_LONG).show(); // For example

        Log.i("Sensor", "ENABLE");

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(accelerometerListener,
                mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);

        startTime = System.currentTimeMillis();
    }

    public void setAlarm(Context context)
    {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, MotionSensorAlarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        initializeSharedPreferences(context);

        Integer motionRepeating = Integer.valueOf(
                sharedPreferences.getString("motionCheckInterval",
                        MotionSensorAlarm.ALARM_LOOP.toString()));

        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                1000 * 60 * motionRepeating, pi); // Millisec * Second * Minute

        Toast.makeText(context, "Set up Motion Sensor Alarm!", Toast.LENGTH_LONG).show(); // For example
    }

    /**
     * Ca
     * @param context
     */
    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, MotionSensorAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    /**
     * Initialize shared preferences
     *
     * @param context Context
     */
    public void initializeSharedPreferences(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }
}
