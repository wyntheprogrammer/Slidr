package com.example.slidr.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "music_tracks")
public class MusicTrack {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String trackName;
    private String storyMode; // "onepiece", "dragonball", "bleach", or "default"
    private int arcIndex;
    private int musicResId; // R.raw.music_file
    private boolean unlocked;

    public MusicTrack(String trackName, String storyMode, int arcIndex, int musicResId, boolean unlocked) {
        this.trackName = trackName;
        this.storyMode = storyMode;
        this.arcIndex = arcIndex;
        this.musicResId = musicResId;
        this.unlocked = unlocked;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTrackName() { return trackName; }
    public void setTrackName(String trackName) { this.trackName = trackName; }

    public String getStoryMode() { return storyMode; }
    public void setStoryMode(String storyMode) { this.storyMode = storyMode; }

    public int getArcIndex() { return arcIndex; }
    public void setArcIndex(int arcIndex) { this.arcIndex = arcIndex; }

    public int getMusicResId() { return musicResId; }
    public void setMusicResId(int musicResId) { this.musicResId = musicResId; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
}