package com.example.minesweeperhandasattochna;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private static final int PICK_MUSIC_REQUEST = 1;

    private Switch musicSwitch;
    private Button changeMusicButton, changePasswordButton;
    private ImageButton backButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();

        // Initialize UI elements
        backButton = findViewById(R.id.backButton);
        musicSwitch = findViewById(R.id.musicSwitch);
        changeMusicButton = findViewById(R.id.changeMusicButton);
        changePasswordButton = findViewById(R.id.resetPasswordButton);

        // Set initial state of the music switch
        musicSwitch.setChecked(isMusicEnabled());

        // Music switch toggle logic
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

        // Reset Password button logic
        changePasswordButton.setOnClickListener(v -> resetPassword());

        // Change Music button logic
        changeMusicButton.setOnClickListener(v -> openFileChooser());
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

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*"); // Allow audio file selection
        startActivityForResult(intent, PICK_MUSIC_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_MUSIC_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri musicUri = data.getData();
            saveCustomMusicUri(musicUri);
            Toast.makeText(this, "Music file selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCustomMusicUri(Uri musicUri) {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("CustomMusicUri", musicUri.toString());
        editor.apply();
    }
}
