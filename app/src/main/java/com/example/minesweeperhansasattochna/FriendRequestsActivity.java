package com.example.minesweeperhansasattochna;

import android.os.Bundle;
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

        friendRequestsRecyclerView = findViewById(R.id.friendRequestsRecyclerView);
        friendRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userEmail = auth.getCurrentUser().getEmail();
            userRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(userEmail.replace(".", ",")).child("friendRequests");

            loadFriendRequests();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFriendRequests() {
        userRef.get().addOnSuccessListener(snapshot -> {
            friendRequests.clear();
            for (DataSnapshot request : snapshot.getChildren()) {
                String status = request.child("status").getValue(String.class);
                if ("pending".equals(status)) {
                    String requester = request.child("from").getValue(String.class);
                    if (requester != null) {
                        friendRequests.add(requester);
                    }
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
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load friend requests.", Toast.LENGTH_SHORT).show());
    }

    private void acceptFriendRequest(String requesterEmail) {
        // Update both users' friend lists and remove the request
        userRef.child(requesterEmail.replace(".", ",")).removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Accepted friend request from: " + requesterEmail, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to accept friend request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void rejectFriendRequest(String requesterEmail) {
        userRef.child(requesterEmail.replace(".", ",")).removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Rejected friend request from: " + requesterEmail, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to reject friend request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
