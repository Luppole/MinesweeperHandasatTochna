package com.example.minesweeperhansasattochna;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private TextView emailTextView, nicknameTextView;
    private EditText addFriendInput, nicknameInput;
    private Button saveNicknameButton, sendFriendRequestButton;
    private ProgressBar loadingIndicator;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        emailTextView = findViewById(R.id.emailTextView);
        nicknameTextView = findViewById(R.id.nicknameTextView);
        nicknameInput = findViewById(R.id.nicknameInput);
        saveNicknameButton = findViewById(R.id.saveNicknameButton);
        addFriendInput = findViewById(R.id.addFriendInput);
        sendFriendRequestButton = findViewById(R.id.sendFriendRequestButton);
        loadingIndicator = findViewById(R.id.loadingIndicator); // Add a ProgressBar in XML

        String userEmail = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;

        if (userEmail != null) {
            emailTextView.setText(userEmail);
            userRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("users").child(userEmail.replace(".", ","));
        } else {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_SHORT).show();
            finish(); // Exit activity if the user is not authenticated
        }

        saveNicknameButton.setOnClickListener(v -> saveNickname());
        sendFriendRequestButton.setOnClickListener(v -> {
            String recipientEmail = addFriendInput.getText().toString().trim();
            if (TextUtils.isEmpty(recipientEmail)) {
                Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_SHORT).show();
            } else {
                sendFriendRequest(recipientEmail);
            }
        });
    }

    private void saveNickname() {
        String newNickname = nicknameInput.getText().toString().trim();
        if (TextUtils.isEmpty(newNickname)) {
            Toast.makeText(this, "Nickname cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingIndicator.setVisibility(View.VISIBLE); // Show loading indicator
        userRef.child("nickname").setValue(newNickname)
                .addOnSuccessListener(aVoid -> {
                    loadingIndicator.setVisibility(View.GONE); // Hide loading indicator
                    nicknameTextView.setText(newNickname); // Update the UI
                    Toast.makeText(this, "Nickname updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    loadingIndicator.setVisibility(View.GONE); // Hide loading indicator
                    Toast.makeText(this, "Failed to update nickname: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendFriendRequest(String recipientEmail) {
        loadingIndicator.setVisibility(View.VISIBLE); // Show loading indicator
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users");

        usersRef.child(recipientEmail.replace(".", ",")).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                DatabaseReference recipientRef = usersRef.child(recipientEmail.replace(".", ",")).child("friendRequests");

                String requestId = recipientRef.push().getKey();
                if (requestId != null) {
                    HashMap<String, String> request = new HashMap<>();
                    request.put("from", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    request.put("status", "pending");

                    recipientRef.child(requestId).setValue(request).addOnSuccessListener(aVoid -> {
                        loadingIndicator.setVisibility(View.GONE); // Hide loading indicator
                        Toast.makeText(this, "Friend request sent!", Toast.LENGTH_SHORT).show();
                        addFriendInput.setText(""); // Clear the input field
                    }).addOnFailureListener(e -> {
                        loadingIndicator.setVisibility(View.GONE); // Hide loading indicator
                        Toast.makeText(this, "Failed to send friend request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                loadingIndicator.setVisibility(View.GONE); // Hide loading indicator
                Toast.makeText(this, "The email does not exist in the database.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            loadingIndicator.setVisibility(View.GONE); // Hide loading indicator
            Toast.makeText(this, "Error checking email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
