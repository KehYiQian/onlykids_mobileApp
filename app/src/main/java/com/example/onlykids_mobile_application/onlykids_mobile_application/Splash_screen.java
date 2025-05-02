package com.example.onlykids_mobile_application.onlykids_mobile_application;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.example.onlykids.R;
import androidx.appcompat.app.AppCompatActivity;

public class Splash_screen extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ImageView logo = findViewById(R.id.logoImage);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_screen_anim);
        logo.startAnimation(animation);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Splash_screen.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}
