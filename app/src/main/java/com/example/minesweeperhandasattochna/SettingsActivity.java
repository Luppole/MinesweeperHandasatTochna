package com.example.minesweeperhandasattochna;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private Switch musicSwitch;
    private ImageButton backButton;
    private Button changePasswordButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize UI elements
        backButton = findViewById(R.id.backButton); // Ensure backButton is correctly cast to ImageButton
        musicSwitch = findViewById(R.id.musicSwitch);
        changePasswordButton = findViewById(R.id.resetPasswordButton);

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

        // Change Password button logic
        changePasswordButton.setOnClickListener(v -> resetPassword());
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

    private void resetPassword() {
        String email = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;
        if (email != null) {
            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "No email associated with this account.", Toast.LENGTH_SHORT).show();
        }
    }
}
