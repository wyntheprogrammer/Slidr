package com.example.slidr.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_progress")
public class UserProgress {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId;
    private int totalStars;

    // Constructor used by Room
    public UserProgress() {
        this.totalStars = 0;
    }

    // Ignore this constructor so Room won't use it
    @Ignore
    public UserProgress(int userId) {
        this.userId = userId;
        this.totalStars = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTotalStars() { return totalStars; }
    public void setTotalStars(int totalStars) { this.totalStars = totalStars; }
}
