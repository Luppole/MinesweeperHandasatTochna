package com.example.minesweeperhandasattochna;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch musicSwitch;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize UI elements
        backButton = findViewById(R.id.backButton); // Ensure backButton is correctly cast to ImageButton
        musicSwitch = findViewById(R.id.musicSwitch);

        // Set initial state of the music switch based on preferences
        musicSwitch.setChecked(isMusicEnabled());

        // Handle music switch toggle
        musicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startService(new Intent(this, MusicService.class));
                saveMusicPreference(true);
            } else {
                stopService(new Intent(this, MusicService.class));
                saveMusicPreference(false);
            }
        });

        // Back button logic
        backButton.setOnClickListener(v -> finish());
    }

    private boolean isMusicEnabled() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        return prefs.getBoolean("MusicEnabled", true);
    }

    private void saveMusicPreference(boolean enabled) {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("MusicEnabled", enabled);
        editor.apply();
    }
}
