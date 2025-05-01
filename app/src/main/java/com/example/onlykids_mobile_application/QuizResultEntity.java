package com.example.onlykids_mobile_application;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_results")
public class QuizResultEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String subject;
    public int score;
    public String difficulty;
    public int hintsUsed;

    public QuizResultEntity(String subject, int score, String difficulty, int hintsUsed) {
        this.subject = subject;
        this.score = score;
        this.difficulty = difficulty;
        this.hintsUsed = hintsUsed;
    }
}
