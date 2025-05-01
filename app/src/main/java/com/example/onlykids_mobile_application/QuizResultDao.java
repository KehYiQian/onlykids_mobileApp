package com.example.onlykids_mobile_application;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface QuizResultDao {
    @Insert
    void insert(QuizResultEntity result);

    @Query("SELECT * FROM quiz_results")
    List<QuizResultEntity> getAll();

    @Delete
    void delete(QuizResultEntity result);

    @Query("DELETE FROM quiz_results")
    void deleteAll();
}
