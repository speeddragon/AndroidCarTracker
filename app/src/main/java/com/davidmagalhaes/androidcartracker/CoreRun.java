package com.davidmagalhaes.androidcartracker;

import android.content.SharedPreferences;
import android.util.Pair;

import java.util.TimerTask;

public class CoreRun extends TimerTask implements Runnable {

    SharedPreferences sharedPreferences;

    public static final Integer IN_MOTION_MAX = 5 * 60;
    public static final Integer CHECK_IN_MOTION_MAX = 60;
    public static final Integer WAIT_CHECK_IN_MOTION_MAX = 7 * 60;

    // Just for log in purpose
    private Boolean airMode = true;
    private Boolean gpsEnabled = false;

    private Boolean inMotion = false;
    private Integer inMotionSeconds = 0;

    private Boolean checkInMotion = true;
    private Integer checkInMotionSeconds = 0;

    private Integer serverPingTimeCount = 0;

    // Check GPS if car is stopped
    private Integer carStoppedSeconds = 0;

    public Boolean isCarStopped() {
        return (carStoppedSeconds > 5*60);
    }

    private Pair<String, String> gpsLatLong;

    private ServerSettings serverSettings;

    public Boolean getGpsEnabled() {
        return gpsEnabled;
    }

    public void setGpsEnabled(Boolean gpsEnabled) {
        this.gpsEnabled = gpsEnabled;
    }

    public Boolean getAirMode() {
        return airMode;
    }

    public void setAirMode(Boolean airMode) {
        this.airMode = airMode;
    }

    public Integer getInMotionSeconds() {
        return inMotionSeconds;
    }

    public void setInMotionSeconds(Integer inMotionSeconds) {
        this.inMotionSeconds = inMotionSeconds;
    }



    public Boolean getInMotion() {
        return inMotion;
    }

    public void setInMotion(Boolean inMotion) {
        this.inMotion = inMotion;
    }

    public Boolean getCheckInMotion() {
        return checkInMotion;
    }

    public void setCheckInMotion(Boolean checkInMotion) {
        this.checkInMotion = checkInMotion;
    }

    public Integer getCheckInMotionSeconds() {
        return checkInMotionSeconds;
    }

    public void setCheckInMotionSeconds(Integer checkInMotionSeconds) {
        this.checkInMotionSeconds = checkInMotionSeconds;
    }

    public ServerSettings getServerSettings() {
        return serverSettings;
    }

    public void setServerSettings(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
    }

    /* -------------------------------------------------------------------------------------- */

    @Override
    public void run() {
        // Get Server Settings
        if (this.serverSettings == null || serverSettings.getServerPingTime() == null) {
            // Make sure internet is on
            setAirMode(false);

            serverSettings = WebClient.getServerSettings(
                    sharedPreferences.getString("serverURL", null));

            return;
        }

        // Server Ping
        serverPingTimeCount++;

        if (serverPingTimeCount > serverSettings.getServerPingTime() * 60 ||
                this.serverSettings.getServerAlert() && serverPingTimeCount > 5 * 60) {

            // On HIGH ALERT MODE get server settings every 5 minutes

            // Enable Internet
            setAirMode(false);

            // Get Server settings
            //sharedPreferences = PreferenceManager.getDefaultSharedPreferences();

            serverSettings = WebClient.getServerSettings(
                    sharedPreferences.getString("serverURL", null));

            serverPingTimeCount = 0;
        }

        // Server on HIGH ALERT MODE
        if (this.serverSettings.getServerAlert()) {
            // Enable Internet
            setAirMode(false);

            // Enable GPS
            setGpsEnabled(true);

            return;
        }

        if (inMotion) {
            inMotionSeconds--;

            if (inMotionSeconds <= 0) {
                // Check again for motion
                inMotion = false;
                setCheckInMotion(true);
                checkInMotionSeconds = CoreRun.CHECK_IN_MOTION_MAX;
            }
        } else {
            // This will trigger the check for inMotion
            checkInMotionSeconds--;

            if (!checkInMotion) {
                if (checkInMotionSeconds <= 0) {
                    // Check if the car is in motion for 60 seconds
                    checkInMotionSeconds = CoreRun.CHECK_IN_MOTION_MAX;

                    setCheckInMotion(true);
                }
            } else {
                if (checkInMotionSeconds <= 0) {
                    // Wait for the next in motion check in 7 minutes
                    checkInMotionSeconds = CoreRun.WAIT_CHECK_IN_MOTION_MAX;

                    setCheckInMotion(false);
                    setGpsEnabled(false);
                    setAirMode(true);
                }
            }
        }
    }

    public void updateLocation(Double latitude, Double longitude) {
        if (this.gpsLatLong == null) {
            this.gpsLatLong = new Pair<String, String>(String.format("%.6f", latitude),
                    String.format("%.6f", longitude));
        } else {
            if (this.gpsLatLong.first.equals(String.format("%.6f", latitude)) &&
                    this.gpsLatLong.second.equals(String.format("%.6f", longitude))) {
                carStoppedSeconds += 3;
            } else {
                carStoppedSeconds = 0;
            }
        }
    }


}
