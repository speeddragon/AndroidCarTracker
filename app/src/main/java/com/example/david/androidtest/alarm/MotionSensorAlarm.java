package com.example.david.androidtest.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.example.david.androidtest.listener.AccelerometerListener;

public class MotionSensorAlarm extends BroadcastReceiver
{
    // Number of minutes that app will keep tracking on after activation by the motion sensor.
    public final static Integer ALARM_END = 5;

    // Number of minutes this will check for motion sensor.
    public final static Integer ALARM_LOOP = 15;

    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        SensorManager mSensorManager;
        Sensor mAccelerometer;

        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");

        // WakeLock is used to access accelerometer readings when screen is off
        wakeLock.acquire();

        Toast.makeText(context, "MotionSensorAlarm - Started!", Toast.LENGTH_LONG).show();
        Log.i("Sensor", "ENABLE");

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SensorEventListener accelerometerListener = new AccelerometerListener(
                context,
                intent,
                wakeLock
        );

        mSensorManager.registerListener(accelerometerListener,
                mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void SetAlarm(Context context)
    {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
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
