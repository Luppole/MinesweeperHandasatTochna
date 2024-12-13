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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

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

        // Back button listener
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameHistoryActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        });

        // Clear history button listener
        clearHistoryButton.setOnClickListener(v -> clearHistory());

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (userEmail != null) {
            historyRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("users")
                    .child(userEmail.replace(".", ","))
                    .child("gameHistory");

            // Load game history in real-time
            listenForGameHistoryUpdates();
        } else {
            Toast.makeText(this, "Please log in to view your game history.", Toast.LENGTH_SHORT).show();
        }
    }

    private void listenForGameHistoryUpdates() {
        historyRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    historyData = new ArrayList<>();
                    for (DataSnapshot entry : snapshot.getChildren()) {
                        String difficulty = entry.child("difficulty").getValue(String.class);
                        Long time = entry.child("timeTaken").getValue(Long.class);
                        Boolean won = entry.child("won").getValue(Boolean.class);
                        String timestamp = entry.child("timestamp").getValue(String.class);

                        if (difficulty != null && time != null && won != null && timestamp != null) {
                            String formattedTimestamp = formatTimestamp(timestamp);
                            historyData.add(new HistoryItem(difficulty.toUpperCase(), time, won, formattedTimestamp));
                        }
                    }

                    // Sort by timestamp (latest first)
                    Collections.sort(historyData, Comparator.comparing(HistoryItem::getTimestamp).reversed());

                    adapter = new HistoryAdapter(historyData);
                    historyList.setAdapter(adapter);
                } else {
                    historyData = new ArrayList<>();
                    adapter = new HistoryAdapter(historyData);
                    historyList.setAdapter(adapter);
                    Toast.makeText(GameHistoryActivity.this, "No game history found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(GameHistoryActivity.this, "Failed to load game history.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yy : HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);
            return date != null ? outputFormat.format(date) : "Unknown Date";
        } catch (ParseException e) {
            e.printStackTrace();
            return "Unknown Date";
        }
    }

    private void clearHistory() {
        historyRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        int count = 0;
                        for (DataSnapshot entry : snapshot.getChildren()) {
                            if (count >= 10) break; // Stop after deleting 10 entries
                            entry.getRef().removeValue();
                            count++;
                        }
                        Toast.makeText(this, "Cleared the first 10 entries!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No history to clear!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to clear history.", Toast.LENGTH_SHORT).show());
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
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);

            HistoryItem item = items.get(position);
            text1.setText(String.format("%s - %s - %ds", item.getDifficulty(), item.isWon() ? "Win" : "Loss", item.getTime()));
            text2.setText(String.format("Played On: %s", item.getTimestamp()));

            // Set font
            Typeface pixellari = ResourcesCompat.getFont(GameHistoryActivity.this, R.font.pixellari);
            text1.setTypeface(pixellari);
            text2.setTypeface(pixellari);

            // Set background color
            convertView.setBackgroundColor(item.isWon() ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336")); // Green for win, red for loss

            // Set text color
            text1.setTextColor(Color.WHITE);
            text2.setTextColor(Color.LTGRAY);
            return convertView;
        }
    }

    // Data class for game history
    private static class HistoryItem {
        private final String difficulty;
        private final long time;
        private final boolean won;
        private final String timestamp;

        public HistoryItem(String difficulty, long time, boolean won, String timestamp) {
            this.difficulty = difficulty;
            this.time = time;
            this.won = won;
            this.timestamp = timestamp;
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

        public String getTimestamp() {
            return timestamp;
        }
    }
}
