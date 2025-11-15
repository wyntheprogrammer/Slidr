package com.example.slidr.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "puzzle_unlocks")
public class PuzzleUnlock {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String storyMode; // "onepiece", "dragonball", "bleach"
    private int arcIndex;
    private boolean unlocked;
    private int starsEarned;
    private int bestMoves;
    private long bestTime;

    public PuzzleUnlock(String storyMode, int arcIndex, boolean unlocked, int starsEarned) {
        this.storyMode = storyMode;
        this.arcIndex = arcIndex;
        this.unlocked = unlocked;
        this.starsEarned = starsEarned;
        this.bestMoves = Integer.MAX_VALUE;
        this.bestTime = Long.MAX_VALUE;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStoryMode() { return storyMode; }
    public void setStoryMode(String storyMode) { this.storyMode = storyMode; }

    public int getArcIndex() { return arcIndex; }
    public void setArcIndex(int arcIndex) { this.arcIndex = arcIndex; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }

    public int getStarsEarned() { return starsEarned; }
    public void setStarsEarned(int starsEarned) { this.starsEarned = starsEarned; }

    public int getBestMoves() { return bestMoves; }
    public void setBestMoves(int bestMoves) { this.bestMoves = bestMoves; }

    public long getBestTime() { return bestTime; }
    public void setBestTime(long bestTime) { this.bestTime = bestTime; }
}