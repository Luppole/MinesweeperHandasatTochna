package com.example.minesweeperhandasattochna;

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
    private DatabaseReference leaderboardRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Initialize UI components
        difficultySpinner = findViewById(R.id.difficultySpinner);
        leaderboardList = findViewById(R.id.leaderboardList);
        ImageButton backButton = findViewById(R.id.backButton);

        // Initialize Firebase Database references
        leaderboardRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("leaderboard");
        usersRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users");

        // Set up the difficulty spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"easy", "medium", "hard"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);

        // Set load leaderboard button listener
        findViewById(R.id.loadLeaderboardButton).setOnClickListener(v -> loadLeaderboard());

        // Set back button listen
        backButton.setOnClickListener(v -> onBackPressed());
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

                        // Fetch nicknames and update leaderboard
                        fetchNicknamesAndUpdate(leaderboardData);
                    } else {
                        Toast.makeText(this, "No leaderboard data found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load leaderboard: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchNicknamesAndUpdate(List<LeaderboardEntry> leaderboardData) {
        List<LeaderboardEntry> updatedData = new ArrayList<>();

        for (LeaderboardEntry entry : leaderboardData) {
            String email = entry.getDisplayName(); // Initially, displayName stores the email.
            String sanitizedEmail = email.replace(".", ",");

            usersRef.child(sanitizedEmail).get().addOnSuccessListener(snapshot -> {
                String nickname = snapshot.child("nickname").getValue(String.class);
                String displayName = (nickname != null && !nickname.isEmpty()) ? nickname : email;

                updatedData.add(new LeaderboardEntry(displayName, entry.getTime()));

                // Check if all entries are updated and refresh the UI
                if (updatedData.size() == leaderboardData.size()) {
                    Collections.sort(updatedData, Comparator.comparingInt(LeaderboardEntry::getTime));
                    LeaderboardAdapter adapter = new LeaderboardAdapter(this, updatedData);
                    leaderboardList.setAdapter(adapter);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load nickname for " + email, Toast.LENGTH_SHORT).show();
            });
        }
    }

    public static class LeaderboardEntry {
        private final String displayName;
        private final int time;

        public LeaderboardEntry(String displayName, int time) {
            this.displayName = displayName;
            this.time = time;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getTime() {
            return time;
        }
    }
}
