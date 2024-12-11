package com.example.minesweeperhansasattochna;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        Button logoutButton = findViewById(R.id.signOutButton);
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut(); // Sign out the user
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close the current activity
        });

        Button profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        Button playGameButton = findViewById(R.id.playGameButton);
        playGameButton.setOnClickListener(this::onButtonShowPopupWindowClick);

        // Initialize Store Button outside the popup logic
        Button storeButton = findViewById(R.id.storeButton);
        storeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StoreActivity.class);
            startActivity(intent);
        });
    }

    public void onButtonShowPopupWindowClick(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_difficulty_popup, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        Button easyButton = popupView.findViewById(R.id.easyButton);
        Button mediumButton = popupView.findViewById(R.id.mediumButton);
        Button hardButton = popupView.findViewById(R.id.hardButton);

        easyButton.setOnClickListener(v -> {
            popupWindow.dismiss();
            startGameActivity("easy");
        });

        mediumButton.setOnClickListener(v -> {
            popupWindow.dismiss();
            startGameActivity("medium");
        });

        hardButton.setOnClickListener(v -> {
            popupWindow.dismiss();
            startGameActivity("hard");
        });
    }

    private void startGameActivity(String difficulty) {
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
    }
}
