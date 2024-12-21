package com.example.minesweeperhandasattochna;

public class Friend {
    private String name;
    private int points;
    private String profilePicture; // Base64-encoded image or URL

    public Friend(String name, int points, String profilePicture) {
        this.name = name;
        this.points = points;
        this.profilePicture = profilePicture;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public String getProfilePicture() {
        return profilePicture;
    }
}
