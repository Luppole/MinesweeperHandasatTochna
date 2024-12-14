package com.example.minesweeperhansasattochna;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start background music service
        startService(new Intent(this, MusicService.class));

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Check if the user is logged in or playing as a guest
        boolean isGuest = getIntent().getBooleanExtra("isGuest", false);
        String userEmail = isGuest ? "Guest" : (mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "Unknown");

        if (!isGuest && mAuth.getCurrentUser() == null) {
            // Redirect to LoginActivity if not logged in and not playing as a guest
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        // Display logged-in email
        TextView loggedInTextView = findViewById(R.id.loggedInTextView);
        loggedInTextView.setText("Logged in with: " + userEmail);

        // Initialize UI Elements
        Button rulesButton = findViewById(R.id.rulesButton);
        Button logoutButton = findViewById(R.id.signOutButton);
        Button profileButton = findViewById(R.id.profileButton);
        Button playGameButton = findViewById(R.id.playGameButton);
        Button storeButton = findViewById(R.id.storeButton);
        Button settingsButton = findViewById(R.id.settingsButton); // Add Settings Button

        // Rules Button Click Listener
        rulesButton.setOnClickListener(v -> {
            Intent rulesIntent = new Intent(MainActivity.this, RulesActivity.class);
            startActivity(rulesIntent);
        });

        // Logout Button Click Listener
        if (isGuest) {
            logoutButton.setText("Exit");
            logoutButton.setOnClickListener(v -> {
                stopService(new Intent(this, MusicService.class)); // Stop music service
                finish(); // Exit the app for guest users
            });
        } else {
            logoutButton.setOnClickListener(v -> {
                mAuth.signOut(); // Sign out the user
                stopService(new Intent(this, MusicService.class)); // Stop music service
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            });
        }

        // Profile Button Click Listener
        profileButton.setOnClickListener(v -> {
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        // Play Game Button Click Listener
        playGameButton.setOnClickListener(this::onButtonShowPopupWindowClick);

        // Store Button Click Listener
        storeButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                Intent intent = new Intent(MainActivity.this, StoreActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "You need to log in to access the store.", Toast.LENGTH_SHORT).show();
            }
        });

        // Settings Button Click Listener
        settingsButton.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        });

        AlarmService alarmService = new AlarmService(this);
        alarmService.setDailyReminder();
    }

    public void onButtonShowPopupWindowClick(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_difficulty_popup, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        Button easyButton = popupView.findViewById(R.id.easyButton);
        Button mediumButton = popupView.findViewById(R.id.mediumButton);
        Button hardButton = popupView.findViewById(R.id.hardButton);

        easyButton.setOnClickListener(v -> {
            popupWindow.dismiss();
            startGameActivity("easy");
        });

        mediumButton.setOnClickListener(v -> {
            popupWindow.dismiss();
            startGameActivity("medium");
        });

        hardButton.setOnClickListener(v -> {
            popupWindow.dismiss();
            startGameActivity("hard");
        });
    }

    private void startGameActivity(String difficulty) {
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
    }
}
