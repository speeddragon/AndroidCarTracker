package com.davidmagalhaes.androidcartracker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.davidmagalhaes.david.androidcartracker.R;

/**
 * Created by David on 21/04/15.
 */
public class SettingsActivity extends Activity {
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(MainActivity.GPS_TRACKER_TOKEN, MODE_PRIVATE);

        Button button = (Button) findViewById(R.id.btnSettingsSave);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                EditText serverURL = (EditText) findViewById(R.id.serverURL);
                sharedPreferences.edit().putString("serverURL", serverURL.getText().toString()).apply();
            }
        });

        setContentView(R.layout.activity_settings);
    }
}
