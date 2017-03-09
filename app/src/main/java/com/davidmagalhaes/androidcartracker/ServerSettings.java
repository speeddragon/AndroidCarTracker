package com.davidmagalhaes.androidcartracker;

public class ServerSettings {
    private Integer serverPingTime;
    private Boolean serverAlert;

    public Integer getServerPingTime() {
        return serverPingTime;
    }

    public void setServerPingTime(Integer serverPingTime) {
        this.serverPingTime = serverPingTime;
    }

    public Boolean getServerAlert() {
        return serverAlert;
    }

    public void setServerAlert(Boolean serverAlert) {
        this.serverAlert = serverAlert;
    }
}
