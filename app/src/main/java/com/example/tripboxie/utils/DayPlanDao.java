package com.example.tripboxie.utils;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DayPlanDao {

    @Insert
    void insert(DayPlan dayPlan);
    @Update
    void update(DayPlan dayPlan);

    @Query("SELECT * FROM day_plan_table WHERE planId = :planId ORDER BY date ASC")
    List<DayPlan> getDaysByPlanId(int planId);
    @Query("SELECT * FROM day_plan_table WHERE planId = :planId AND date = :date LIMIT 1")
    DayPlan getDayPlanByPlanIdAndDate(int planId, String date);
    @Query("DELETE FROM day_plan_table WHERE planId = :planId")
    void deleteDaysByPlan(int planId);
}
