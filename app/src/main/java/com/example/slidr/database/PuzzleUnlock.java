package com.example.slidr.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "puzzle_unlocks")
public class PuzzleUnlock {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId; // Link to User table
    private String storyMode; // "onepiece", "dragonball", "bleach"
    private int storyIndex;
    private boolean unlocked;
    private int starsEarned;
    private int bestMoves;
    private long bestTime;

    public PuzzleUnlock(int userId, String storyMode, int storyIndex, boolean unlocked, int starsEarned) {
        this.userId = userId;
        this.storyMode = storyMode;
        this.storyIndex = storyIndex;
        this.unlocked = unlocked;
        this.starsEarned = starsEarned;
        this.bestMoves = Integer.MAX_VALUE;
        this.bestTime = Long.MAX_VALUE;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getStoryMode() { return storyMode; }
    public void setStoryMode(String storyMode) { this.storyMode = storyMode; }

    public int getStoryIndex() { return storyIndex; }
    public void setStoryIndex(int storyIndex) { this.storyIndex = storyIndex; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }

    public int getStarsEarned() { return starsEarned; }
    public void setStarsEarned(int starsEarned) { this.starsEarned = starsEarned; }

    public int getBestMoves() { return bestMoves; }
    public void setBestMoves(int bestMoves) { this.bestMoves = bestMoves; }

    public long getBestTime() { return bestTime; }
    public void setBestTime(long bestTime) { this.bestTime = bestTime; }
}