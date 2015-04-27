package com.example.david.androidtest.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.example.david.androidtest.R;

public class SettingsActivity extends Activity {
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(MainActivity.GPS_TRACKER_TOKEN, MODE_PRIVATE);

        // Show saved settings

        // Settings: Server URL
        EditText serverURL = (EditText) findViewById(R.id.serverURL);
        serverURL.setText(sharedPreferences.getString("serverURL", ""));

        // Settings: Car ID
        NumberPicker np = (NumberPicker) findViewById(R.id.carIdPicker);
        np.setMinValue(1);
        np.setMaxValue(9);
        np.setValue(sharedPreferences.getInt("carID", 1));

        Button button = (Button) findViewById(R.id.btnSettingsSave);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                // Settings: Server Url
                EditText serverURL = (EditText) findViewById(R.id.serverURL);
                sharedPreferences.edit()
                        .putString("serverURL", serverURL.getText().toString())
                        .apply();

                // Settings: Car ID
                NumberPicker np = (NumberPicker) findViewById(R.id.carIdPicker);
                sharedPreferences.edit()
                        .putInt("carID", np.getValue())
                        .apply();

                // Show successfully saved settings
                CharSequence text = "Settings saved!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                toast.show();

                finish();
            }
        });
    }
}
