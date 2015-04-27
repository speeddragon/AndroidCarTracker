package com.example.david.androidtest.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.david.androidtest.alarm.GpsSenderAlarm;
import com.example.david.androidtest.alarm.MotionSensorAlarm;
import com.example.david.androidtest.R;

import java.util.Calendar;

public class MainActivity extends ActionBarActivity {
    public static final String GPS_TRACKER_TOKEN = "gpstracker";

    protected PowerManager.WakeLock mWakeLock;

    SharedPreferences sharedPreferences;
    MainActivityReceiver mainActivityReceiver;

    /**
     * Toggle screen backlight
     *
     * @param value True to enable back light
     */
    public void toggleBackLight(Boolean value) {
        if (value) {
            /* This code together with the one in onDestroy()
             * will make the screen be always on until this Activity gets destroyed. */
            final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
            this.mWakeLock.acquire();
        } else {
            if (mWakeLock != null) {
                this.mWakeLock.release();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivityReceiver = new MainActivityReceiver();
        sharedPreferences = getSharedPreferences(MainActivity.GPS_TRACKER_TOKEN, MODE_PRIVATE);

        final Activity mainActivity = this;
        setContentView(R.layout.activity_main);

        // Disable back light always on
        toggleBackLight(false);

        // Back light
        ToggleButton toggle = (ToggleButton) findViewById(R.id.tBtnNetwork);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleBackLight(isChecked);
            }
        });

        // Alarm
        final AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent gpsSenderIntent = new Intent(this, GpsSenderAlarm.class);
        Intent motionSensorAlarmIntent = new Intent(this, MotionSensorAlarm.class);

        final PendingIntent gpsSenderPendingIntent = PendingIntent.getBroadcast(mainActivity, 0,
                gpsSenderIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        final PendingIntent motionSensorAlarmPendingIntent = PendingIntent.getBroadcast(mainActivity, 0,
                motionSensorAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Tracker
        ToggleButton tBtnService = (ToggleButton) findViewById(R.id.tBtnService);
        tBtnService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Calendar cal = Calendar.getInstance();

                    // Send GPS location every 30 minutes
                    // TODO: Change this to a Server Side Setting
                    alarm.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            cal.getTimeInMillis(),
                            GpsSenderAlarm.ALARM_LOOP*60*1000,
                            gpsSenderPendingIntent);

                    // Check for motion every 15 minutes
                    alarm.setRepeating(AlarmManager.RTC_WAKEUP,
                            cal.getTimeInMillis(),
                            MotionSensorAlarm.ALARM_LOOP*60*1000,
                            motionSensorAlarmPendingIntent);

                    Log.i("Tracker Toggle", "ON");
                } else {
                    alarm.cancel(gpsSenderPendingIntent);
                    alarm.cancel(motionSensorAlarmPendingIntent);

                    Log.i("Tracker Toggle", "OFF");
                }
            }
        });

        // Battery Save Mode
        ToggleButton tBtnBattery = (ToggleButton) findViewById(R.id.tBtnBattery);
        tBtnBattery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setAirMode(mainActivity, isChecked);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(mainActivityReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onPause() {

        super.onPause();

        this.unregisterReceiver(mainActivityReceiver);
    }

    @Override
    public void onDestroy() {
        toggleBackLight(false);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setAirMode(Context context, boolean isEnabled) {
        // Toggle airplane mode.
        Settings.System.putInt(
                context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, isEnabled ? 1 : 0);

        // Post an intent to reload.
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", !isEnabled);
        sendBroadcast(intent);
    }

    private final class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            TextView accText = (TextView) findViewById(R.id.accText);
            accText.setText(intent.getStringExtra("accelerometer"));
        }
    }
}
