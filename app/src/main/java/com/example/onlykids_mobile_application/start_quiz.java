package com.example.onlykids_mobile_application;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class start_quiz extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_quiz);

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            Intent previousIntent = getIntent();
            Intent intent = new Intent(start_quiz.this, quizGame.class);
            intent.putExtra("quizJson", previousIntent.getStringExtra("quizJson"));
            intent.putExtra("currentLevel", previousIntent.getIntExtra("currentLevel", 1));
            intent.putExtra("topic", previousIntent.getStringExtra("topic"));
            intent.putExtra("userQuestion", previousIntent.getStringExtra("userQuestion"));
            startActivity(intent);
            finish();
        });

    }
}
