package com.example.minesweeperhansasattochna;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
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
    private Spinner difficultySpinner;
    private DatabaseReference historyRef;
    private ArrayList<HistoryItem> historyData;
    private ArrayList<HistoryItem> filteredData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);
        ImageView backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        historyList = findViewById(R.id.historyList);
        difficultySpinner = findViewById(R.id.difficultySpinner);

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (userEmail != null) {
            historyRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("users")
                    .child(userEmail.replace(".", ","))
                    .child("gameHistory");

            setupSpinner();
            loadHistoryData();
        } else {
            Toast.makeText(this, "Please log in to view your game history.", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);
            return date != null ? outputFormat.format(date) : "Unknown Date";
        } catch (ParseException e) {
            e.printStackTrace();
            return "Unknown Date";
        }
    }

    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"All", "Easy", "Medium", "Hard"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(spinnerAdapter);

        difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDifficulty = (String) parent.getItemAtPosition(position);
                filterHistory(selectedDifficulty);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filterHistory("All");
            }
        });
    }

    private void loadHistoryData() {
        historyData = new ArrayList<>();
        historyRef.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                String difficulty = dataSnapshot.child("difficulty").getValue(String.class);
                Long time = dataSnapshot.child("timeTaken").getValue(Long.class);
                Boolean won = dataSnapshot.child("won").getValue(Boolean.class);
                String timestamp = dataSnapshot.child("timestamp").getValue(String.class);

                if (difficulty != null && time != null && won != null && timestamp != null) {
                    historyData.add(new HistoryItem(difficulty, time, won, timestamp));
                }
            }

            historyData.sort(Comparator.comparing(HistoryItem::getTimestamp).reversed());
            filterHistory("All");
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load history.", Toast.LENGTH_SHORT).show());
    }

    private void filterHistory(String difficulty) {
        if (historyData == null) {
            historyData = new ArrayList<>();
        }

        if (difficulty.equals("All")) {
            filteredData = new ArrayList<>(historyData);
        } else {
            filteredData = new ArrayList<>();
            for (HistoryItem item : historyData) {
                if (item.getDifficulty().equalsIgnoreCase(difficulty)) {
                    filteredData.add(item);
                }
            }
        }

        HistoryAdapter adapter = new HistoryAdapter(filteredData);
        historyList.setAdapter(adapter);
    }

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
            text2.setText(String.format("Played On: %s", formatTimestamp(item.getTimestamp())));

            convertView.setBackgroundColor(item.isWon() ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));

            text1.setTextColor(Color.WHITE);
            text2.setTextColor(Color.LTGRAY);

            convertView.setOnClickListener(v -> shareGameResult(item));

            return convertView;
        }
    }

    private void shareGameResult(HistoryItem item) {
        String message = String.format("I %s playing Minesweeper on %s difficulty after %d seconds on %s!",
                item.isWon() ? "won" : "lost", item.getDifficulty(), item.getTime(), formatTimestamp(item.getTimestamp()));

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);

        Intent shareIntent = Intent.createChooser(sendIntent, "Share Game Result");
        startActivity(shareIntent);
    }

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
