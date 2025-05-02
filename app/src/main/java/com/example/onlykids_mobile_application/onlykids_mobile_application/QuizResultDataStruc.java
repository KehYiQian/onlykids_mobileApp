package com.example.onlykids_mobile_application.onlykids_mobile_application;

public class QuizResultDataStruc {

    private String subject;
    private int score;
    private int maxScore;
    private String difficulty;
    private int hintsUsed;


    public QuizResultDataStruc(String subject, int score, String difficulty, int hintsUsed) {
        this.subject = subject;
        this.score = score;
        this.maxScore = 3;
        this.difficulty = difficulty;
        this.hintsUsed = hintsUsed;


    }

    public String getSubject() { return subject; }
    public int getScore() { return score; }
    public int getMaxScore() { return maxScore; }
    public String getDifficulty() { return difficulty; }
    public int getHintsUsed() { return hintsUsed; }

}
