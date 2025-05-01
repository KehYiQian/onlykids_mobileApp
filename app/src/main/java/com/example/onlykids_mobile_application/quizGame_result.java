package com.example.onlykids_mobile_application;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.MessageFormat;

public class quizGame_result extends AppCompatActivity {

    private int currentLevel;
    private static final int MAX_LEVEL = 3;

    private AlertDialog loadingDialog;

    private long totalTimeUsed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_game_result);

        // When starting to load
        loadingDialog = dialog_loading_helper.showLoadingDialog(quizGame_result.this, "⌛ Generating the new quiz...");
        // Hide the dialog after response)
        dialog_loading_helper.hideLoadingDialog(loadingDialog);

        totalTimeUsed = getIntent().getLongExtra("accumulatedTime", 0);

        TextView timeUsedText = findViewById(R.id.timeUsedText); // Add in layout first
        timeUsedText.setText(MessageFormat.format("Total Time: {0} seconds", totalTimeUsed / 1000));

        TextView scoreText = findViewById(R.id.scoreText);
        TextView feedbackText = findViewById(R.id.feedbackText);
        Button continueButton = findViewById(R.id.continueButton);
        Button backHomeButton = findViewById(R.id.backHomeButton);

        int score = getIntent().getIntExtra("score", 0);
        int total = getIntent().getIntExtra("total", 3);
        currentLevel = getIntent().getIntExtra("currentLevel", 1);

        scoreText.setText(MessageFormat.format("You got {0} out of {1} correct!", score, total));

        if (score == total) {
            feedbackText.setText(getString(R.string.perfect_score_ready_for_harder_questions));
        } else if (score >= total / 2) {
            feedbackText.setText(getString(R.string.good_effort_let_s_improve_next));
        } else {
            feedbackText.setText(getString(R.string.keep_practicing));
        }

        if (currentLevel > MAX_LEVEL) {
            continueButton.setVisibility(View.GONE); // No more levels will be performed because of achieving the maximum level
        }

        continueButton.setOnClickListener(v -> {
            loadingDialog.show();
            String topic = getIntent().getStringExtra("topic");
            String userQuestion = getIntent().getStringExtra("userQuestion");

            String difficulty;
            if (currentLevel == 1) {
                difficulty = "EASY";
            } else if (currentLevel == 2) {
                difficulty = "MEDIUM";
            } else {
                difficulty = "HARD";
            }


            String prompt = "You are a quiz generator for primary school students. Focused on the" + userQuestion + " in topic \"" + topic + "\", create a "
                    + difficulty + " level JSON array with exactly 3 multiple-choice questions. " +
                    "Each question should be an object containing 'question', 'options' (an array of 4 choices), and 'answer'. " +
                    "Ensure the complexity reflects the following guidelines:\n\n" +
                    "- EASY: use simple, direct questions with one digit small numbers or basic facts.\n" +
                    "- MEDIUM: use slightly longer questions with more options, real-world context, or moderate two digit numbers.\n" +
                    "- HARD: use multi-step reasoning, tricky wording, smaller three digit numbers, or unfamiliar formats that require higher-order thinking.\n\n" +
                    "Ensure all questions are strictly related to \"" + userQuestion + "\". " +
                    "Return ONLY a valid JSON array. Do NOT include explanations or text outside the array.";


            chatGPT_services.callChatGPT(quizGame_result.this, prompt, new chatGPT_services.ChatGPTCallback() {
                @Override
                public void onSuccess(String reply) {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        Intent intent = new Intent(quizGame_result.this, quizGame.class);
                        intent.putExtra("currentLevel", currentLevel);
                        intent.putExtra("quizJson", reply);
                        intent.putExtra("topic", topic);
                        intent.putExtra("userQuestion", userQuestion);
                        intent.putExtra("accumulatedTime", totalTimeUsed);
                        startActivity(intent);
                        finish();
                    });
                }

                @Override
                public void onError() {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        Toast.makeText(quizGame_result.this, "Failed to generate new quiz.", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });


        backHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(quizGame_result.this, chatRoom.class);
            startActivity(intent);
            finish();
        });
    }


}
