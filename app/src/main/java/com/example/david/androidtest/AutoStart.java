package com.example.david.androidtest;

/**
 * On boot of smartphone it will enable the alarms.
 * Created by David on 22/06/14.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.david.androidtest.alarm.GpsSenderAlarm;
import com.example.david.androidtest.alarm.MotionSensorAlarm;

public class AutoStart extends BroadcastReceiver
{
    MotionSensorAlarm motionSensorAlarm = new MotionSensorAlarm();
    GpsSenderAlarm gpsSenderAlarm = new GpsSenderAlarm();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            // For auto start mobile phones
            motionSensorAlarm.SetAlarm(context);
            gpsSenderAlarm.SetAlarm(context);
        }
    }
}