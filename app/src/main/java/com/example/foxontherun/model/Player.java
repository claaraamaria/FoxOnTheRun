package com.example.foxontherun.model;

public class Player {

    private static String globalName;
    private static Boolean globalRole;
    private static String globalRoomName;
    private static Double latitude;
    private static Double longitude;
    private static Double altitude;


    public static String getGlobalName() {
        return globalName;
    }

    public static void setGlobalName(String globalName) {
        Player.globalName = globalName;
    }

    public static Boolean getGlobalRole() {
        return globalRole;
    }

    public static void setGlobalRole(Boolean globalRole) {
        Player.globalRole = globalRole;
    }

    public static String getGlobalRoomName() {
        return globalRoomName;
    }

    public static void setGlobalRoomName(String globalRoomName) {
        Player.globalRoomName = globalRoomName;
    }

    public static Double getLatitude() {
        return latitude;
    }

    public static void setLatitude(Double latitude) {
        Player.latitude = latitude;
    }

    public static Double getLongitude() {
        return longitude;
    }

    public static void setLongitude(Double longitude) {
        Player.longitude = longitude;
    }

    public static Double getAltitude() {
        return altitude;
    }

    public static void setAltitude(Double altitude) {
        Player.altitude = altitude;
    }

}
