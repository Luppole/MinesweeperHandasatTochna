package com.example.minesweeperhansasattochna;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class GameHistoryActivity extends AppCompatActivity {

    private ListView historyList;
    private DatabaseReference historyRef;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> historyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);

        historyList = findViewById(R.id.historyList);
        ImageButton homeButton = findViewById(R.id.homeButton);
        androidx.appcompat.widget.AppCompatButton clearHistoryButton = findViewById(R.id.clearHistoryButton);

        // Set home button click listener to navigate back to main activity
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameHistoryActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Optional to avoid stacking activities
        });

        clearHistoryButton.setOnClickListener(v -> clearHistory());

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
                        historyData = new ArrayList<>();
                        for (DataSnapshot entry : snapshot.getChildren()) {
                            String difficulty = entry.child("difficulty").getValue(String.class);
                            Long time = entry.child("timeTaken").getValue(Long.class);
                            Boolean won = entry.child("won").getValue(Boolean.class);

                            if (difficulty != null && time != null && won != null) {
                                String result = won ? "Win" : "Loss";
                                historyData.add(difficulty.toUpperCase() + " - " + result + " - " + time + "s");
                            }
                        }

                        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, historyData) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView textView = (TextView) view.findViewById(android.R.id.text1);

                                // Safely load the font
                                try {
                                    Typeface pixellari = ResourcesCompat.getFont(GameHistoryActivity.this, R.font.pixellari);
                                    textView.setTypeface(pixellari);
                                } catch (Exception e) {
                                    Toast.makeText(GameHistoryActivity.this, "Font not found!", Toast.LENGTH_SHORT).show();
                                }

                                return view;
                            }
                        };

                        // Set the adapter to the ListView
                        historyList.setAdapter(adapter);
                    } else {
                        Toast.makeText(this, "No game history found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load game history.", Toast.LENGTH_SHORT).show());
    }

    private void clearHistory() {
        historyRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (historyData != null) {
                        historyData.clear();
                        adapter.notifyDataSetChanged();
                    }
                    Toast.makeText(this, "Game history cleared successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to clear game history: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
