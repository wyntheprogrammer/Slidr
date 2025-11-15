package com.example.slidr.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "game_history")
public class GameHistory {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int gridSize;
    private int moves;
    private long timeInSeconds;
    private long timestamp;
    private boolean completed;

    public GameHistory(int gridSize, int moves, long timeInSeconds, long timestamp, boolean completed) {
        this.gridSize = gridSize;
        this.moves = moves;
        this.timeInSeconds = timeInSeconds;
        this.timestamp = timestamp;
        this.completed = completed;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGridSize() { return gridSize; }
    public void setGridSize(int gridSize) { this.gridSize = gridSize; }

    public int getMoves() { return moves; }
    public void setMoves(int moves) { this.moves = moves; }

    public long getTimeInSeconds() { return timeInSeconds; }
    public void setTimeInSeconds(long timeInSeconds) { this.timeInSeconds = timeInSeconds; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}