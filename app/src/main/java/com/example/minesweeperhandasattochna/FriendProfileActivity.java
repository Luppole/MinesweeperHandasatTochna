package com.example.minesweeperhandasattochna;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FriendProfileActivity extends AppCompatActivity {

    private TextView friendNicknameTextView, friendPointsTextView;
    private ImageView friendProfilePicture;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        // Initialize views
        friendNicknameTextView = findViewById(R.id.friendNicknameTextView);
        friendPointsTextView = findViewById(R.id.friendPointsTextView);
        friendProfilePicture = findViewById(R.id.friendProfilePicture);

        // Firebase setup
        usersRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users");

        // Get friend's email from intent
        String friendEmail = getIntent().getStringExtra("friendEmail");
        if (friendEmail != null) {
            loadFriendProfile(friendEmail.replace(".", ","));
        }
    }

    private void loadFriendProfile(String friendKey) {
        usersRef.child(friendKey).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String nickname = snapshot.child("nickname").getValue(String.class);
                Integer points = snapshot.child("points").getValue(Integer.class);
                String profilePictureUrl = snapshot.child("profilePicture").getValue(String.class);

                friendNicknameTextView.setText("Nickname: " + (nickname != null ? nickname : "--"));
                friendPointsTextView.setText("Points: " + (points != null ? points : 0));

                if (profilePictureUrl != null) {
                    Glide.with(this).load(profilePictureUrl).into(friendProfilePicture);
                }
            }
        }).addOnFailureListener(e -> {
            friendNicknameTextView.setText("Failed to load friend's profile");
        });
    }
}
