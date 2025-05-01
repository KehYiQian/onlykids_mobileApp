package com.example.onlykids_mobile_application;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("OnlyKidsPrefs", MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("isFirstTime", true);

        if (isFirstTime) {
            startActivity(new Intent(this, preclass_question.class));
            finish();
        } else {
            setContentView(R.layout.activity_main);
            findViewById(R.id.btnChatRoom).setOnClickListener(v ->
                    startActivity(new Intent(this, chatRoom.class))
            );

            findViewById(R.id.btnMistakeMemo).setOnClickListener(v ->
                    startActivity(new Intent(this, Mistake_memo.class))
            );
        }

    }
}
