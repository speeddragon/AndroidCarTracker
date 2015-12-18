package com.davidmagalhaes.androidcartracker;

/**
 * On boot of smartphone it will enable the alarms.
 * Created by David on 22/06/14.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class AutoStart extends BroadcastReceiver
{
    MotionSensorAlarm motionSensorAlarm = new MotionSensorAlarm();
    GpsSenderAlarm gpsSenderAlarm = new GpsSenderAlarm();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        // TODO: This needs checking
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                "com.davidmagalhaes.androidcartracker", Context.MODE_PRIVATE);

        Boolean startOnBoot = sharedPreferences.getBoolean("start_on_boot", false);

        if (startOnBoot && intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            //context.getSharedPreferences();
            motionSensorAlarm.SetAlarm(context);
            gpsSenderAlarm.SetAlarm(context);
        }
    }
}