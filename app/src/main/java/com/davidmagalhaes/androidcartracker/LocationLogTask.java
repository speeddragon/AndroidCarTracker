package com.davidmagalhaes.androidcartracker;

import android.os.AsyncTask;

/**
 * Created by David on 01/05/14.
 */
public class LocationLogTask extends AsyncTask<String, Integer, String> {

    @Override
    protected String doInBackground(String... strings) {
        try {
            WebClient.insertLocationLog(strings[0], strings[1], strings[2], strings[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        // None
    }

    @Override
    protected void onPostExecute(String result) {
        // None
    }
}
