package com.example.minesweeperhandasattochna;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AchievementsActivity extends AppCompatActivity {

    private RecyclerView achievementsRecyclerView;
    private AchievementsAdapter adapter;
    private HashMap<String, Achievement> allAchievements;
    private HashMap<String, Integer> userProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        achievementsRecyclerView = findViewById(R.id.achievementsRecyclerView);
        achievementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        allAchievements = AchievementsManager.getAchievements();
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Navigate back to the previous screen
            finish(); // This will close the current activity and return to the previous one
        });

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (userEmail != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("users")
                    .child(userEmail.replace(".", ","))
                    .child("stats");

            fetchUserProgress(userRef);
        } else {
            Toast.makeText(this, "Please log in to view achievements.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUserProgress(DatabaseReference userRef) {
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                userProgress = new HashMap<>();

                // Fetch stats for progress calculation
                int totalGames = snapshot.child("totalGames").exists() ? snapshot.child("totalGames").getValue(Integer.class) : 0;
                int totalWins = snapshot.child("totalWins").exists() ? snapshot.child("totalWins").getValue(Integer.class) : 0;
                int winStreak = snapshot.child("winStreak").exists() ? snapshot.child("winStreak").getValue(Integer.class) : 0;

                // Map stats to achievements
                userProgress.put("play_100_games", totalGames);
                userProgress.put("win_50_games", totalWins);
                userProgress.put("win_streak_10", winStreak);

                // Bind data to RecyclerView
                adapter = new AchievementsAdapter(allAchievements, userProgress);
                achievementsRecyclerView.setAdapter(adapter);
            } else {
                Toast.makeText(this, "No statistics found for achievements.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load achievements.", Toast.LENGTH_SHORT).show();
        });
    }
}
