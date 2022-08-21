package com.example.foxontherun.model;

public class DistanceDTO {

    private Integer gameState;
    private Double distance;

    public DistanceDTO(Integer gameState, Double distance) {
        this.gameState = gameState;
        this.distance = distance;
    }

    public Integer getGameState() {
        return gameState;
    }

    public void setGameState(Integer gameState) {
        this.gameState = gameState;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
