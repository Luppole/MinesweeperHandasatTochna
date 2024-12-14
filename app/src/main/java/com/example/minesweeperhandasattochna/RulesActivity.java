package com.example.minesweeperhandasattochna;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class RulesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);

        // Add back button functionality
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }
}
