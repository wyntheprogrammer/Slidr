package com.example.slidr.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_progress")
public class UserProgress {
    @PrimaryKey
    private int userId = 1; // Single user

    private int totalStars;

    public UserProgress() {
        this.totalStars = 0;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTotalStars() { return totalStars; }
    public void setTotalStars(int totalStars) { this.totalStars = totalStars; }
}