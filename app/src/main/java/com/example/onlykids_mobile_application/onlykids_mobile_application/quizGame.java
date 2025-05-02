package com.example.onlykids_mobile_application.onlykids_mobile_application;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.onlykids.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class quizGame extends AppCompatActivity {

    private TextView questionText, timerText;
    private final Button[] optionButtons = new Button[4];
    private final ImageView[] statusIcons = new ImageView[4];
    private Button submitNextButton;

    private List<QuizQuestion> quizList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private int totalAnswers = 0;

    private int currentLevel = 1;
    private String topic = "";
    private String userQuestion = "";

    private boolean answerSubmitted = false;

    private final Handler timerHandler = new Handler();
    private long startTimeMillis;
    private long timeUsedThisLevel = 0;
    private long accumulatedTime = 0;

    private Mistake_database db; // Room database for saving mistakes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_game);

        timerText = findViewById(R.id.timerText);

        startTimeMillis = System.currentTimeMillis();
        accumulatedTime = getIntent().getLongExtra("accumulatedTime", 0);

        timerHandler.postDelayed(updateTimerRunnable, 0);

        submitNextButton = findViewById(R.id.submitNextButton);
        submitNextButton.setVisibility(View.INVISIBLE);

        questionText = findViewById(R.id.quizText);
        optionButtons[0] = findViewById(R.id.optionA);
        optionButtons[1] = findViewById(R.id.optionB);
        optionButtons[2] = findViewById(R.id.optionC);
        optionButtons[3] = findViewById(R.id.optionD);
        statusIcons[0] = findViewById(R.id.statusA);
        statusIcons[1] = findViewById(R.id.statusB);
        statusIcons[2] = findViewById(R.id.statusC);
        statusIcons[3] = findViewById(R.id.statusD);

        currentLevel = getIntent().getIntExtra("currentLevel", 1);
        String quizJson = getIntent().getStringExtra("quizJson");
        topic = getIntent().getStringExtra("topic");
        userQuestion = getIntent().getStringExtra("userQuestion");

        // Setup database
        db = Room.databaseBuilder(getApplicationContext(), Mistake_database.class, "mistake_db")
                .allowMainThreadQueries()
                .build();

        if (quizJson == null || quizJson.isEmpty()) {
            questionText.setText(getString(R.string.no_quiz_found));
            Toast.makeText(this, "❌ No quiz data available!", Toast.LENGTH_SHORT).show();
            return;
        }

        quizList = parseQuizJson(quizJson);

        currentQuestionIndex = 0;
        correctAnswers = 0;
        totalAnswers = quizList.size();

        if (quizList.isEmpty()) {
            questionText.setText(getString(R.string.failed_to_load_quiz_questions));
            Toast.makeText(this, "No valid quiz questions!", Toast.LENGTH_SHORT).show();
            return;
        }

        showQuestion();

        submitNextButton.setOnClickListener(v -> {
            if (!answerSubmitted) {
                Toast.makeText(this, "⚠️ Please select an answer first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<QuizQuestion> parseQuizJson(String json) {
        List<QuizQuestion> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String question = obj.getString("question");
                JSONArray optionsArray = obj.getJSONArray("options");
                List<String> options = new ArrayList<>();
                for (int j = 0; j < optionsArray.length(); j++) {
                    options.add(optionsArray.getString(j));
                }
                String answer = obj.getString("answer");
                list.add(new QuizQuestion(question, options, answer));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void showQuestion() {
        if (currentQuestionIndex >= quizList.size()) {
            timerHandler.removeCallbacks(updateTimerRunnable);

            String difficulty;
            if (currentLevel == 1) {
                difficulty = "EASY";
            } else if (currentLevel == 2) {
                difficulty = "MEDIUM";
            } else {
                difficulty = "HARD";
            }
            // Save quiz result to SQLite
            QuizResultEntity result = new QuizResultEntity(
                    topic,
                    correctAnswers,
                    difficulty,
                    0
            );
            QuizResultDatabase.getInstance(getApplicationContext())
                    .quizResultDao()
                    .insert(result);

            Intent intent = new Intent(quizGame.this, quizGame_result.class);
            intent.putExtra("score", correctAnswers);
            intent.putExtra("total", totalAnswers);
            intent.putExtra("currentLevel", currentLevel + 1);
            intent.putExtra("topic", topic);
            intent.putExtra("userQuestion", userQuestion);
            intent.putExtra("accumulatedTime", accumulatedTime + timeUsedThisLevel);
            startActivity(intent);
            finish();
            return;
        }

        QuizQuestion currentQ = quizList.get(currentQuestionIndex);
        questionText.setText(MessageFormat.format("{0}{1}: {2}", getString(R.string.level), currentLevel, currentQ.question));

        for (int i = 0; i < 4; i++) {
            Button btn = optionButtons[i];
            ImageView icon = statusIcons[i];

            btn.setText(currentQ.options.get(i));
            btn.setVisibility(View.VISIBLE);
            btn.setEnabled(true);
            btn.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_light));
            icon.setVisibility(View.INVISIBLE);

            final int index = i;
            btn.setOnClickListener(v ->
                handleAnswer(index)
            );
        }

        submitNextButton.setText(getString(R.string.submit));
        submitNextButton.setVisibility(View.INVISIBLE);
        answerSubmitted = false;
    }

    private void handleAnswer(int selectedIndex) {
        for (Button b : optionButtons) {
            b.setEnabled(false);
        }

        QuizQuestion currentQ = quizList.get(currentQuestionIndex);
        String selectedOption = optionButtons[selectedIndex].getText().toString();
        boolean isCorrect = selectedOption.equals(currentQ.answer);

        if (isCorrect) {
            correctAnswers++;
            optionButtons[selectedIndex].setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
            statusIcons[selectedIndex].setImageResource(R.drawable.icon_correct);

            //  fade in icon
            statusIcons[selectedIndex].startAnimation(
                    android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in)
            );

            //  Pulse animation for button
            optionButtons[selectedIndex].animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).withEndAction(() -> {
                optionButtons[selectedIndex].animate().scaleX(1f).scaleY(1f).setDuration(150).start();
            }).start();
        } else {
            optionButtons[selectedIndex].setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
            statusIcons[selectedIndex].setImageResource(R.drawable.icon_wrong);

            //  fade in icon
            statusIcons[selectedIndex].startAnimation(
                    android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in)
            );

            // Shake animation for wrong button
            optionButtons[selectedIndex].startAnimation(
                    android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake)
            );

            saveMistake(currentQ);
        }

        statusIcons[selectedIndex].setVisibility(View.VISIBLE);

        submitNextButton.setVisibility(View.VISIBLE);
        submitNextButton.setText(getString(R.string.next));
        answerSubmitted = true;

        submitNextButton.setOnClickListener(v -> {
            if (answerSubmitted) {
                for (Button btn : optionButtons) {
                    btn.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_light));
                }
                for (ImageView icon : statusIcons) {
                    icon.setVisibility(View.INVISIBLE);
                }

                currentQuestionIndex++;
                showQuestion();
            }
        });
    }


    private void saveMistake(QuizQuestion mistakeQuestion) {
        String uuid = UUID.randomUUID().toString();
        String optionsJson = new Gson().toJson(mistakeQuestion.options);

        Mistake_entity mistake = new Mistake_entity(
                uuid,
                mistakeQuestion.question,
                optionsJson,
                mistakeQuestion.answer,
                topic,
                currentLevel,
                timeUsedThisLevel
        );

        db.mistakeDao().insert(mistake);
    }

    private final Runnable updateTimerRunnable = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            timeUsedThisLevel = now - startTimeMillis;

            long totalSeconds = (accumulatedTime + timeUsedThisLevel) / 1000;
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
            timerText.setText(String.format(getString(R.string.time_02d_02d), minutes, seconds));

            timerHandler.postDelayed(this, 1000);
        }
    };

    private static class QuizQuestion {
        String question;
        List<String> options;
        String answer;

        QuizQuestion(String question, List<String> options, String answer) {
            this.question = question;
            this.options = options;
            this.answer = answer;
        }
    }
}
