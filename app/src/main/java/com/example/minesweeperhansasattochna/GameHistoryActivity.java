package com.example.minesweeperhansasattochna;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
    private HistoryAdapter adapter;
    private ArrayList<HistoryItem> historyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);

        historyList = findViewById(R.id.historyList);
        ImageButton backButton = findViewById(R.id.backButton);
        androidx.appcompat.widget.AppCompatButton clearHistoryButton = findViewById(R.id.clearHistoryButton);

        // Home button click listener
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameHistoryActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        });

        // Clear history button click listener
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
                                historyData.add(new HistoryItem(difficulty.toUpperCase(), time, won));
                            }
                        }

                        adapter = new HistoryAdapter(historyData);
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
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to clear game history.", Toast.LENGTH_SHORT).show());
    }

    // Custom adapter for the game history
    private class HistoryAdapter extends BaseAdapter {
        private final ArrayList<HistoryItem> items;

        public HistoryAdapter(ArrayList<HistoryItem> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            TextView textView = convertView.findViewById(android.R.id.text1);

            HistoryItem item = items.get(position);
            textView.setText(String.format("%s - %s - %ds", item.getDifficulty(), item.isWon() ? "Win" : "Loss", item.getTime()));

            // Set font
            Typeface pixellari = ResourcesCompat.getFont(GameHistoryActivity.this, R.font.pixellari);
            textView.setTypeface(pixellari);

            // Set background color
            convertView.setBackgroundColor(item.isWon() ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336")); // Green for win, red for loss

            // Set text color
            textView.setTextColor(Color.WHITE);
            return convertView;
        }
    }

    // Data class for game history
    private static class HistoryItem {
        private final String difficulty;
        private final long time;
        private final boolean won;

        public HistoryItem(String difficulty, long time, boolean won) {
            this.difficulty = difficulty;
            this.time = time;
            this.won = won;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public long getTime() {
            return time;
        }

        public boolean isWon() {
            return won;
        }
    }
}
