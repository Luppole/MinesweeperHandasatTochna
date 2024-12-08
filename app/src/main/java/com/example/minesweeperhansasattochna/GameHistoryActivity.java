package com.example.minesweeperhansasattochna;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class GameHistoryActivity extends AppCompatActivity {

    private ListView historyList;
    private DatabaseReference historyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);

        historyList = findViewById(R.id.historyList);

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (userEmail != null) {
            historyRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("gameHistory").child(userEmail.replace(".", ","));

            loadGameHistory();
        } else {
            Toast.makeText(this, "Please log in to view your game history.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGameHistory() {
        historyRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        ArrayList<String> historyData = new ArrayList<>();
                        for (DataSnapshot entry : snapshot.getChildren()) {
                            String difficulty = entry.child("difficulty").getValue(String.class);
                            long time = entry.child("timeTaken").getValue(Long.class);
                            boolean won = entry.child("won").getValue(Boolean.class);
                            String result = won ? "Win" : "Loss";
                            historyData.add(difficulty + " - " + result + " - " + time + "s");
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyData);
                        historyList.setAdapter(adapter);
                    } else {
                        Toast.makeText(this, "No game history found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load game history.", Toast.LENGTH_SHORT).show());
    }
}
