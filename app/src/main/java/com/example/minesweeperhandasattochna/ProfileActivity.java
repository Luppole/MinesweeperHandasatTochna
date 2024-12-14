package com.example.minesweeperhandasattochna;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private TextView emailTextView, nicknameTextView;
    private EditText addFriendInput, nicknameInput;
    private ProgressBar loadingIndicator;
    private RecyclerView friendsRecyclerView;
    private FriendAdapter friendAdapter;
    private ArrayList<Friend> friendList;

    private DatabaseReference userRef, usersRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        emailTextView = findViewById(R.id.emailTextView);
        nicknameTextView = findViewById(R.id.nicknameTextView);
        nicknameInput = findViewById(R.id.nicknameInput);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        addFriendInput = findViewById(R.id.addFriendInput);
        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        Button statsButton = findViewById(R.id.statsButton);
        ImageView backButton = findViewById(R.id.backButton);
        statsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, StatsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.viewAchievementsButton).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AchievementsActivity.class);
            startActivity(intent);
        });



        // Firebase setup
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users");

        if (auth.getCurrentUser() != null) {
            String userEmail = auth.getCurrentUser().getEmail();
            emailTextView.setText(userEmail);
            userRef = usersRef.child(userEmail.replace(".", ","));
            loadProfileData();
            loadFriendsList();
        } else {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_SHORT).show();
            finish();
        }

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.saveNicknameButton).setOnClickListener(v -> saveNickname());
        findViewById(R.id.sendFriendRequestButton).setOnClickListener(v -> sendFriendRequest());
        findViewById(R.id.viewFriendRequestsButton).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FriendRequestsActivity.class);
            startActivity(intent);
        });

        // Set up RecyclerView
        friendList = new ArrayList<>();
        friendAdapter = new FriendAdapter(friendList);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setAdapter(friendAdapter);
    }

    private void loadProfileData() {
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String nickname = snapshot.child("nickname").getValue(String.class);
                nicknameTextView.setText("Nickname: " + (nickname != null ? nickname : "--"));
            } else {
                nicknameTextView.setText("Nickname: --");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load profile data.", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadFriendsList() {
        loadingIndicator.setVisibility(View.VISIBLE);
        userRef.child("friends").get().addOnSuccessListener(snapshot -> {
            loadingIndicator.setVisibility(View.GONE);
            friendList.clear();

            for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                String friendKey = friendSnapshot.getKey();

                if (friendKey != null) {
                    usersRef.child(friendKey).get().addOnSuccessListener(friendDataSnapshot -> {
                        String nickname = friendDataSnapshot.child("nickname").getValue(String.class);
                        int points = friendDataSnapshot.child("points").exists()
                                ? friendDataSnapshot.child("points").getValue(Integer.class)
                                : 0;
                        String displayName = (nickname != null && !nickname.isEmpty()) ? nickname : friendKey.replace(",", ".");
                        friendList.add(new Friend(displayName, points));
                        friendAdapter.notifyDataSetChanged();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load friend data.", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).addOnFailureListener(e -> {
            loadingIndicator.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to load friends list.", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveNickname() {
        String newNickname = nicknameInput.getText().toString().trim();
        if (TextUtils.isEmpty(newNickname)) {
            Toast.makeText(this, "Nickname cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingIndicator.setVisibility(View.VISIBLE);
        userRef.child("nickname").setValue(newNickname)
                .addOnSuccessListener(aVoid -> {
                    loadingIndicator.setVisibility(View.GONE);
                    nicknameTextView.setText("Nickname: " + newNickname);
                    Toast.makeText(this, "Nickname updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    loadingIndicator.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to update nickname.", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendFriendRequest() {
        String recipientEmail = addFriendInput.getText().toString().trim();
        if (TextUtils.isEmpty(recipientEmail)) {
            Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingIndicator.setVisibility(View.VISIBLE);
        String senderEmail = auth.getCurrentUser().getEmail();
        if (senderEmail == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            loadingIndicator.setVisibility(View.GONE);
            return;
        }

        usersRef.child(recipientEmail.replace(".", ",")).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                DatabaseReference recipientRef = usersRef.child(recipientEmail.replace(".", ",")).child("friendRequests");
                recipientRef.push().setValue(senderEmail)
                        .addOnSuccessListener(aVoid -> {
                            loadingIndicator.setVisibility(View.GONE);
                            Toast.makeText(this, "Friend request sent successfully!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            loadingIndicator.setVisibility(View.GONE);
                            Toast.makeText(this, "Failed to send friend request.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                loadingIndicator.setVisibility(View.GONE);
                Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            loadingIndicator.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to send friend request.", Toast.LENGTH_SHORT).show();
        });
    }
}
