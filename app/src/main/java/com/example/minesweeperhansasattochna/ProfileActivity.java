package com.example.minesweeperhansasattochna;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
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

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (userEmail != null) {
            emailTextView.setText(userEmail);
            userRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("users").child(userEmail.replace(".", ","));
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
        userRef.child("nickname").setValue(newNickname)
                .addOnSuccessListener(aVoid -> {
                    nicknameTextView.setText(newNickname);
                    Toast.makeText(this, "Nickname updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update nickname.", Toast.LENGTH_SHORT).show());
    }

    private void sendFriendRequest(String recipientEmail) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users");

        // Check if the recipient exists in the database
        usersRef.child(recipientEmail.replace(".", ",")).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // If the recipient exists, proceed to send the friend request
                DatabaseReference recipientRef = usersRef.child(recipientEmail.replace(".", ",")).child("friendRequests");

                String requestId = recipientRef.push().getKey();
                if (requestId != null) {
                    HashMap<String, String> request = new HashMap<>();
                    request.put("from", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    request.put("status", "pending");

                    recipientRef.child(requestId).setValue(request).addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Friend request sent!", Toast.LENGTH_SHORT).show();
                        addFriendInput.setText(""); // Clear the input field
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send friend request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                // If the recipient does not exist, show an error message
                Toast.makeText(this, "The email does not exist in the database.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error checking email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

}
