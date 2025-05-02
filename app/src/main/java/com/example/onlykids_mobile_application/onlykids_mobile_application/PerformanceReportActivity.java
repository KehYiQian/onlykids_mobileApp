package com.example.onlykids_mobile_application.onlykids_mobile_application;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import com.example.onlykids.R;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PerformanceReportActivity extends AppCompatActivity {

    private TextView tvQuizDone, tvAveScore, /*tvHintUsed,*/ tvGroupInfo, labelXAxis, labelYAxis, tvFeedbackDetail, tvUserInfo ;
    private Spinner subjectSpinner, timeSpinner;
    private LineChart lineChart;
    private List<QuizResultDataStruc> allQuizResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance_report);

        setupViews();
        SharedPreferences prefs = getSharedPreferences("OnlyKidsPrefs", MODE_PRIVATE);
        String name = prefs.getString("user_name", "Unknown");
        String age = prefs.getString("user_age", "-");
        String level = prefs.getString("user_level", "-");

        String userInfo = "Name: " + name + "   Age: " + age + "   Level: " + level;
        tvUserInfo.setText(userInfo);

        ImageButton btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> {
            prefs.edit().putBoolean("editProfile", true).apply();
            startActivity(new Intent(this, preclass_question.class));
        });

        setupSpinners();
        QuizResultDatabase db = QuizResultDatabase.getInstance(this);
        QuizResultDao dao = db.quizResultDao();
        List<QuizResultEntity> raw = dao.getAll();
        allQuizResults = convertToStruct(raw);
        refreshReport();
    }

    private List<QuizResultDataStruc> convertToStruct(List<QuizResultEntity> raw) {
        List<QuizResultDataStruc> list = new ArrayList<>();
        for (QuizResultEntity e : raw) {
            list.add(new QuizResultDataStruc(e.subject, e.score, e.difficulty, e.hintsUsed));
            //sample("Math", 3, "EASY", 0)) hintUsed for future implementation
        }
        return list;
    }

    private void setupViews() {
        tvAveScore = findViewById(R.id.tvAveScore);
        //tvHintUsed = findViewById(R.id.tvHintUsed);
        tvQuizDone = findViewById(R.id.tvQuizDone);
        tvGroupInfo = findViewById(R.id.tvGroupInfo);
        labelXAxis = findViewById(R.id.labelXAxis);
        labelYAxis = findViewById(R.id.labelYAxis);
        tvFeedbackDetail = findViewById(R.id.tvFeedbackDetail);
        lineChart = findViewById(R.id.lineChart);

        tvUserInfo = findViewById(R.id.tvUserInfo);
        subjectSpinner = findViewById(R.id.spinnerReportSubject);
        timeSpinner = findViewById(R.id.spinnerReportTime);
    }

    private void setupSpinners() {
        List<String> subjects = Arrays.asList("Math", "English", "Science");

        ArrayAdapter<String> SubjectSpinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        SubjectSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(SubjectSpinAdapter);

        List<String> time = Arrays.asList("Recent Attempt", "All");

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, time);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);

        subjectSpinner.setOnItemSelectedListener(new SimpleSpinnerListener(this::refreshReport));
        timeSpinner.setOnItemSelectedListener(new SimpleSpinnerListener(this::refreshReport));
    }

    private void refreshReport() {
        String subject = subjectSpinner.getSelectedItem().toString();
        boolean isRecent = timeSpinner.getSelectedItemPosition() == 0;
        List<QuizResultDataStruc> filtered = filterData(subject);
        List<Entry> entries;

        if (isRecent) {
            List<QuizResultDataStruc> last10 = getLastN(filtered, 10);
            entries = convertToEntries(last10);
            updateInsights(last10);
            tvGroupInfo.setVisibility(View.GONE);
            labelXAxis.setText("Quiz Attempt");
        } else {
            List<Float> groupedScores = groupInto10Scores(filtered);
            entries = convertToEntriesFromFloat(groupedScores);
            updateInsights(filtered);
            tvGroupInfo.setVisibility(View.VISIBLE);
            labelXAxis.setText("Group");
        }

        updateChart(entries, isRecent);
    }

    private List<QuizResultDataStruc> filterData(String subject) {
        List<QuizResultDataStruc> result = new ArrayList<>();
        for (QuizResultDataStruc q : allQuizResults) {
            if (q.getSubject().equalsIgnoreCase(subject)) {
                result.add(q);
            }
        }
        return result;
    }

    private List<QuizResultDataStruc> getLastN(List<QuizResultDataStruc> list, int n) {
        int size = list.size();
        return list.subList(Math.max(0, size - n), size);
    }

    private List<Float> groupInto10Scores(List<QuizResultDataStruc> list) {
        List<Float> groupedScores = new ArrayList<>();
        int size = list.size();
        if (size == 0) return groupedScores;

        int totalGroups = Math.min(10, size);
        int baseSize = size / totalGroups;
        int remainder = size % totalGroups;

        int index = 0;
        for (int i = 0; i < totalGroups; i++) {
            int groupSize = baseSize + (i < remainder ? 1 : 0);
            int end = index + groupSize;
            List<QuizResultDataStruc> group = list.subList(index, end);

            float total = 0f;
            for (QuizResultDataStruc q : group) {
                total += getNormalizedScore(q);
            }
            groupedScores.add(total / group.size());
            index = end;
        }

        return groupedScores;
    }

    private List<Entry> convertToEntries(List<QuizResultDataStruc> list) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            entries.add(new Entry(i + 1, getNormalizedScore(list.get(i))));
        }
        return entries;
    }

    private List<Entry> convertToEntriesFromFloat(List<Float> values) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            entries.add(new Entry(i + 1, values.get(i)));
        }
        return entries;
    }

    private void updateChart(List<Entry> entries, boolean isRecent) {
        LineDataSet dataSet = new LineDataSet(entries, "Normalized Scores");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.RED);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);

        lineChart.setData(new LineData(dataSet));
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        if (!isRecent) {
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "G" + (int) value;
                }
            });
        } else {
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) value);
                }
            });
        }

        xAxis.setAvoidFirstLastClipping(true);
        lineChart.setExtraRightOffset(20f);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }

    private void updateInsights(List<QuizResultDataStruc> data) {
        float total = 0;
        //int hints = 0;
        for (QuizResultDataStruc q : data) {
            total += getNormalizedScore(q);
            //hints += q.getHintsUsed();
        }

        float avg = data.isEmpty() ? 0 : total / data.size();
        tvQuizDone.setText("Quiz Done: " + data.size());
        //tvHintUsed.setText("Total Hints: " + hints);
        tvAveScore.setText("Average Score: " + String.format("%.2f", avg));

//        //--- Hint Feedback ---
//        String hintFeedback;
//        float avgHint = data.isEmpty() ? 0 : (float) hints / data.size();
//        if (hints == 0) {
//            hintFeedback = "💡 Awesome! You didn’t use any hints.";
//        } else if (avgHint <= 0.5f) {
//            hintFeedback = "💡 Great job! You’re barely using hints.";
//        } else if (avgHint <= 1.5f) {
//            hintFeedback = "💡 Try to rely a little less on hints.";
//        } else {
//            hintFeedback = "💡 Let’s use fewer hints next time!";
//        }

        // --- Score Trend Feedback using linear regression ---
        String trendFeedback;
        List<Float> scores = new ArrayList<>();
        for (QuizResultDataStruc q : data) {
            scores.add(getNormalizedScore(q));
        }

        if (scores.size() < 3) {
            trendFeedback = "📉 Not enough data yet to see a trend. Keep playing!";
        } else {
            float slope = getTrendSlope(scores);
            if (slope > 0.1f) {
                trendFeedback = "🎯 You're improving! Keep it up!";
            } else if (slope < -0.1f) {
                trendFeedback = "📉 Scores are dropping. Let’s improve together.";
            } else {
                trendFeedback = "🎯 You're staying consistent. Great effort!";
            }
        }

        tvFeedbackDetail.setText(trendFeedback);
//        //hintUsed for future use
//        if (data.size()==0){
//            tvFeedbackDetail.setText(trendFeedback);
//        }
//        else{
//            tvFeedbackDetail.setText(trendFeedback + "\n" + hintFeedback);
//        }

    }

    private float getTrendSlope(List<Float> scores) {
        int n = scores.size();
        float sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < n; i++) {
            float x = i + 1, y = scores.get(i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }
        return (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
    }

    private float getNormalizedScore(QuizResultDataStruc quiz) {
        String diff = quiz.getDifficulty().toLowerCase();
        float multiplier;
        switch (diff) {
            case "medium":
                multiplier = 1.25f;
                break;
            case "hard":
                multiplier = 1.5f;
                break;
            default:
                multiplier = 1.0f;
                break;
        }
        return quiz.getScore() * multiplier;
    }

    public class SimpleSpinnerListener implements AdapterView.OnItemSelectedListener {

        private final Runnable callback;

        public SimpleSpinnerListener(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            callback.run();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // No-op
        }
    }

}
