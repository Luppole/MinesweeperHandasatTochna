package com.example.minesweeperhansasattochna;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private TextView emailTextView, nicknameTextView;
    private EditText addFriendInput, nicknameInput;
    private Button saveNicknameButton, sendFriendRequestButton, viewFriendRequestsButton;
    private ProgressBar loadingIndicator;
    private ListView friendsLeaderboardList;

    private DatabaseReference userRef, usersRef;
    private FirebaseAuth auth;

    private ArrayList<String> leaderboardData;
    private ArrayAdapter<String> leaderboardAdapter;

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
        viewFriendRequestsButton = findViewById(R.id.viewFriendRequestsButton);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        friendsLeaderboardList = findViewById(R.id.friendsLeaderboardList);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users");

        if (auth.getCurrentUser() != null) {
            String userEmail = auth.getCurrentUser().getEmail();
            emailTextView.setText(userEmail);

            userRef = usersRef.child(userEmail.replace(".", ","));
            loadFriendsLeaderboard();
        } else {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_SHORT).show();
            finish();
        }

        saveNicknameButton.setOnClickListener(v -> saveNickname());
        sendFriendRequestButton.setOnClickListener(v -> sendFriendRequest());
        viewFriendRequestsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FriendRequestsActivity.class);
            startActivity(intent);
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
                    nicknameTextView.setText(newNickname);
                    Toast.makeText(this, "Nickname updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    loadingIndicator.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to update nickname: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                String requestId = recipientRef.push().getKey();

                if (requestId != null) {
                    HashMap<String, String> request = new HashMap<>();
                    request.put("from", senderEmail);
                    request.put("status", "pending");

                    recipientRef.child(requestId).setValue(request)
                            .addOnSuccessListener(aVoid -> {
                                loadingIndicator.setVisibility(View.GONE);
                                Toast.makeText(this, "Friend request sent successfully!", Toast.LENGTH_SHORT).show();
                                addFriendInput.setText("");
                            })
                            .addOnFailureListener(e -> {
                                loadingIndicator.setVisibility(View.GONE);
                                Toast.makeText(this, "Failed to send friend request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    loadingIndicator.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to generate request ID.", Toast.LENGTH_SHORT).show();
                }
            } else {
                loadingIndicator.setVisibility(View.GONE);
                Toast.makeText(this, "The email does not exist in the database.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            loadingIndicator.setVisibility(View.GONE);
            Toast.makeText(this, "Error checking recipient email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadFriendsLeaderboard() {
        leaderboardData = new ArrayList<>();
        leaderboardAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, leaderboardData);
        friendsLeaderboardList.setAdapter(leaderboardAdapter); // Set the adapter

        // Add current user to the leaderboard
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String nickname = snapshot.child("nickname").getValue(String.class);
                int points = snapshot.child("points").exists() ? snapshot.child("points").getValue(Integer.class) : 0;
                String displayName = nickname != null ? nickname : auth.getCurrentUser().getEmail();
                leaderboardData.add("(You) " + displayName + " - Points: " + points);
                leaderboardAdapter.notifyDataSetChanged();
            } else {
                leaderboardData.add("Unable to fetch your data.");
                leaderboardAdapter.notifyDataSetChanged();
            }

            // Fetch and display friends' data
            fetchFriendsData();
        }).addOnFailureListener(e -> {
            leaderboardData.add("Failed to load your data.");
            leaderboardAdapter.notifyDataSetChanged();
        });
    }

    private void fetchFriendsData() {
        // Fetch friends of the current user
        userRef.child("friends").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    String friendKey = friendSnapshot.getKey(); // e.g., "weezard1234@gmail,com"
                    if (friendKey != null) {
                        Log.d("FriendsLeaderboard", "Processing friend: " + friendKey);

                        // Fetch friend's data
                        usersRef.child(friendKey).get().addOnSuccessListener(friendDataSnapshot -> {
                            if (friendDataSnapshot.exists()) {
                                String nickname = friendDataSnapshot.child("nickname").getValue(String.class);
                                int points = friendDataSnapshot.child("points").exists()
                                        ? friendDataSnapshot.child("points").getValue(Integer.class)
                                        : 0; // Default to 0 if points do not exist
                                String displayName = (nickname != null && !nickname.isEmpty()) ? nickname : friendKey.replace(",", ".");

                                // Add friend's data to the leaderboard
                                String entry = displayName + " - Points: " + points;
                                Log.d("FriendsLeaderboard", "Added friend: " + entry);
                                leaderboardData.add(entry);
                                leaderboardAdapter.notifyDataSetChanged();
                            } else {
                                Log.d("FriendsLeaderboard", "No data for friend: " + friendKey);
                            }
                        }).addOnFailureListener(e -> {
                            Log.e("FriendsLeaderboard", "Failed to fetch friend data for: " + friendKey, e);
                            leaderboardData.add(friendKey.replace(",", ".") + " - Failed to load data");
                            leaderboardAdapter.notifyDataSetChanged();
                        });
                    }
                }
            } else {
                leaderboardData.add("You have no friends added to your leaderboard.");
                leaderboardAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(e -> {
            leaderboardData.add("Failed to fetch friends' data.");
            leaderboardAdapter.notifyDataSetChanged();
        });
    }


}
