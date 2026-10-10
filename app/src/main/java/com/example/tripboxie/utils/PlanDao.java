package com.example.tripboxie.utils;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlanDao {
    @Insert
    void insert(Plan plan);
    @Update
    void update(Plan plan);
    @Query("SELECT * FROM plan_table")
    List<Plan> getAll();
    @Query("SELECT * FROM plan_table WHERE id = :id LIMIT 1")
    Plan getPlanById(int id);
    @Query("DELETE FROM plan_table")
    void deleteAll();
    @Query("DELETE FROM plan_table WHERE id = :planId")
    void deleteById(int planId);
}