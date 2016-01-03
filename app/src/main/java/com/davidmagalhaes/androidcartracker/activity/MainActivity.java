package com.davidmagalhaes.androidcartracker.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.davidmagalhaes.androidcartracker.alarm.GpsSenderAlarm;
import com.davidmagalhaes.androidcartracker.alarm.MotionSensorAlarm;
import com.davidmagalhaes.androidcartracker.R;

import java.util.Calendar;

public class MainActivity extends Activity {
    public static final String GPS_TRACKER_TOKEN = "gpstracker";

    protected PowerManager.WakeLock mWakeLock;

    final MainActivity activity = this;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_main);

        // Backlight
        Boolean backlight = sharedPreferences.getBoolean("backlight", false);
        toggleBackLight(backlight);
        if (backlight) {
            Toast.makeText(getApplicationContext(), "Backlight always ON!", Toast.LENGTH_SHORT).show();
        }

        // Battery Saving
        Boolean batterySaver = sharedPreferences.getBoolean("batterySaver", false);
        setAirMode(activity, batterySaver);
        if (batterySaver) {
            Toast.makeText(getApplicationContext(), "Battery Saver ON!", Toast.LENGTH_SHORT).show();
        }

        // Alarm
        final AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intentGps = new Intent(this, GpsSenderAlarm.class);
        Intent intentMotionSensor = new Intent(this, MotionSensorAlarm.class);

        final PendingIntent pendingIntentGps = PendingIntent.getBroadcast(activity, 0, intentGps,
                PendingIntent.FLAG_CANCEL_CURRENT);
        final PendingIntent pintentIntentMotionSensor = PendingIntent.getBroadcast(activity, 0,
                intentMotionSensor, PendingIntent.FLAG_CANCEL_CURRENT);

        // Enable Disable
        ToggleButton tBtnService = (ToggleButton) findViewById(R.id.tBtnService);
        tBtnService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Calendar cal = Calendar.getInstance();

                    Integer gpsRepeating = Integer.valueOf(
                            sharedPreferences.getString("gpsStaticReport",
                                    GpsSenderAlarm.ALARM_LOOP.toString()));

                    Integer motionRepeating = Integer.valueOf(
                            sharedPreferences.getString("motionCheckInterval",
                                    MotionSensorAlarm.ALARM_LOOP.toString()));

                    alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                            gpsRepeating*60*1000, pendingIntentGps);

                    alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                            motionRepeating*60*1000, pintentIntentMotionSensor);

                    Log.i("Tracker Toggle", "ON");
                } else {
                    alarm.cancel(pendingIntentGps);
                    alarm.cancel(pintentIntentMotionSensor);

                    Log.i("Tracker Toggle", "OFF");
                }
            }
        });

        Button button = (Button) findViewById(R.id.settingsBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Backlight
        Boolean backlight = sharedPreferences.getBoolean("backlight", false);
        toggleBackLight(backlight);
        if (backlight) {
            Toast.makeText(getApplicationContext(), "Backlight always ON!", Toast.LENGTH_SHORT).show();
        }

        // Battery Saving
        Boolean batterySaver = sharedPreferences.getBoolean("batterySaver", false);
        setAirMode(activity, batterySaver);
        if (batterySaver) {
            Toast.makeText(getApplicationContext(), "Battery Saver ON!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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

    /**
     * Put phone on Air Plane mode
     *
     * @param context
     * @param isEnabled
     */
    private void setAirMode(Context context, boolean isEnabled) {
        // Toggle airplane mode.
        Settings.System.putInt(
                context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, isEnabled ? 1 : 0);

        // Post an intent to reload.
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", !isEnabled);
        sendBroadcast(intent);
    }

    /**
     * Toggle screen backlight
     *
     * @param value
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
}
