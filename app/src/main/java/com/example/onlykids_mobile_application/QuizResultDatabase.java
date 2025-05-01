package com.example.onlykids_mobile_application;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {QuizResultEntity.class}, version = 1)
public abstract class QuizResultDatabase extends RoomDatabase {
    public abstract QuizResultDao quizResultDao();

    private static volatile QuizResultDatabase INSTANCE;

    public static QuizResultDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (QuizResultDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    QuizResultDatabase.class, "quiz_results.db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries() // for testing only!
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
