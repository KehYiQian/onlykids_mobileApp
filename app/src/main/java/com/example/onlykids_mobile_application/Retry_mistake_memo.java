package com.example.onlykids_mobile_application;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Objects;

public class Retry_mistake_memo extends AppCompatActivity {

    private final Button[] optionButtons = new Button[4];
    private final ImageView[] statusIcons = new ImageView[4];

    private String correctAnswer;
    private String mistakeId;
    private boolean answerSubmitted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retry_mistake_memo);

        TextView questionText = findViewById(R.id.retryQuestion);
        optionButtons[0] = findViewById(R.id.retryOptionA);
        optionButtons[1] = findViewById(R.id.retryOptionB);
        optionButtons[2] = findViewById(R.id.retryOptionC);
        optionButtons[3] = findViewById(R.id.retryOptionD);
        statusIcons[0] = findViewById(R.id.retryStatusA);
        statusIcons[1] = findViewById(R.id.retryStatusB);
        statusIcons[2] = findViewById(R.id.retryStatusC);
        statusIcons[3] = findViewById(R.id.retryStatusD);
        Button backButton = findViewById(R.id.retryBackButton);

        mistakeId = getIntent().getStringExtra("mistake_id");
        String question = getIntent().getStringExtra("question");
        correctAnswer = getIntent().getStringExtra("answer");
        String optionsJson = getIntent().getStringExtra("optionsJson");

        List<String> options = new Gson().fromJson(optionsJson, new TypeToken<List<String>>() {}.getType());

        questionText.setText(question);

        for (int i = 0; i < 4; i++) {
            int index = i;
            optionButtons[i].setText(Objects.requireNonNull(options).get(i));
            optionButtons[i].setOnClickListener(v -> handleAnswer(index));
        }

        backButton.setOnClickListener(v -> finish());
    }

    private void handleAnswer(int selectedIndex) {
        if (answerSubmitted) return;

        for (Button b : optionButtons) b.setEnabled(false);

        String selectedText = optionButtons[selectedIndex].getText().toString();
        boolean isCorrect = selectedText.equals(correctAnswer);

        if (isCorrect) {
            optionButtons[selectedIndex].setBackgroundColor(Color.GREEN);
            statusIcons[selectedIndex].setImageResource(R.drawable.icon_correct);

            statusIcons[selectedIndex].startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.fade_in)
            );

            optionButtons[selectedIndex].animate()
                    .scaleX(1.1f).scaleY(1.1f).setDuration(150)
                    .withEndAction(() -> optionButtons[selectedIndex]
                            .animate().scaleX(1f).scaleY(1f).setDuration(150).start())
                    .start();

            // Delete from Room
            Mistake_database db = Room.databaseBuilder(
                    getApplicationContext(),
                    Mistake_database.class,
                    "mistake_db"
            ).allowMainThreadQueries().build();

            db.mistakeDao().clearById(mistakeId);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("removed_id", mistakeId);
            setResult(RESULT_OK, resultIntent);
            finish();

        } else {
            optionButtons[selectedIndex].setBackgroundColor(Color.RED);
            statusIcons[selectedIndex].setImageResource(R.drawable.icon_wrong);

            statusIcons[selectedIndex].startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.fade_in)
            );
            optionButtons[selectedIndex].startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.shake)
            );

            Toast.makeText(this, "Incorrect. The mistake remains.", Toast.LENGTH_SHORT).show();
        }

        statusIcons[selectedIndex].setVisibility(View.VISIBLE);
        answerSubmitted = true;
    }
}
