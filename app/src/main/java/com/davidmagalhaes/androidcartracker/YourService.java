package com.davidmagalhaes.androidcartracker;

/**
 * Created by David on 22/06/14.
 */
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class YourService extends Service
{
    MotionSensorAlarm motionSensorAlarm = new MotionSensorAlarm();
    @Override
    public void onCreate() {
        // code to execute when the service is first created
        super.onCreate();

        Toast.makeText(this, "GPS Tracker created!", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        motionSensorAlarm.SetAlarm(YourService.this);
        return START_STICKY;
    }



    public void onStart(Context context,Intent intent, int startId)
    {
        motionSensorAlarm.SetAlarm(context);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "GPS Tracker stopped!", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
