package com.example.minesweeperhansasattochna;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        TextView loginRedirect = findViewById(R.id.loginRedirect);

        // Create a spannable string to underline and bold "Log In"
        SpannableString spannableString = new SpannableString("Already have an account? Log In");

        // Bold "Log In"
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 25, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Underline "Log In"
        spannableString.setSpan(new UnderlineSpan(), 25, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Make "Log In" clickable
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.secondary)); // Set link color
                ds.setUnderlineText(true); // Keeps underline even on click
            }
        };
        spannableString.setSpan(clickableSpan, 25, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        loginRedirect.setText(spannableString);
        loginRedirect.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }
}
