package com.example.slidr.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "music_tracks")
public class MusicTrack {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId; // Link to User table - ADDED FOR USER-SPECIFIC UNLOCKS
    private String trackName;
    private String storyMode; // "onepiece", "dragonball", "bleach", or "default"
    private int storyIndex;
    private int musicResId; // R.raw.music_file
    private boolean unlocked;

    // Constructor for database initialization (without userId)
    public MusicTrack(String trackName, String storyMode, int storyIndex, int musicResId, boolean unlocked) {
        this.userId = 0; // Will be set when user logs in
        this.trackName = trackName;
        this.storyMode = storyMode;
        this.storyIndex = storyIndex;
        this.musicResId = musicResId;
        this.unlocked = unlocked;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTrackName() { return trackName; }
    public void setTrackName(String trackName) { this.trackName = trackName; }

    public String getStoryMode() { return storyMode; }
    public void setStoryMode(String storyMode) { this.storyMode = storyMode; }

    public int getStoryIndex() { return storyIndex; }
    public void setStoryIndex(int storyIndex) { this.storyIndex = storyIndex; }

    public int getMusicResId() { return musicResId; }
    public void setMusicResId(int musicResId) { this.musicResId = musicResId; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
}