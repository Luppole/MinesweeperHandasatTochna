package com.example.minesweeperhansasattochna;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StoreActivity extends AppCompatActivity {

    private TextView pointsTextView;
    private DatabaseReference userRef;
    private int userPoints = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        pointsTextView = findViewById(R.id.pointsTextView);
        View buyHintButton = findViewById(R.id.buyHintButton);
        View buySuperHintButton = findViewById(R.id.buySuperHintButton);
        View buyShieldButton = findViewById(R.id.buyShieldButton);
        View buyMineDetectorButton = findViewById(R.id.buyMineDetectorButton);
        ImageButton backButton = findViewById(R.id.backButton);

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (userEmail != null) {
            userRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("users")
                    .child(userEmail.replace(".", ","));

            loadUserPoints();
        } else {
            Toast.makeText(this, "Please log in to access the store.", Toast.LENGTH_SHORT).show();
            finish();
        }

        buyHintButton.setOnClickListener(v -> purchaseItem("hint", 2000));
        buySuperHintButton.setOnClickListener(v -> purchaseItem("superHint", 5000));
        buyShieldButton.setOnClickListener(v -> purchaseItem("shield", 7000));
        buyMineDetectorButton.setOnClickListener(v -> purchaseItem("mineDetector", 4000));

        // Back button logic
        backButton.setOnClickListener(v -> onBackPressed());
    }


    private void loadUserPoints() {
        userRef.child("points").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                userPoints = snapshot.getValue(Integer.class);
                pointsTextView.setText("Points: " + userPoints);
            } else {
                pointsTextView.setText("Points: 0");
            }
        }).addOnFailureListener(e -> {
            pointsTextView.setText("Points: --");
            Toast.makeText(this, "Failed to load points.", Toast.LENGTH_SHORT).show();
        });

        if (userPoints >= 2000) {
            NotificationService notificationService = new NotificationService(this);
            notificationService.sendNotification(
                    "Store Update",
                    "You have enough points to buy a Hint!",
                    StoreActivity.class
            );
        }
    }

    private void purchaseItem(String itemName, int cost) {
        if (userPoints >= cost) {
            userPoints -= cost;
            userRef.child("points").setValue(userPoints)
                    .addOnSuccessListener(aVoid -> {
                        userRef.child("items").child(itemName).get()
                                .addOnSuccessListener(snapshot -> {
                                    int currentCount = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                                    userRef.child("items").child(itemName).setValue(currentCount + 1)
                                            .addOnSuccessListener(aVoid1 -> {
                                                pointsTextView.setText("Points: " + userPoints);
                                                Toast.makeText(this, itemName + " purchased successfully!", Toast.LENGTH_SHORT).show();
                                            });
                                });
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to deduct points.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Not enough points!", Toast.LENGTH_SHORT).show();
        }
    }
}
