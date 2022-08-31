package com.example.foxontherun.model;

public class DistanceDTO {

    private Integer gameState;
    private Double distance;
    private Double angle;

    public DistanceDTO(Integer gameState, Double distance, Double angle) {
        this.gameState = gameState;
        this.distance = distance;
        this.angle = angle;
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

    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
        this.angle = angle;
    }
}
