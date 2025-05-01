package com.example.onlykids_mobile_application;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private MaterialButton btnStart, btnNotes, btnMistakeMemo, btnReport, btnVideo, btnExit;
    private TextView tvAppTitle;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        tvAppTitle = findViewById(R.id.tv_app_title);
        btnStart = findViewById(R.id.btn_start);
        btnNotes = findViewById(R.id.btn_notes);
        btnMistakeMemo = findViewById(R.id.btn_mistake_memo);
        btnReport = findViewById(R.id.btn_report);
        btnVideo = findViewById(R.id.btn_video);
        btnExit = findViewById(R.id.btn_exit);

        // Initialize MediaPlayer for background music
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true); // Set the music to loop indefinitely
        mediaPlayer.setVolume(0.8f, 0.8f);

        // Load animations
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation popInAnimation = AnimationUtils.loadAnimation(this, R.anim.pop_in);
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_button);

        // Apply fade-in animation to the title
        tvAppTitle.startAnimation(fadeInAnimation);

        // Apply pop-in animation to buttons with a staggered delay
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> btnStart.startAnimation(popInAnimation), 100);
        handler.postDelayed(() -> btnNotes.startAnimation(popInAnimation), 200);
        handler.postDelayed(() -> btnMistakeMemo.startAnimation(popInAnimation), 300);
        handler.postDelayed(() -> btnReport.startAnimation(popInAnimation), 400);
        handler.postDelayed(() -> btnVideo.startAnimation(popInAnimation), 500);
        handler.postDelayed(() -> btnExit.startAnimation(popInAnimation), 600);

        // Set click listeners with scale animation
//        btnStart.setOnClickListener(v -> {
//            v.startAnimation(scaleAnimation);
//            Intent intent = new Intent(MainActivity.this, PreClassActivity.class);
//            startActivity(intent);
//        });
//
//        btnNotes.setOnClickListener(v -> {
//            v.startAnimation(scaleAnimation);
//            Intent intent = new Intent(MainActivity.this, NotesActivity.class);
//            startActivity(intent);
//        });
//
//        btnMistakeMemo.setOnClickListener(v -> {
//            v.startAnimation(scaleAnimation);
//            Intent intent = new Intent(MainActivity.this, MistakeMemoActivity.class);
//            startActivity(intent);
//        });
//
//        btnReport.setOnClickListener(v -> {
//            v.startAnimation(scaleAnimation);
//            Intent intent = new Intent(MainActivity.this, PerformanceReportActivity.class);
//            startActivity(intent);
//        });
//
        btnVideo.setOnClickListener(v -> {
            v.startAnimation(scaleAnimation);
            Intent intent = new Intent(MainActivity.this, VideoSearchActivity.class);
            startActivity(intent);
        });

        btnExit.setOnClickListener(v -> {
            v.startAnimation(scaleAnimation);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start or resume the background music when the activity becomes visible
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the background music when the activity is paused (e.g., app in background)
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources when the activity is destroyed
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}