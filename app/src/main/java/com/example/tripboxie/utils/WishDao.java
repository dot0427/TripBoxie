package com.example.tripboxie.utils;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WishDao {
    @Insert
    void insert(Wish wish);
    @Update
    void update(Wish wish);
    @Query("SELECT * FROM wish_table")
    List<Wish> getAll();
    @Query("SELECT * FROM wish_table WHERE id = :id LIMIT 1")
    Wish getWishById(int id);
    @Query("DELETE FROM wish_table")
    void deleteAll();
    @Query("DELETE FROM wish_table WHERE id = :wishId")
    void deleteById(int wishId);
}
