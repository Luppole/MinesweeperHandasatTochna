package com.example.minesweeperhansasattochna;

public class Achievement {
    private final String title;
    private final String description;
    private final int goal;

    public Achievement(String title, String description, int goal) {
        this.title = title;
        this.description = description;
        this.goal = goal;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getGoal() {
        return goal;
    }
}
