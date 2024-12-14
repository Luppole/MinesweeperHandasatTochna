package com.example.minesweeperhandasattochna;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatsActivity extends AppCompatActivity {

    private TextView gamesPlayedView, percentageWonView, totalWinsView, totalLossesView, averageTimeView, winStreakView;
    private DatabaseReference userStatsRef;
    private int gamesPlayed = 0;
    private int gamesWon = 0;
    private int gamesLost = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // Initialize views
        gamesPlayedView = findViewById(R.id.gamesPlayedView);
        percentageWonView = findViewById(R.id.percentageWonView);
        totalWinsView = findViewById(R.id.totalWinsView);
        totalLossesView = findViewById(R.id.totalLossesView);
        averageTimeView = findViewById(R.id.averageTimeView);
        winStreakView = findViewById(R.id.winStreakView);
        ImageButton backButton = findViewById(R.id.backButton);

        // Back button logic
        backButton.setOnClickListener(v -> onBackPressed());

        // Firebase reference setup
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (userEmail != null) {
            userStatsRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("users")
                    .child(userEmail.replace(".", ","))
                    .child("stats");

            loadStats();
        } else {
            Toast.makeText(this, "Please log in to view stats.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadStats() {
        userStatsRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // Fetch totalWins and totalLosses from the snapshot
                gamesWon = snapshot.child("totalWins").exists() ? snapshot.child("totalWins").getValue(Integer.class) : 0;
                gamesLost = snapshot.child("totalLosses").exists() ? snapshot.child("totalLosses").getValue(Integer.class) : 0;

                // Calculate gamesPlayed as the sum of wins and losses
                gamesPlayed = gamesWon + gamesLost;

                int totalTime = snapshot.child("totalTime").exists() ? snapshot.child("totalTime").getValue(Integer.class) : 0;
                int winStreak = snapshot.child("winStreak").exists() ? snapshot.child("winStreak").getValue(Integer.class) : 0;

                // Calculate metrics
                float percentageWon = gamesPlayed > 0 ? ((float) gamesWon / gamesPlayed) * 100 : 0;
                int averageTime = gamesPlayed > 0 ? totalTime / gamesPlayed : 0;

                // Update views
                gamesPlayedView.setText("Games Played: " + gamesPlayed);
                percentageWonView.setText("Percentage Won: " + String.format("%.2f", percentageWon) + "%");
                totalWinsView.setText("Total Wins: " + gamesWon);
                totalLossesView.setText("Total Losses: " + gamesLost);
                averageTimeView.setText("Average Time: " + averageTime + "s");
                winStreakView.setText("Win Streak: " + winStreak);
            } else {
                Toast.makeText(this, "No stats available.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load stats.", Toast.LENGTH_SHORT).show());
    }
}
