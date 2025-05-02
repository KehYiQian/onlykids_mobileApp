package com.example.onlykids_mobile_application.onlykids_mobile_application;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "mistake_memo")
public class Mistake_entity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "question")
    public String question;

    @ColumnInfo(name = "optionsJson")
    public String optionsJson;

    @ColumnInfo(name = "answer")
    public String answer;

    @ColumnInfo(name = "topic")
    public String topic;

    @ColumnInfo(name = "level")
    public int level;

    @ColumnInfo(name = "timeUsedMillis")
    public long timeUsedMillis;

    public Mistake_entity(@NonNull String id, String question, String optionsJson, String answer, String topic, int level, long timeUsedMillis) {
        this.id = id;
        this.question = question;
        this.optionsJson = optionsJson;
        this.answer = answer;
        this.topic = topic;
        this.level = level;
        this.timeUsedMillis = timeUsedMillis;
    }
}
