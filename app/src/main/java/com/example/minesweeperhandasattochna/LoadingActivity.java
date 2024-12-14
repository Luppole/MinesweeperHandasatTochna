package com.example.minesweeperhandasattochna;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // Retrieve "isGuest" flag from the intent
        boolean isGuest = getIntent().getBooleanExtra("isGuest", false);

        // Simulate loading delay
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
            intent.putExtra("isGuest", isGuest); // Pass "isGuest" flag to MainActivity
            startActivity(intent);
            finish();
        }, 2000); // 2 seconds delay
    }
}
