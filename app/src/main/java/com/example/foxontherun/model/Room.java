package com.example.foxontherun.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Room {
    @SerializedName("roomName")
    @Expose
    private String roomName;
    @SerializedName("latitude")
    @Expose
    private Double latitude;
    @SerializedName("longitude")
    @Expose
    private Double longitude;
    @SerializedName("altitude")
    @Expose
    private Double altitude;
    @SerializedName("currentNoOfPlayers")
    @Expose
    private Integer currentNoOfPlayers;
    @SerializedName("maxNoOfPlayers")
    @Expose
    private Integer maxNoOfPlayers;
    @SerializedName("players")
    @Expose
    private List<Player> players;

    public Room() {
    }

    public Room(
            String roomName, Double latitude, Double longitude, Double altitude,
            Integer currentNoOfPlayers, Integer maxNoOfPlayers, List<Player> players)
    {
        this.roomName = roomName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.currentNoOfPlayers = currentNoOfPlayers;
        this.maxNoOfPlayers = maxNoOfPlayers;
        this.players = players;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
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

    public Integer getCurrentNoOfPlayers() {
        return currentNoOfPlayers;
    }

    public void setCurrentNoOfPlayers(Integer currentNoOfPlayers) {
        this.currentNoOfPlayers = currentNoOfPlayers;
    }

    public Integer getMaxNoOfPlayers() {
        return maxNoOfPlayers;
    }

    public void setMaxNoOfPlayers(Integer maxNoOfPlayers) {
        this.maxNoOfPlayers = maxNoOfPlayers;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return roomName.equals(room.roomName);
    }
}