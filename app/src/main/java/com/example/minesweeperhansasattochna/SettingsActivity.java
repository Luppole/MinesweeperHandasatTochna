package com.example.minesweeperhansasattochna;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch musicSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        musicSwitch = findViewById(R.id.musicSwitch);
        musicSwitch.setChecked(isMusicEnabled());

        musicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startService(new Intent(this, MusicService.class));
                saveMusicPreference(true);
            } else {
                stopService(new Intent(this, MusicService.class));
                saveMusicPreference(false);
            }
        });
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
