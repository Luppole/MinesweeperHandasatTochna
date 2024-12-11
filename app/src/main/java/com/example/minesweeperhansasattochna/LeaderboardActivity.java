package com.example.minesweeperhansasattochna;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {

    private Spinner difficultySpinner;
    private ListView leaderboardList;
    private DatabaseReference leaderboardRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Initialize UI components
        difficultySpinner = findViewById(R.id.difficultySpinner);
        leaderboardList = findViewById(R.id.leaderboardList);
        ImageButton homeButton = findViewById(R.id.homeButton); // Correctly initialized after setContentView

        // Initialize Firebase Database reference
        leaderboardRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("leaderboard");

        // Set up the difficulty spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"easy", "medium", "hard"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);

        // Set load leaderboard button listener
        findViewById(R.id.loadLeaderboardButton).setOnClickListener(v -> loadLeaderboard());

        // Set home button click listener to navigate back to main activity
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(LeaderboardActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Optional to avoid stacking activities
        });
    }

    private void loadLeaderboard() {
        String selectedDifficulty = difficultySpinner.getSelectedItem().toString();

        leaderboardRef.child(selectedDifficulty).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        List<LeaderboardEntry> leaderboardData = new ArrayList<>();

                        for (DataSnapshot entry : snapshot.getChildren()) {
                            Map<String, Object> data = (Map<String, Object>) entry.getValue();
                            if (data != null) {
                                String email = (String) data.get("email");
                                Long timeLong = (Long) data.get("time");
                                Integer time = timeLong != null ? timeLong.intValue() : null;

                                if (email != null && time != null) {
                                    leaderboardData.add(new LeaderboardEntry(email, time));
                                }
                            }
                        }

                        // Sort leaderboard by time (ascending)
                        Collections.sort(leaderboardData, Comparator.comparingInt(LeaderboardEntry::getTime));
                        LeaderboardAdapter adapter = new LeaderboardAdapter(this, leaderboardData);
                        leaderboardList.setAdapter(adapter);
                    } else {
                        Toast.makeText(this, "No leaderboard data found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load leaderboard: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public static class LeaderboardEntry {
        private final String email;
        private final int time;

        public LeaderboardEntry(String email, int time) {
            this.email = email;
            this.time = time;
        }

        public String getEmail() {
            return email;
        }

        public int getTime() {
            return time;
        }
    }
}
