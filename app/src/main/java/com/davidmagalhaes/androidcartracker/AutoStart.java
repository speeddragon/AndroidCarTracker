package com.davidmagalhaes.androidcartracker;

/**
 * On boot of smartphone it will enable the alarms.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.davidmagalhaes.androidcartracker.alarm.GpsSenderAlarm;
import com.davidmagalhaes.androidcartracker.alarm.MotionSensorAlarm;

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
            motionSensorAlarm.setAlarm(context);
            gpsSenderAlarm.SetAlarm(context);
        } else {
            motionSensorAlarm.cancelAlarm(context);
            gpsSenderAlarm.CancelAlarm(context);
        }
    }
}