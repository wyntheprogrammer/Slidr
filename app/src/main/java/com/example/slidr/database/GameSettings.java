package com.example.slidr.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "game_settings")
public class GameSettings {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId; // ADDED: Link to User table for user-specific settings

    private boolean musicEnabled;
    private int selectedMusicId; // Currently selected music track ID
    private float musicVolume; // 0.0 to 1.0

    public GameSettings() {
        this.userId = 0; // Will be set when user logs in
        this.musicEnabled = false; // Default to OFF
        this.selectedMusicId = -1; // -1 means "No Music" selected
        this.musicVolume = 0.7f;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public boolean isMusicEnabled() { return musicEnabled; }
    public void setMusicEnabled(boolean musicEnabled) { this.musicEnabled = musicEnabled; }

    public int getSelectedMusicId() { return selectedMusicId; }
    public void setSelectedMusicId(int selectedMusicId) { this.selectedMusicId = selectedMusicId; }

    public float getMusicVolume() { return musicVolume; }
    public void setMusicVolume(float musicVolume) { this.musicVolume = musicVolume; }
}