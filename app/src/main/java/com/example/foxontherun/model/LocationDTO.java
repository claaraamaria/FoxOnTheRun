package com.example.foxontherun.model;

public class LocationDTO {

    private String name;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Float phoneAzimuth;

    public LocationDTO(String name) {
        this.name = name;
    }

    public LocationDTO(String name, Double latitude, Double longitude, Double altitude, Float phoneAzimuth) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.phoneAzimuth = phoneAzimuth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Float getPhoneAzimuth() {
        return phoneAzimuth;
    }

    public void setPhoneAzimuth(Float phoneAzimuth) {
        this.phoneAzimuth = phoneAzimuth;
    }
}
