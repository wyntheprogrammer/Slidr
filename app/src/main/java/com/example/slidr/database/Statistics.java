package com.example.slidr.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "statistics")
public class Statistics {
    @PrimaryKey
    private int gridSize;

    private int gamesPlayed;
    private int gamesCompleted;
    private int bestMoves;
    private long bestTime;
    private int totalMoves;
    private long totalTime;

    public Statistics(int gridSize) {
        this.gridSize = gridSize;
        this.gamesPlayed = 0;
        this.gamesCompleted = 0;
        this.bestMoves = Integer.MAX_VALUE;
        this.bestTime = Long.MAX_VALUE;
        this.totalMoves = 0;
        this.totalTime = 0;
    }

    // Getters and Setters
    public int getGridSize() { return gridSize; }
    public void setGridSize(int gridSize) { this.gridSize = gridSize; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public int getGamesCompleted() { return gamesCompleted; }
    public void setGamesCompleted(int gamesCompleted) { this.gamesCompleted = gamesCompleted; }

    public int getBestMoves() { return bestMoves; }
    public void setBestMoves(int bestMoves) { this.bestMoves = bestMoves; }

    public long getBestTime() { return bestTime; }
    public void setBestTime(long bestTime) { this.bestTime = bestTime; }

    public int getTotalMoves() { return totalMoves; }
    public void setTotalMoves(int totalMoves) { this.totalMoves = totalMoves; }

    public long getTotalTime() { return totalTime; }
    public void setTotalTime(long totalTime) { this.totalTime = totalTime; }
}