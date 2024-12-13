package com.example.minesweeperhansasattochna;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FriendRequestsActivity extends AppCompatActivity {

    private RecyclerView friendRequestsRecyclerView;
    private DatabaseReference userRef;
    private ArrayList<String> friendRequests = new ArrayList<>();
    private FriendRequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        // Initialize back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        friendRequestsRecyclerView = findViewById(R.id.friendRequestsRecyclerView);
        friendRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userEmail = auth.getCurrentUser().getEmail();
            if (userEmail != null) {
                userRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                        .getReference("users").child(userEmail.replace(".", ",")).child("friendRequests");

                loadFriendRequests();
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void loadFriendRequests() {
        userRef.get().addOnSuccessListener(snapshot -> {
            friendRequests.clear();
            if (snapshot.exists()) {
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    String requester = requestSnapshot.child("from").getValue(String.class);
                    String status = requestSnapshot.child("status").getValue(String.class);

                    if ("pending".equals(status) && requester != null) {
                        friendRequests.add(requester);
                    }
                }

                adapter = new FriendRequestAdapter(this, friendRequests, new FriendRequestAdapter.FriendRequestActionsListener() {
                    @Override
                    public void onAccept(String requesterEmail) {
                        acceptFriendRequest(requesterEmail);
                    }

                    @Override
                    public void onReject(String requesterEmail) {
                        rejectFriendRequest(requesterEmail);
                    }
                });
                friendRequestsRecyclerView.setAdapter(adapter);
            } else {
                Toast.makeText(this, "No friend requests found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load friend requests.", Toast.LENGTH_SHORT).show());
    }

    private void acceptFriendRequest(String requesterEmail) {
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (currentUserEmail == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference currentUserFriendsRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users").child(currentUserEmail.replace(".", ",")).child("friends");

        DatabaseReference requesterFriendsRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users").child(requesterEmail.replace(".", ",")).child("friends");

        // Add each other as friends
        currentUserFriendsRef.child(requesterEmail.replace(".", ",")).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    requesterFriendsRef.child(currentUserEmail.replace(".", ",")).setValue(true)
                            .addOnSuccessListener(aVoid1 -> {
                                // Remove the friend request after accepting
                                userRef.child(requesterEmail.replace(".", ",")).removeValue()
                                        .addOnSuccessListener(aVoid2 -> {
                                            friendRequests.remove(requesterEmail);
                                            adapter.notifyDataSetChanged();
                                            Toast.makeText(this, "Friend request accepted and deleted!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove friend request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to add to requester's friends list: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add to friends list: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void rejectFriendRequest(String requesterEmail) {
        // Remove the friend request after rejecting
        userRef.child(requesterEmail.replace(".", ",")).removeValue()
                .addOnSuccessListener(aVoid -> {
                    friendRequests.remove(requesterEmail);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Friend request rejected and deleted!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to reject friend request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
