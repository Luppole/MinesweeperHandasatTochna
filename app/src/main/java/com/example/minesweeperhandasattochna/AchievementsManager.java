package com.example.minesweeperhandasattochna;

import java.util.HashMap;

public class AchievementsManager {
    public static HashMap<String, Achievement> getAchievements() {
        HashMap<String, Achievement> achievements = new HashMap<>();

        // Add predefined achievements
        achievements.put("play_100_games", new Achievement("Play 100 Games", "Play 100 games to complete this achievement", 100));
        achievements.put("win_50_games", new Achievement("Win 50 Games", "Win 50 games to complete this achievement", 50));
        achievements.put("win_streak_10", new Achievement("10 Game Win Streak", "Achieve a 10-game win streak", 10));

        return achievements;
    }
}
