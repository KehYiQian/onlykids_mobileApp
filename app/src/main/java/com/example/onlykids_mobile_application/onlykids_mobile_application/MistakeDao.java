package com.example.onlykids_mobile_application.onlykids_mobile_application;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MistakeDao {
    @Insert
    void insert(Mistake_entity mistake);

    @Query("SELECT * FROM mistake_memo ORDER BY level ASC")
    List<Mistake_entity> getAllMistakes();

    @Query("DELETE FROM mistake_memo")
    void clearAll();

    @Query("DELETE FROM mistake_memo WHERE id = :id")
    void clearById(String id);

}
