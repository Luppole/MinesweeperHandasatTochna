package com.example.minesweeperhandasattochna;

public class Friend {
    private String name;
    private int points;
    private String profilePicture;
    private String email;

    public Friend(String name, int points, String profilePicture, String email) {
        this.name = name;
        this.points = points;
        this.profilePicture = profilePicture;
        this.email = email;
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

    public String getEmail() {
        return email;
    }
}
