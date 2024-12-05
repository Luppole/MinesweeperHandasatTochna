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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView signupRedirect = findViewById(R.id.signupRedirect);

        // Create a spannable string to underline and bold "Sign Up"
        SpannableString spannableString = new SpannableString("Don't have an account? Sign Up");

        // Bold "Sign Up"
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 22, 29, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Underline "Sign Up"
        spannableString.setSpan(new UnderlineSpan(), 22, 29, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Make "Sign Up" clickable
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
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
        spannableString.setSpan(clickableSpan, 22, 29, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        signupRedirect.setText(spannableString);
        signupRedirect.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }
}
