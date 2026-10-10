package com.example.tripboxie.utils;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Wish.class, Plan.class, DayPlan.class}, version = 4) // ！修改到 room 記得更新版本！
public abstract class AppDatabase extends RoomDatabase {
    public abstract WishDao wishDao();
    public abstract PlanDao planDao();
    public abstract DayPlanDao dayPlanDao();
    private static volatile AppDatabase INSTANCE;
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // 建立資料庫
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "tripboxie_db")
                            .allowMainThreadQueries() // 允許在主程式修改 room 資料
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}