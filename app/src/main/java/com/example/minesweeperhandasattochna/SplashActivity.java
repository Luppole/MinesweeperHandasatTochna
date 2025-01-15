package com.example.minesweeperhandasattochna;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import java.util.Locale;


public class SplashActivity extends AppCompatActivity {

    TextToSpeech greetingsTts;
    String text;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        greetingsTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR)
                    greetingsTts.setLanguage(Locale.ENGLISH);
            }
        });

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            text = "Welcome back " + username;
        }

        else {
            text = "Welcome To Minesweeper, guest!";
        }

        greetingsTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);

        new Handler().postDelayed(() -> {
            Intent intent;
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 5000); // Delay of 3 seconds
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_splash);
    }

}
