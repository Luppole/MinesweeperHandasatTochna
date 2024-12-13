package com.example.minesweeperhansasattochna;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField, confirmPasswordField;
    private Button signupButton;
    private TextView loginRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        signupButton = findViewById(R.id.signupButton);
        loginRedirect = findViewById(R.id.loginRedirect);

        // Handle Sign-Up Button
        signupButton.setOnClickListener(v -> handleSignUp());

        // Add Hyperlink for "Log In"
        setLoginHyperlink();
    }

    private void handleSignUp() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        initializeUserInDatabase(email.replace(".", ","));
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initializeUserInDatabase(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        HashMap<String, Object> userData = new HashMap<>();
        userData.put("nickname", "Player");
        userData.put("points", 0);

        // Initialize default stats and achievements
        HashMap<String, Object> defaultStats = new HashMap<>();
        defaultStats.put("totalGames", 0);
        defaultStats.put("totalWins", 0);
        defaultStats.put("winStreak", 0);

        HashMap<String, Object> defaultAchievements = new HashMap<>();
        defaultAchievements.put("play_100_games/progress", 0);
        defaultAchievements.put("play_100_games/goal", 100);
        defaultAchievements.put("win_50_games/progress", 0);
        defaultAchievements.put("win_50_games/goal", 50);
        defaultAchievements.put("win_streak_10/progress", 0);
        defaultAchievements.put("win_streak_10/goal", 10);

        userRef.setValue(userData);
        userRef.child("stats").setValue(defaultStats);
        userRef.child("achievements").setValue(defaultAchievements);
    }

    private void setLoginHyperlink() {
        String text = "Already have an account? Log In";
        SpannableString spannableString = new SpannableString(text);

        // Make "Log In" clickable
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true); // Add underline
                ds.setColor(getResources().getColor(android.R.color.holo_blue_light)); // Optional: Change color
            }
        };

        // Bold "Log In"
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 25, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply ClickableSpan to "Log In"
        spannableString.setSpan(clickableSpan, 25, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        loginRedirect.setText(spannableString);
        loginRedirect.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }
}
