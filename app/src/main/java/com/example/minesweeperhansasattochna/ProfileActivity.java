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
        sendFriendRequestButton.setOnClickListener(v -> sendFriendRequest());
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

    private void sendFriendRequest() {
        String friendEmail = addFriendInput.getText().toString().trim();
        if (TextUtils.isEmpty(friendEmail)) {
            Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference friendRequestsRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users").child(friendEmail.replace(".", ",")).child("friendRequests");

        HashMap<String, Object> request = new HashMap<>();
        request.put("from", FirebaseAuth.getInstance().getCurrentUser().getEmail());

        friendRequestsRef.push().setValue(request)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Friend request sent!", Toast.LENGTH_SHORT).show();
                    addFriendInput.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send friend request.", Toast.LENGTH_SHORT).show());
    }
}
