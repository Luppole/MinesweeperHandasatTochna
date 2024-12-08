package com.example.minesweeperhansasattochna;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {

    private Spinner difficultySpinner;
    private ListView leaderboardList;
    private DatabaseReference leaderboardRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        difficultySpinner = findViewById(R.id.difficultySpinner);
        leaderboardList = findViewById(R.id.leaderboardList);

        leaderboardRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("leaderboard");

        // Populate Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"easy", "medium", "hard"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);

        findViewById(R.id.loadLeaderboardButton).setOnClickListener(v -> loadLeaderboard());
    }

    private void loadLeaderboard() {
        String selectedDifficulty = difficultySpinner.getSelectedItem().toString();

        leaderboardRef.child(selectedDifficulty).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        ArrayList<String> leaderboardData = new ArrayList<>();
                        for (DataSnapshot entry : snapshot.getChildren()) {
                            Map<String, Object> data = (Map<String, Object>) entry.getValue();
                            if (data != null) {
                                String email = (String) data.get("email");
                                Integer time = data.get("time") != null ? ((Long) data.get("time")).intValue() : null;

                                if (email != null && time != null) {
                                    leaderboardData.add(email + " - " + time + "s");
                                }
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, leaderboardData);
                        leaderboardList.setAdapter(adapter);
                    } else {
                        Toast.makeText(this, "No leaderboard data found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load leaderboard: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }



}
