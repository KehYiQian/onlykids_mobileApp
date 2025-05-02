package com.example.onlykids_mobile_application;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Mistake_entity.class}, version = 1, exportSchema = false)
public abstract class Mistake_database extends RoomDatabase {

    public abstract MistakeDao mistakeDao();
}
