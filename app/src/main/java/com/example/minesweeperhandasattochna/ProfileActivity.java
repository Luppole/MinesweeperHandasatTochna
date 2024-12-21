package com.example.minesweeperhandasattochna;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView emailTextView, nicknameTextView;
    private EditText addFriendInput, nicknameInput;
    private ProgressBar loadingIndicator;
    private RecyclerView friendsRecyclerView;
    private FriendAdapter friendAdapter;
    private ArrayList<Friend> friendList;

    private ImageView profilePicture;
    private Button changeProfilePictureButton;

    private DatabaseReference userRef, usersRef;
    private FirebaseAuth auth;
    private Uri imageUri;

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
        profilePicture = findViewById(R.id.profilePicture);
        changeProfilePictureButton = findViewById(R.id.changeProfilePictureButton);

        Button statsButton = findViewById(R.id.statsButton);
        ImageView backButton = findViewById(R.id.backButton);

        // Navigate to Statistics
        statsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, StatsActivity.class);
            startActivity(intent);
        });

        // Navigate to Achievements
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
            loadProfilePicture();
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

        changeProfilePictureButton.setOnClickListener(v -> openFileChooser());

        // Set up RecyclerView
        friendList = new ArrayList<>();
        friendAdapter = new FriendAdapter(friendList, this); // Pass 'this' as the Context
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setAdapter(friendAdapter);
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                uploadProfilePicture(bitmap);
            } catch (IOException e) {
                Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadProfilePicture(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            userRef.child("profilePicture").setValue(encodedImage)
                    .addOnSuccessListener(aVoid -> {
                        profilePicture.setImageBitmap(bitmap);
                        Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to upload image.", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadProfilePicture() {
        userRef.child("profilePicture").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String encodedImage = snapshot.getValue(String.class);
                if (encodedImage != null) {
                    byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    profilePicture.setImageBitmap(decodedBitmap);
                }
            }
        });
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
                        String profilePicture = friendDataSnapshot.child("profilePicture").getValue(String.class);
                        int points = friendDataSnapshot.child("points").exists()
                                ? friendDataSnapshot.child("points").getValue(Integer.class)
                                : 0;
                        String displayName = (nickname != null && !nickname.isEmpty()) ? nickname : friendKey.replace(",", ".");
                        friendList.add(new Friend(displayName, points, profilePicture));
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
