package com.example.tripboxie.utils;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "day_plan_table",
        foreignKeys = @ForeignKey(
                entity = Plan.class,
                parentColumns = "id",
                childColumns = "planId",
                onDelete = ForeignKey.CASCADE))
public class DayPlan implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int planId;
    public String date;
    public String activitiesJson;

    public DayPlan(int planId, String date, String activitiesJson) {
        this.planId = planId;
        this.date = date;
        this.activitiesJson = activitiesJson;
    }

    public DayPlan() {}
    public String getDate() {
        return date;
    }

    public String getActivitiesJson() {
        return activitiesJson;
    }
}
