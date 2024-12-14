package com.example.minesweeperhandasattochna;

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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button loginButton, guestButton;
    private TextView signupRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Check if a user is already logged in
        if (mAuth.getCurrentUser() != null) {
            // User is logged in, redirect to MainActivity
            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish(); // Close LoginActivity
            return;
        }

        // Set content view if no user is logged in
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);
        guestButton = findViewById(R.id.guestButton);
        signupRedirect = findViewById(R.id.signupRedirect);

        // Handle Login Button Click
        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Redirect to LoadingActivity before MainActivity
                            Intent loadingIntent = new Intent(LoginActivity.this, LoadingActivity.class);
                            startActivity(loadingIntent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Handle Guest Button Click
        guestButton.setOnClickListener(v -> {
            Intent guestIntent = new Intent(LoginActivity.this, LoadingActivity.class);
            guestIntent.putExtra("isGuest", true); // Pass the "isGuest" flag
            startActivity(guestIntent);
            finish();
        });

        // Add Hyperlink to "Sign Up"
        setSignupHyperlink();
    }

    private void setSignupHyperlink() {
        String text = "Don't have an account? Sign Up";
        SpannableString spannableString = new SpannableString(text);

        // Make "Sign Up" clickable
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent signupIntent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(signupIntent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true); // Add underline
            }
        };

        // Bold "Sign Up"
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 23, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply ClickableSpan to "Sign Up"
        spannableString.setSpan(clickableSpan, 23, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        signupRedirect.setText(spannableString);
        signupRedirect.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }
}
