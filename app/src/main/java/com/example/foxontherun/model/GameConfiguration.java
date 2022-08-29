package com.example.foxontherun.model;

public class GameConfiguration {

    private static Long waitLobbyTimer;
    private static Long hideTimer;
    private static Long gameOnTimer;

    public static Long getWaitLobbyTimer() {
        return waitLobbyTimer;
    }

    public static void setWaitLobbyTimer(Long waitLobbyTimer) {
        GameConfiguration.waitLobbyTimer = waitLobbyTimer;
    }

    public static Long getHideTimer() {
        return hideTimer;
    }

    public static void setHideTimer(Long hideTimer) {
        GameConfiguration.hideTimer = hideTimer;
    }

    public static Long getGameOnTimer() {
        return gameOnTimer;
    }

    public static void setGameOnTimer(Long gameOnTimer) {
        GameConfiguration.gameOnTimer = gameOnTimer;
    }
}
